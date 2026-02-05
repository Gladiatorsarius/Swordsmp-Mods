package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;

import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class TNTRodRightclickedProcedure {
	public static boolean eventResult = true;

	public TNTRodRightclickedProcedure() {
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
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SwordssmpModItems.TNT_ROD) {
			SwordssmpMod.queueServerWork(10, () -> {
				if (world instanceof ServerLevel _level) {
					_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
							"/execute at @e[type=minecraft:fishing_bobber] run summon tnt ~ ~ ~");
					(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(100, _level, null, _stkprov -> {
					});
				}
				SwordssmpMod.queueServerWork(10, () -> {
					if (world instanceof ServerLevel _level) {
						_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
								"/execute at @e[type=minecraft:fishing_bobber] run summon tnt ~ ~ ~");
						(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(100, _level, null, _stkprov -> {
						});
					}
					SwordssmpMod.queueServerWork(10, () -> {
						if (world instanceof ServerLevel _level) {
							_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
									"/execute at @e[type=minecraft:fishing_bobber] run summon tnt ~ ~ ~");
							(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(100, _level, null, _stkprov -> {
							});
						}
						SwordssmpMod.queueServerWork(10, () -> {
							if (world instanceof ServerLevel _level) {
								_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
										"/execute at @e[type=minecraft:fishing_bobber] run summon tnt ~ ~ ~");
								(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(100, _level, null, _stkprov -> {
								});
							}
							SwordssmpMod.queueServerWork(10, () -> {
								if (world instanceof ServerLevel _level) {
									_level.getServer().getCommands().performPrefixedCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
											"/execute at @e[type=minecraft:fishing_bobber] run summon tnt ~ ~ ~");
									(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(100, _level, null, _stkprov -> {
									});
								}
							});
						});
					});
				});
			});
		} else {
			if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SwordssmpModItems.TNT_ROD) {
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, new Vec3(x, y, z).x, new Vec3(x, y, z).y, new Vec3(x, y, z).z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("ui.button.click")), SoundSource.PLAYERS, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("ui.button.click")), SoundSource.PLAYERS, 1, 1, false);
					}
				}
			}
		}
	}
}
