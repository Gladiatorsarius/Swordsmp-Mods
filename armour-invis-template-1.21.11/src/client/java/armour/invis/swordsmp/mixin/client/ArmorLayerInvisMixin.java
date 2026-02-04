package armour.invis.swordsmp.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class ArmorLayerInvisMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void armourInvis$hideArmor(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		MobEffectInstance invis = livingEntity.getEffect(MobEffects.INVISIBILITY);
		if (invis != null && invis.getAmplifier() >= 1) {
			ci.cancel();
		}
	}
}
