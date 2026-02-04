package armour.invis.swordsmp.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorVisibilityMixin {
	@Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
	private void armourInvis$hideArmorForInvisTwo(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		MobEffectInstance invis = self.getEffect(MobEffects.INVISIBILITY);
		if (invis != null && invis.getAmplifier() >= 1) {
			cir.setReturnValue(true);
		}
	}
}
