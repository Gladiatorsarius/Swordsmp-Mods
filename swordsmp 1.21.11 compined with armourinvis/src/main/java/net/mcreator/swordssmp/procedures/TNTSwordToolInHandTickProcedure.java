package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;

import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.swordssmp.init.SwordssmpModItems;

public class TNTSwordToolInHandTickProcedure {
	public static boolean eventResult = true;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SwordssmpModItems.TNT_SWORD) {
			if (world instanceof ServerLevel _level) {
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"execute at @e[type=minecraft:tnt_minecart] as @e[type=minecraft:tnt_minecart] run particle flame ~ ~-1 ~ 0 0 0 0 10 normal");
			}
		}
	}
}
