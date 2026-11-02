package net.mcreator.swordssmp.mixin;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.procedures.LifeStealerLivingEntityIsHitWithToolProcedure;

@Mixin(LivingEntity.class)
public abstract class LifeStealerDyingStateMixin {
	// Life Stealer attribution: record whether the victim was already dying just before this
	// hit's damage lands, so the sword's post-damage callback can tell the killing blow apart
	// from a hit on a victim who was already dying of another cause. HEAD, so it runs even for
	// hits the damage pipeline later rejects; the tag dies with the entity.
	@Inject(method = "hurtServer", at = @At("HEAD"))
	private void recordDyingBeforeHit(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.isDeadOrDying()) {
			self.addTag(LifeStealerLivingEntityIsHitWithToolProcedure.DYING_BEFORE_HIT_TAG);
		} else {
			self.removeTag(LifeStealerLivingEntityIsHitWithToolProcedure.DYING_BEFORE_HIT_TAG);
		}
	}
}
