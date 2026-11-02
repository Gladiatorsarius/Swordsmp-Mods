package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public final class ProcedureUtils {
	private ProcedureUtils() {
	}

	// Mirrors Level#playSound(Player, BlockPos, ...) on the server and playLocalSound on the client.
	public static void playBlockSound(LevelAccessor world, double x, double y, double z, String soundId, SoundSource source, float volume, float pitch) {
		if (world instanceof Level level) {
			if (!level.isClientSide()) {
				level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(soundId)), source, volume, pitch);
			} else {
				level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(soundId)), source, volume, pitch, false);
			}
		}
	}

	// Mirrors Level#playSound(Player, double, double, double, ...) on the server and playLocalSound on the client.
	public static void playSound(LevelAccessor world, double x, double y, double z, String soundId, SoundSource source, float volume, float pitch) {
		if (world instanceof Level level) {
			if (!level.isClientSide()) {
				level.playSound(null, x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(soundId)), source, volume, pitch);
			} else {
				level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(soundId)), source, volume, pitch, false);
			}
		}
	}

	public static void runCommand(ServerLevel level, double x, double y, double z, String command) {
		level.getServer().getCommands().performPrefixedCommand(
				new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), level.getServer(), null).withSuppressedOutput(), command);
	}

	public static CompoundTag getCustomTag(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		return customData != null ? customData.copyTag() : new CompoundTag();
	}

	public static void saveCustomTag(ItemStack stack, CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static long gameTime(LevelAccessor world) {
		return world instanceof Level level ? level.getGameTime() : 0L;
	}

	// Removes the cooldown keys when someone other than the cooldown owner holds the item. Returns true when the tag changed.
	public static boolean resetCooldownIfOwnerChanged(CompoundTag tag, String prefix, Entity entity) {
		if (entity instanceof Player player) {
			String ownerId = tag.getString(prefix + "CooldownOwner").orElse("");
			if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
				tag.remove(prefix + "CooldownUntil");
				tag.remove(prefix + "CooldownOwner");
				return true;
			}
		}
		return false;
	}

	// Sets the per-item cooldown tag and the native cooldown overlay.
	public static void startCooldown(ItemStack stack, CompoundTag tag, String prefix, Entity entity, long now, long ticks) {
		tag.putLong(prefix + "CooldownUntil", now + ticks);
		if (entity instanceof Player player) {
			tag.putString(prefix + "CooldownOwner", player.getStringUUID());
			if (!player.level().isClientSide()) {
				player.getCooldowns().addCooldown(stack, (int) ticks);
			}
		}
		saveCustomTag(stack, tag);
	}
}
