package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class WardenBlasterRightclickedProcedure {
	public static boolean eventResult = true;

	public WardenBlasterRightclickedProcedure() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (hand == player.getUsedItemHand())
				execute(level, player.getX(), player.getY(), player.getZ(), player);
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() == SwordssmpModItems.WARDEN_BLASTER) {
			CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
			boolean tagChanged = false;
			if (entity instanceof Player player) {
				String ownerId = cooldownTag.getString("WardenBlasterCooldownOwner").orElse("");
				if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
					cooldownTag.remove("WardenBlasterCooldownUntil");
					cooldownTag.remove("WardenBlasterCooldownOwner");
					tagChanged = true;
				}
			}
			long now = 0L;
			if (world instanceof Level _level) {
				now = _level.getGameTime();
			}
			long cooldownUntil = cooldownTag.getLong("WardenBlasterCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("WardenBlasterCooldownUntil");
				cooldownTag.remove("WardenBlasterCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			// native cooldown guard
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
				return;
			if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_charge")), SoundSource.PLAYERS, 3, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_charge")), SoundSource.PLAYERS, 3, 1, false);
					}
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_boom")), SoundSource.PLAYERS, 3, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_boom")), SoundSource.PLAYERS, 3, 1, false);
					}
				}
				if (world instanceof ServerLevel _level) {
					var commandSource = _level.getServer().createCommandSourceStack().withSuppressedOutput();
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^1 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:4,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^2 {Radius:1,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:4,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^3 {Radius:1,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:4,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^4 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:3,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^5 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:3,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^6 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:3,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^7 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:3,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^8 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:3,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^9 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:3,duration:2,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run summon area_effect_cloud ^ ^1 ^10 {Radius:1f,Duration:20,potion_contents:{custom_effects:[{id:\"minecraft:instant_damage\",amplifier:3,duration:1,show_particles:0b}]}}");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^1 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^2 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^3 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^4 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^5 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^6 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^7 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^8 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^9 0 0 0 1 0 normal");
					_level.getServer().getCommands().performPrefixedCommand(commandSource,
							"/execute at @p run particle sonic_boom ^ ^1 ^10 0 0 0 1 0 normal");
				}
				// Add native cooldown overlay
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getMainHandItem(), 600);
				}

				cooldownTag.putLong("WardenBlasterCooldownUntil", now + 600L);
				if (entity instanceof Player player) {
					cooldownTag.putString("WardenBlasterCooldownOwner", player.getStringUUID());
				}
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			}
		}
