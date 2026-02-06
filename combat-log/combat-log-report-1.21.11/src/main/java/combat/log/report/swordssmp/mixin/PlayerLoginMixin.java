package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatLogReport;
import combat.log.report.swordssmp.incident.CombatLogIncident;
import combat.log.report.swordssmp.incident.IncidentManager;
import combat.log.report.swordssmp.punishment.PendingPunishment;
import combat.log.report.swordssmp.punishment.PunishmentManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerLoginMixin {
    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void onPlayerLogin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        PunishmentManager punishmentManager = PunishmentManager.getInstance();
        IncidentManager incidentManager = IncidentManager.getInstance();
        
        // Check if player has pending punishment
        if (punishmentManager.hasPendingPunishment(player.getUUID())) {
            PendingPunishment punishment = punishmentManager.getPendingPunishment(player.getUUID());
            CombatLogIncident incident = incidentManager.getIncident(punishment.getIncidentId());
            
            // Handle punishment based on incident status
            boolean shouldKick = punishmentManager.handlePlayerLogin(player, incident);
            
            if (shouldKick) {
                CombatLogReport.LOGGER.warn("Player {} tried to join with pending combat log ticket - kicking",
                    player.getName().getString());
                
                player.connection.disconnect(
                    Component.literal("§c§lCombat Log Ticket Pending\n\n" +
                        "§eYou have an active combat log ticket in Discord.\n" +
                        "§ePlease submit your proof before joining the server.\n\n" +
                        "§7Ticket ID: " + (incident != null ? incident.getId().toString().substring(0, 8) : "N/A"))
                );
            }
        }
    }
}
