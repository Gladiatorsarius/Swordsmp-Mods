package net.mcreator.swordssmp.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

public class GrayApplePlayerFinishesUsingItemProcedure {
	public static boolean eventResult = true;

	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
			_entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 9, false, true));
			_entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1, false, true));
			_entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, true));
		}
	}
}
