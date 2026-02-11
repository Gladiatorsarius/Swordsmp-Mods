package armour.invis.swordsmp.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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
			MobEffectInstance invis = entity.getEffect(MobEffects.INVISIBILITY);
			int amplifier = (invis != null) ? invis.getAmplifier() : -1;
			accessor.armourInvis$setInvisibilityAmplifier(amplifier);
			// Also expose whether the entity has the client-side tag set by its owner
			boolean hasTag = entity.getTags().contains("Armour invis");
			accessor.armourInvis$setHasArmourInvisTag(hasTag);
		}
	}
}
