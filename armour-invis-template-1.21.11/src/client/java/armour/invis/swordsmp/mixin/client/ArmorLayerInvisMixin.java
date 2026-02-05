package armour.invis.swordsmp.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import armour.invis.swordsmp.accessor.InvisibilityAmplifierAccessor;

@Mixin(HumanoidArmorLayer.class)
public class ArmorLayerInvisMixin {
	@Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"), cancellable = true)
	private void armourInvis$hideArmor(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, HumanoidRenderState renderState, float limbSwing, float limbSwingAmount, CallbackInfo ci) {
		if (renderState instanceof InvisibilityAmplifierAccessor accessor) {
			if (accessor.armourInvis$getInvisibilityAmplifier() >= 1) {
				ci.cancel();
			}
		}
	}
}
