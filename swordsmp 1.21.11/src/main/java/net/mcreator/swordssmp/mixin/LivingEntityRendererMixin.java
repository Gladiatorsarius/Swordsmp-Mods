package net.mcreator.swordssmp.mixin;

import net.mcreator.swordssmp.SwordsmpInvisibilityState;
import net.mcreator.swordssmp.network.InvisibilityAmplifierClientCache;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
	private void swordsmp$extractInvisibilityState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
		// The client only holds effect instances for the local player; other
		// entities' amplifiers arrive via InvisibilityAmplifierSyncMessage.
		MobEffectInstance effect = entity.getEffect(MobEffects.INVISIBILITY);
		int amplifier = effect != null ? effect.getAmplifier() : InvisibilityAmplifierClientCache.get(entity.getId());
		((SwordsmpInvisibilityState) state).swordsmp_setInvisibilityAmplifier(amplifier);
	}
}
