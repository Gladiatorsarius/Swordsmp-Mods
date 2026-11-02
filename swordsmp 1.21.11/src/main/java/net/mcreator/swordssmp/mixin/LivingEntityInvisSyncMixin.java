package net.mcreator.swordssmp.mixin;

import net.mcreator.swordssmp.network.InvisibilityAmplifierSyncMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Collection;

/**
 * Vanilla sends mob-effect packets only to the affected player (and passengers),
 * never to tracking clients. Push the Invisibility amplifier to everyone
 * tracking this entity whenever it changes, so their clients can hide armor.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityInvisSyncMixin {
	@Inject(method = "onEffectAdded", at = @At("RETURN"))
	private void swordsmp$syncInvisOnAdd(MobEffectInstance effect, Entity source, CallbackInfo ci) {
		swordsmp$syncInvisibilityAmplifier(effect);
	}

	@Inject(method = "onEffectUpdated", at = @At("RETURN"))
	private void swordsmp$syncInvisOnUpdate(MobEffectInstance effect, boolean reapply, Entity source, CallbackInfo ci) {
		swordsmp$syncInvisibilityAmplifier(effect);
	}

	@Inject(method = "onEffectsRemoved", at = @At("RETURN"))
	private void swordsmp$syncInvisOnRemove(Collection<MobEffectInstance> effects, CallbackInfo ci) {
		for (MobEffectInstance effect : effects) {
			if (effect.is(MobEffects.INVISIBILITY)) {
				swordsmp$syncInvisibilityAmplifier(effect);
				return;
			}
		}
	}

	@Unique
	private void swordsmp$syncInvisibilityAmplifier(MobEffectInstance changedEffect) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!changedEffect.is(MobEffects.INVISIBILITY) || !(self.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		MobEffectInstance current = self.getEffect(MobEffects.INVISIBILITY);
		int amplifier = current != null ? current.getAmplifier() : -1;
		serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(self, ServerPlayNetworking.createS2CPacket(new InvisibilityAmplifierSyncMessage(self.getId(), amplifier)));
	}
}
