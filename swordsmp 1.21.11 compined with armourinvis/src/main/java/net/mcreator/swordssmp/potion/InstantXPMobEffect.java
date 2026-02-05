package net.mcreator.swordssmp.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

import net.mcreator.swordssmp.procedures.InstantXPEffectStartedappliedProcedure;

public class InstantXPMobEffect extends MobEffect {
	public InstantXPMobEffect() {
		super(MobEffectCategory.BENEFICIAL, -16715009);
		this.withSoundOnAdded(BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.experience_orb.pickup")));
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		InstantXPEffectStartedappliedProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}
}
