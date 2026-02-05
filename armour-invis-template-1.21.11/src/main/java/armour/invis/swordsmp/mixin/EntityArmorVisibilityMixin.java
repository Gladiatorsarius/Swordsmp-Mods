package armour.invis.swordsmp.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityArmorVisibilityMixin {
	@Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
	private void armourInvis$hideArmorForInvisTwo(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;
		if (self instanceof LivingEntity livingEntity) {
			MobEffectInstance invis = livingEntity.getEffect(MobEffects.INVISIBILITY);
			if (invis != null && invis.getAmplifier() >= 1) {
				cir.setReturnValue(true);
			}
		}
	}
}
