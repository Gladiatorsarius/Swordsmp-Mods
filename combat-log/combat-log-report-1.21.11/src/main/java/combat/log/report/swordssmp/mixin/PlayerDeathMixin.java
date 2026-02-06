package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatLogReport;
import combat.log.report.swordssmp.CombatManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

@Mixin(ServerPlayer.class)
public class PlayerDeathMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        CombatManager manager = CombatManager.getInstance();
        UUID playerId = player.getUUID();
        
        if (manager.isInCombat(playerId)) {
            CombatLogReport.LOGGER.info("Player {} died in combat, clearing combat tags", player.getName().getString());
            
            // Get opponents before clearing
            Set<UUID> opponents = manager.getOpponents(playerId);
            
            // Clear combat tag from dead player
            manager.removePlayer(playerId);
            
            // Clear combat tag from all opponents who were fighting this player
            for (UUID opponentId : opponents) {
                if (manager.isInCombat(opponentId)) {
                    manager.removePlayer(opponentId);
                    CombatLogReport.LOGGER.info("Cleared combat tag from opponent {}", opponentId);
                }
            }
        }
    }
}
