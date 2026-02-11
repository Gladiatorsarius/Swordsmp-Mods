package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import net.mcreator.swordssmp.init.SwordssmpModGameRules;

public class LifeStealerLivingEntityIsHitWithToolProcedure {
	public static boolean eventResult = true;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (world instanceof ServerLevel _serverLevelGR0 && Boolean.TRUE.equals(_serverLevelGR0.getGameRules().get(SwordssmpModGameRules.LIFE_STEALER_HARDCORE))) {
			if (entity instanceof Player) {
				if (entity instanceof LivingEntity _livEnt && _livEnt.isDeadOrDying()) {
					if (sourceentity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
						_livingEntity4.getAttribute(Attributes.MAX_HEALTH).setBaseValue((_livingEntity4.getMaxHealth() + 2));
					}
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, 1);
						} else {
							_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, 1, false);
						}
					}
				}
			}
		} else {
			if (entity instanceof LivingEntity _livEnt && _livEnt.isDeadOrDying()) {
				if (sourceentity instanceof LivingEntity _livingEntity) {
					_livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Integer.MAX_VALUE, 1, false, false));
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.beacon.activate")), SoundSource.NEUTRAL, 1, 1, false);
					}
				}
			}
		}
	}
}
