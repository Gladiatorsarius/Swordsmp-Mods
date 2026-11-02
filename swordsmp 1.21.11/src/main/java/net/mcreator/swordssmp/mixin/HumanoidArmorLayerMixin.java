package net.mcreator.swordssmp.mixin;

import net.mcreator.swordssmp.SwordsmpInvisibilityState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
    @Inject(at = @At("HEAD"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", cancellable = true)
    private void swordsmp$hideArmorOnInvisibility(PoseStack poseStack, SubmitNodeCollector bufferSource, int packedLight, HumanoidRenderState state, float limbSwing, float limbSwingAmount, CallbackInfo ci) {
        if (state instanceof SwordsmpInvisibilityState invisibilityState && invisibilityState.swordsmp_getInvisibilityAmplifier() >= 1) {
            ci.cancel();
        }
    }
}
