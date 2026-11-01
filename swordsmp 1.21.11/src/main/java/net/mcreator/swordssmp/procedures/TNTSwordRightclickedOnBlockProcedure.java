package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class TNTSwordRightclickedOnBlockProcedure {
	public static boolean eventResult = true;

	public TNTSwordRightclickedOnBlockProcedure() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (hand == player.getUsedItemHand())
				execute(level, hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ(), player);
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() == SwordssmpModItems.TNT_SWORD) {
			CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
			boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "TNTSword", entity);
			long now = ProcedureUtils.gameTime(world);
			long cooldownUntil = cooldownTag.getLong("TNTSwordCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("TNTSwordCooldownUntil");
				cooldownTag.remove("TNTSwordCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
			}
			// native cooldown guard
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
				return;
			ProcedureUtils.playBlockSound(world, x, y, z, "entity.tnt.primed", SoundSource.NEUTRAL, 50, 1);
			if (world instanceof ServerLevel _level) {
				Entity entityToSpawn = EntityType.TNT_MINECART.spawn(_level, BlockPos.containing(x, y, z), EntitySpawnReason.MOB_SUMMONED);
				if (entityToSpawn != null) {
					entityToSpawn.setDeltaMovement(0, 1.5, 0);
				}
			}
			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "TNTSword", entity, now, 300L);
		}
	}
}
