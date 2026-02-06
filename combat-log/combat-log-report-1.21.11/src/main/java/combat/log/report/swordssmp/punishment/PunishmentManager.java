package combat.log.report.swordssmp.punishment;

import combat.log.report.swordssmp.CombatHeadManager;
import combat.log.report.swordssmp.CombatLogReport;
import combat.log.report.swordssmp.incident.CombatLogIncident;
import combat.log.report.swordssmp.incident.IncidentStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pending punishments for combat loggers
 */
public class PunishmentManager {
    private static final PunishmentManager INSTANCE = new PunishmentManager();
    private final Map<UUID, PendingPunishment> pendingPunishments = new ConcurrentHashMap<>();

    private PunishmentManager() {}

    public static PunishmentManager getInstance() {
        return INSTANCE;
    }

    /**
     * Add a pending punishment for a player
     * shouldBan: Ban player immediately (while ticket pending)
     * shouldKillOnLogin: Kill player on login if ticket is denied
     */
    public void addPendingPunishment(UUID playerUuid, UUID incidentId, boolean shouldBan, boolean shouldKillOnLogin) {
        PendingPunishment punishment = new PendingPunishment(playerUuid, incidentId, shouldBan, shouldKillOnLogin);
        pendingPunishments.put(playerUuid, punishment);
        
        CombatLogReport.LOGGER.info("Added pending punishment for player {} (incident: {}, ban: {}, kill: {})",
            playerUuid, incidentId, shouldBan, shouldKillOnLogin);
    }

    /**
     * Check if player has pending punishment
     */
    public boolean hasPendingPunishment(UUID playerUuid) {
        return pendingPunishments.containsKey(playerUuid);
    }

    /**
     * Get pending punishment for a player
     */
    public PendingPunishment getPendingPunishment(UUID playerUuid) {
        return pendingPunishments.get(playerUuid);
    }

    /**
     * Clear punishment for a player (after resolution)
     */
    public void clearPunishment(UUID playerUuid) {
        PendingPunishment removed = pendingPunishments.remove(playerUuid);
        if (removed != null) {
            CombatLogReport.LOGGER.info("Cleared punishment for player {}", playerUuid);
        }
    }

    /**
     * Execute punishment on player login based on incident status
     * Returns true if player should be kicked/banned
     */
    public boolean handlePlayerLogin(ServerPlayer player, CombatLogIncident incident) {
        UUID playerUuid = player.getUUID();
        PendingPunishment punishment = pendingPunishments.get(playerUuid);
        
        if (punishment == null || incident == null) {
            return false; // No punishment or incident
        }

        IncidentStatus status = incident.getStatus();

        // If approved, this should have been cleared already, but handle it just in case
        if (status == IncidentStatus.APPROVED) {
            // Restore inventory and remove head
            CombatHeadManager headManager = CombatHeadManager.getInstance();
            if (headManager.hasStoredInventory(playerUuid)) {
                headManager.removeHeadAndRestoreInventory(player, playerUuid);
                player.sendSystemMessage(Component.literal(
                    "§a§lYour combat log appeal was approved! Your inventory has been restored."
                ));
            } else {
                player.sendSystemMessage(Component.literal(
                    "§a§lYour combat log appeal was approved! You may continue playing."
                ));
            }
            clearPunishment(playerUuid);
            return false; // Allow login
        }

        // If still pending, ban them
        if (status == IncidentStatus.PENDING || status == IncidentStatus.CLIP_UPLOADED) {
            if (punishment.shouldBan()) {
                CombatLogReport.LOGGER.warn("Player {} tried to login with pending ticket - banning", 
                    player.getName().getString());
                return true; // Should be kicked/banned
            } else {
                // Just notify
                player.sendSystemMessage(Component.literal(
                    "§e§lYou have a pending combat log ticket in Discord! Please submit your proof."
                ));
                return false;
            }
        }

        // If denied or auto-denied, kill player
        if (status == IncidentStatus.DENIED || status == IncidentStatus.AUTO_DENIED) {
            if (punishment.shouldKillOnLogin()) {
                CombatLogReport.LOGGER.warn("Executing punishment on player {} for denied combat log", 
                    player.getName().getString());
                
                player.hurt(player.damageSources().generic(), Float.MAX_VALUE);
                player.sendSystemMessage(Component.literal(
                    "§c§lYou were killed for combat logging.\n" +
                    "§eYour items are in a player head at your logout location.\n" +
                    "§7Ticket: " + incident.getId()
                ));
            }
            clearPunishment(playerUuid);
            return false;
        }

        return false;
    }

    /**
     * Update punishment status based on incident changes
     */
    public void updatePunishmentStatus(UUID playerUuid, IncidentStatus newStatus) {
        PendingPunishment punishment = pendingPunishments.get(playerUuid);
        if (punishment == null) {
            return;
        }

        // If approved, REMOVE punishment entirely
        if (newStatus == IncidentStatus.APPROVED) {
            clearPunishment(playerUuid);
            CombatLogReport.LOGGER.info("Cleared punishment for player {} - ticket approved", playerUuid);
            return;
        }
        
        // If denied, remove ban but keep kill
        if (newStatus == IncidentStatus.DENIED || newStatus == IncidentStatus.AUTO_DENIED) {
            punishment.setShouldBan(false);
            punishment.setShouldKillOnLogin(true);
            punishment.setReason("Your combat log ticket was denied");
            CombatLogReport.LOGGER.info("Updated punishment for player {} - denied", playerUuid);
        }
    }

    /**
     * Get all pending punishments (for persistence)
     */
    public Map<UUID, PendingPunishment> getAllPunishments() {
        return new ConcurrentHashMap<>(pendingPunishments);
    }

    /**
     * Load punishment from persistence
     */
    public void loadPunishment(PendingPunishment punishment) {
        pendingPunishments.put(punishment.getPlayerUuid(), punishment);
    }

    /**
     * Clear all punishments (for testing/reloading)
     */
    public void clearAll() {
        pendingPunishments.clear();
    }
}
