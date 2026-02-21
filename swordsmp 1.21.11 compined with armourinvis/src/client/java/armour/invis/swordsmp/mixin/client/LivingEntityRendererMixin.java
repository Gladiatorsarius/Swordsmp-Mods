package armour.invis.swordsmp.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import armour.invis.swordsmp.ArmourInvisData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import armour.invis.swordsmp.accessor.InvisibilityAmplifierAccessor;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
	private void armourInvis$extractInvisibilityAmplifier(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
		if (state instanceof InvisibilityAmplifierAccessor accessor) {
			// Prefer the server-synced tracked value when available
			// Prefer the accessor mixin if present (avoids direct defineId collisions)
			try {
				if (entity instanceof armour.invis.swordsmp.accessor.ArmourInvisEntityAccessor acc) {
					int tracked = acc.armourInvis$getTrackedAmplifier();
					if (tracked >= 1) {
						accessor.armourInvis$setInvisibilityAmplifier(tracked);
						accessor.armourInvis$setHasArmourInvisTag(true);
						return;
					}
				}
			} catch (Throwable ignored) {
			}
			MobEffectInstance invis = entity.getEffect(MobEffects.INVISIBILITY);
			int amplifier = (invis != null) ? invis.getAmplifier() : -1;
			accessor.armourInvis$setInvisibilityAmplifier(amplifier);
		}
	}
}
