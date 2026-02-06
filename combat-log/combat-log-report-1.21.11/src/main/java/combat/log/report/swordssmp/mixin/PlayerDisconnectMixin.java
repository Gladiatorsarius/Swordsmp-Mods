package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatHeadManager;
import combat.log.report.swordssmp.CombatLogGameRules;
import combat.log.report.swordssmp.CombatLogReport;
import combat.log.report.swordssmp.CombatManager;
import combat.log.report.swordssmp.incident.CombatLogIncident;
import combat.log.report.swordssmp.incident.IncidentManager;
import combat.log.report.swordssmp.punishment.PunishmentManager;
import combat.log.report.swordssmp.socket.SocketClient;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

@Mixin(PlayerList.class)
public class PlayerDisconnectMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerDisconnect(ServerPlayer player, CallbackInfo ci) {
        CombatManager combatManager = CombatManager.getInstance();
        
        if (combatManager.isInCombat(player.getUUID())) {
            long remainingTime = combatManager.getRemainingTime(player.getUUID());
            double remainingSeconds = remainingTime / 1000.0;
            
            CombatLogReport.LOGGER.warn("Player {} logged out during combat with {} seconds remaining!", 
                player.getName().getString(), remainingSeconds);
            
            // Check if combat log system is bypassed via gamerule
            ServerLevel serverLevel = (ServerLevel) player.level();
            boolean bypassSystem = Boolean.TRUE.equals(serverLevel.getGameRules().get(CombatLogGameRules.BYPASS_COMBAT_LOG_SYSTEM));
            
            if (bypassSystem) {
                // System bypassed - just clear combat tag and let items drop normally
                CombatLogReport.LOGGER.info("Combat log system bypassed by gamerule, items will drop naturally");
                
                // Broadcast simple message
                PlayerList playerList = (PlayerList) (Object) this;
                playerList.broadcastSystemMessage(
                    Component.literal("§e[Combat Log] §c" + player.getName().getString() + 
                        " logged out during combat with " + String.format("%.1f", remainingSeconds) + 
                        " seconds remaining."), 
                    false
                );
                
                combatManager.removePlayer(player.getUUID());
                return;
            }
            
            // Normal combat log system flow
            // Create incident record
            IncidentManager incidentManager = IncidentManager.getInstance();
            CombatLogIncident incident = incidentManager.createIncident(
                player.getUUID(),
                player.getName().getString(),
                remainingSeconds
            );
            
            // Get combat opponents before clearing combat tag
            Set<UUID> opponents = combatManager.getOpponents(player.getUUID());
            
            // Create player head with inventory
            CombatHeadManager headManager = CombatHeadManager.getInstance();
            headManager.createCombatLogHead(player, incident.getId(), opponents);
            
            // Add pending punishment (ban while ticket pending, kill on denial)
            PunishmentManager punishmentManager = PunishmentManager.getInstance();
            punishmentManager.addPendingPunishment(
                player.getUUID(),
                incident.getId(),
                true,  // shouldBan - yes, ban while ticket pending
                true   // shouldKillOnLogin - yes, kill if ticket denied
            );
            
            // Send incident to Discord bot via WebSocket
            SocketClient socketClient = SocketClient.getInstance();
            socketClient.sendIncident(
                incident.getId(),
                player.getUUID(),
                player.getName().getString(),
                remainingSeconds
            );
            
            // Broadcast report message to other players
            PlayerList playerList = (PlayerList) (Object) this;
            playerList.broadcastSystemMessage(
                Component.literal("§e[Combat Log Report] §c" + player.getName().getString() + 
                    " logged out during combat with " + String.format("%.1f", remainingSeconds) + 
                    " seconds remaining! Ticket will be created in Discord."), 
                false
            );
            
            combatManager.removePlayer(player.getUUID());
        }
    }
}
