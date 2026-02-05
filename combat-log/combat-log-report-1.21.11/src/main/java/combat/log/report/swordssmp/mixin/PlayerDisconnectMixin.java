package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatLogReport;
import combat.log.report.swordssmp.CombatManager;
import combat.log.report.swordssmp.incident.CombatLogIncident;
import combat.log.report.swordssmp.incident.IncidentManager;
import combat.log.report.swordssmp.punishment.PunishmentManager;
import combat.log.report.swordssmp.socket.SocketClient;
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
public class PlayerDisconnectMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerDisconnect(ServerPlayer player, CallbackInfo ci) {
        CombatManager combatManager = CombatManager.getInstance();
        
        if (combatManager.isInCombat(player.getUUID())) {
            long remainingTime = combatManager.getRemainingTime(player.getUUID());
            double remainingSeconds = remainingTime / 1000.0;
            
            CombatLogReport.LOGGER.warn("Player {} logged out during combat with {} seconds remaining!", 
                player.getName().getString(), remainingSeconds);
            
            // Create incident record
            IncidentManager incidentManager = IncidentManager.getInstance();
            CombatLogIncident incident = incidentManager.createIncident(
                player.getUUID(),
                player.getName().getString(),
                remainingSeconds
            );
            
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
