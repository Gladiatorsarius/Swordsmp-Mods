package net.mcreator.swordssmp.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

import net.mcreator.swordssmp.procedures.CorrosiveFireEffectStartedappliedProcedure;

public class CorrosiveFireMobEffect extends MobEffect {
	public CorrosiveFireMobEffect() {
		super(MobEffectCategory.HARMFUL, -10092699);
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		CorrosiveFireEffectStartedappliedProcedure.execute(entity.level(), entity);
	}
}
