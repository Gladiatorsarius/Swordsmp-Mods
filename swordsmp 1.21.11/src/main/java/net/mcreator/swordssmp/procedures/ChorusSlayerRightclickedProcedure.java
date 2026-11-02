package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import net.mcreator.swordssmp.init.SwordssmpModItems;


public class ChorusSlayerRightclickedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() == SwordssmpModItems.CHORUS_SLAYER) {
			// Only allow use when the native cooldown bar is empty
			if (entity instanceof Player _player) {
				if (_player.getCooldowns().isOnCooldown(mainHandStack))
					return;
			}
			CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
			boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "ChorusSlayer", entity);
			long now = ProcedureUtils.gameTime(world);
			long cooldownUntil = cooldownTag.getLong("ChorusSlayerCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("ChorusSlayerCooldownUntil");
				cooldownTag.remove("ChorusSlayerCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
			}
			// Ability is ready - execute (native cooldown ensures single-use until expiry)
			entity.push((entity.getLookAngle().x * 10), 0.2, (entity.getLookAngle().z * 10));
			ProcedureUtils.playBlockSound(world, x, y, z, "item.chorus_fruit.teleport", SoundSource.PLAYERS, 3, 40);
			if (world instanceof ServerLevel _level) {
				_level.sendParticles(ParticleTypes.END_ROD, x, y, z, 80, 3, 0, 3, 5);
			}
			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "ChorusSlayer", entity, now, 1200L);
		}
	}
}
