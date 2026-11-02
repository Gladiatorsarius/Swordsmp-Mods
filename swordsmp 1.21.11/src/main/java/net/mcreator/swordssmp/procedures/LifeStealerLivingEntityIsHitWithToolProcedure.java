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
	public static final String KILL_REWARDED_TAG = "swordssmp_lifestealer_kill_rewarded";

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (world instanceof ServerLevel serverLevel && Boolean.TRUE.equals(serverLevel.getGameRules().get(SwordssmpModGameRules.LIFE_STEALER_HARDCORE))) {
			// +2 max health is granted once per kill: the first hit that observes the victim dying
			// (i.e. the killing blow, applied just before this callback) claims it via an atomic tag
			// on the victim. Later hits on the dying victim, by anyone, grant nothing. The tag is
			// stored in the victim's entity NBT, so it survives chunk unload/reload while dying and
			// disappears when the victim is removed; SwordssmpMod clears it on player respawn.
			if (entity instanceof Player && entity instanceof LivingEntity victim && victim.isDeadOrDying() && victim.addTag(KILL_REWARDED_TAG)) {
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
