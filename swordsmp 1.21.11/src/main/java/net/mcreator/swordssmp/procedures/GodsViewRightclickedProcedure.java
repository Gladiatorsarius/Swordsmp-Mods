package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

import net.mcreator.swordssmp.init.SwordssmpModItems;

public class GodsViewRightclickedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(entity instanceof Player player))
			return;
		ItemStack mainHandStack = player.getMainHandItem();
		if (mainHandStack.getItem() != SwordssmpModItems.GODS_VIEW)
			return;
		CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
		boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "GodsView", player);
		long now = ProcedureUtils.gameTime(world);
		long cooldownUntil = cooldownTag.getLong("GodsViewCooldownUntil").orElse(0L);
		if (cooldownUntil > 0L) {
			if (now < cooldownUntil) {
				return;
			}
			cooldownTag.remove("GodsViewCooldownUntil");
			cooldownTag.remove("GodsViewCooldownOwner");
			tagChanged = true;
		}
		if (tagChanged) {
			ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
		}
		// block if native cooldown overlay is active
		if (player.getCooldowns().isOnCooldown(mainHandStack))
			return;

		// perform ability (no per-player variable gating)
		if (world instanceof ServerLevel _level) {
			ProcedureUtils.runCommand(_level, x, y, z, "/effect give @a[distance=0..80] minecraft:glowing 5 255");
		}
		ProcedureUtils.playBlockSound(world, x, y, z, "block.beacon.activate", SoundSource.PLAYERS, 3, 1);
		if (world instanceof ServerLevel _level) {
			LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level, EntitySpawnReason.TRIGGERED);
			entityToSpawn.snapTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
			entityToSpawn.setVisualOnly(true);
			_level.addFreshEntity(entityToSpawn);
		}

		ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "GodsView", player, now, 300L);
	}
}
