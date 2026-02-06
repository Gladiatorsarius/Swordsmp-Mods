package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketMixin {
    
    @Shadow
    @Nullable
    private LivingEntity attachedToEntity;
    
    private boolean combatCheckDone = false;
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onRocketTick(CallbackInfo ci) {
        // Only check once per rocket
        if (combatCheckDone) {
            return;
        }
        
        combatCheckDone = true;
        
        // Check if rocket is attached to a player (elytra boost)
        if (attachedToEntity instanceof ServerPlayer serverPlayer) {
            CombatManager combatManager = CombatManager.getInstance();
            
            // If player is in combat, cancel the rocket
            if (combatManager.isInCombat(serverPlayer.getUUID())) {
                serverPlayer.displayClientMessage(
                    Component.literal("§c§lCannot use rockets while in combat!"),
                    true // Action bar
                );
                
                // Kill the rocket entity immediately
                FireworkRocketEntity rocket = (FireworkRocketEntity) (Object) this;
                rocket.discard();
                ci.cancel();
            }
        }
    }
}
