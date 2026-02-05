package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatLogReport;
import combat.log.report.swordssmp.CombatManager;
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
        CombatManager manager = CombatManager.getInstance();
        
        if (manager.isInCombat(player.getUUID())) {
            long remainingTime = manager.getRemainingTime(player.getUUID());
            CombatLogReport.LOGGER.warn("Player {} logged out during combat with {} seconds remaining!", 
                player.getName().getString(), remainingTime / 1000.0);
            
            // Broadcast report message to other players
            PlayerList playerList = (PlayerList) (Object) this;
            playerList.broadcastSystemMessage(
                Component.literal("§e[Combat Log Report] §c" + player.getName().getString() + " logged out during combat with " + String.format("%.1f", remainingTime / 1000.0) + " seconds remaining!"), 
                false
            );
            
            manager.removePlayer(player.getUUID());
        }
    }
}
