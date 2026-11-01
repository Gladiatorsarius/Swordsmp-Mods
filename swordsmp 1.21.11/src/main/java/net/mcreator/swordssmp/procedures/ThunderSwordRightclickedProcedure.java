package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class ThunderSwordRightclickedProcedure {
	public static boolean eventResult = true;

	public ThunderSwordRightclickedProcedure() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (hand == player.getUsedItemHand()) {
				execute(level, player.getX(), player.getY(), player.getZ(), player);
			}
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() != SwordssmpModItems.THUNDER_SWORD) {
			return;
		}
		CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
		boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "Thunder", entity);
		long now = ProcedureUtils.gameTime(world);
		long cooldownUntil = cooldownTag.getLong("ThunderCooldownUntil").orElse(0L);
		if (cooldownUntil > 0L) {
			if (now < cooldownUntil) {
				return;
			}
			cooldownTag.remove("ThunderCooldownUntil");
			cooldownTag.remove("ThunderCooldownOwner");
			tagChanged = true;
		}
		if (tagChanged) {
			ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
		}
		if (world instanceof ServerLevel _level) {
			ProcedureUtils.runCommand(_level, x, y, z, "/execute as @e[distance=2..10,limit=100] at @s run summon minecraft:lightning_bolt ~ ~ ~");
			ProcedureUtils.runCommand(_level, x, y, z, "/effect give @e[distance=2..10,limit=100] slowness 3 10");
			ProcedureUtils.runCommand(_level, x, y, z, "/effect give @e[distance=2..10,limit=100] blindness 3 10");

			if (entity instanceof LivingEntity _entity) {
				_entity.removeEffect(MobEffects.SLOWNESS);
				_entity.removeEffect(MobEffects.BLINDNESS);
			}

			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "Thunder", entity, now, 200L);

			eventResult = false; // Consume the event
		}
	}
}
