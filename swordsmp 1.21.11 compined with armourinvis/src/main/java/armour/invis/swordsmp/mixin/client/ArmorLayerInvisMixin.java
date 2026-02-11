package armour.invis.swordsmp.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import armour.invis.swordsmp.accessor.InvisibilityAmplifierAccessor;

@Mixin(HumanoidArmorLayer.class)
public class ArmorLayerInvisMixin {
	@Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"), cancellable = true)
	private void armourInvis$hideArmor(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, HumanoidRenderState renderState, float limbSwing, float limbSwingAmount, CallbackInfo ci) {
		// Prefer the render-state accessor when available
		if (renderState instanceof InvisibilityAmplifierAccessor accessor) {
			if (accessor.armourInvis$getInvisibilityAmplifier() >= 1) {
				ci.cancel();
				return;
			}
			// If the entity was tagged by its client (has owner-applied tag), hide armor as well
			if (accessor.armourInvis$getHasArmourInvisTag()) {
				ci.cancel();
				return;
			}
		}

		// Fallback: try to obtain the entity from the render state and check its invisibility effect
		try {
			java.lang.reflect.Method m = renderState.getClass().getMethod("getEntity");
			Object maybeEntity = m.invoke(renderState);
			if (maybeEntity instanceof LivingEntity le) {
				// Prefer checking entity tags first (set by the entity's client)
				try {
					java.lang.reflect.Method tagsMethod = le.getClass().getMethod("getTags");
					Object tagsObj = tagsMethod.invoke(le);
					if (tagsObj instanceof java.util.Set<?> tags && tags.contains("Armour invis")) {
						ci.cancel();
						return;
					}
				} catch (NoSuchMethodException ignored) {
					// fall back to effect check below
				}
				MobEffectInstance invis = le.getEffect(MobEffects.INVISIBILITY);
				if (invis != null && invis.getAmplifier() >= 1) {
					ci.cancel();
				}
			}
		} catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ignored) {
			// If reflection fails, do nothing to avoid crashes
		}
	}
}
