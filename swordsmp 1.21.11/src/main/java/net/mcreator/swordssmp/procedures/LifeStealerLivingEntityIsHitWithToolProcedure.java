package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.init.SwordssmpModGameRules;

public class LifeStealerLivingEntityIsHitWithToolProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (world instanceof ServerLevel serverLevel && Boolean.TRUE.equals(serverLevel.getGameRules().get(SwordssmpModGameRules.LIFE_STEALER_HARDCORE))) {
			if (entity instanceof Player && entity instanceof LivingEntity victim && victim.isDeadOrDying()) {
				if (sourceentity instanceof LivingEntity attacker && attacker.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
					attacker.getAttribute(Attributes.MAX_HEALTH).setBaseValue((attacker.getMaxHealth() + 2));
				}
				ProcedureUtils.playBlockSound(world, x, y, z, "block.beacon.activate", SoundSource.NEUTRAL, 1, 1);
			}
		}
		if (entity instanceof LivingEntity victim && victim.isDeadOrDying()) {
			if (sourceentity instanceof LivingEntity attacker) {
				attacker.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Integer.MAX_VALUE, 1, false, false));
			}
			ProcedureUtils.playBlockSound(world, x, y, z, "block.beacon.activate", SoundSource.NEUTRAL, 1, 1);
		}
	}
}
