package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;

import net.minecraft.world.entity.EntityType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

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
		CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
		CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
		boolean tagChanged = false;
		if (entity instanceof Player player) {
			String ownerId = cooldownTag.getString("ThunderCooldownOwner").orElse("");
			if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
				cooldownTag.remove("ThunderCooldownUntil");
				cooldownTag.remove("ThunderCooldownOwner");
				tagChanged = true;
			}
		}
		long now = 0L;
		if (world instanceof Level _level) {
			now = _level.getGameTime();
		}
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
			mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
		}
		if (world instanceof ServerLevel _level) {
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"/execute at @e[distance=2..10] as @e[distance=2..10] run summon minecraft:lightning_bolt ~ ~ ~");
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"/effect give @e[distance=2..10] slowness 3 10");
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"/effect give @e[distance=2..10] blindness 3 10");
			
			if (entity instanceof LivingEntity _entity) {
				_entity.removeEffect(MobEffects.SLOWNESS);
				_entity.removeEffect(MobEffects.BLINDNESS);
			}
			
			// Set per-item cooldown (stored on the stack)
			cooldownTag.putLong("ThunderCooldownUntil", now + 200L);
			if (entity instanceof Player player) {
				cooldownTag.putString("ThunderCooldownOwner", player.getStringUUID());
			}
			mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));

			if (entity instanceof Player _player && !_player.level().isClientSide()) {
				_player.getCooldowns().addCooldown(mainHandStack, 200);
			}
			
			eventResult = false; // Consume the event
		}
	}
}
