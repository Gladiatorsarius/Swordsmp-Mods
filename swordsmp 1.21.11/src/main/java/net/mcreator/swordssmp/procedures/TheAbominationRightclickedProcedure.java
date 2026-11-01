package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class TheAbominationRightclickedProcedure {
	public static boolean eventResult = true;

	public TheAbominationRightclickedProcedure() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockentity) -> {
			execute(world, pos.getX(), pos.getY(), pos.getZ(), player);
			boolean result = eventResult;
			eventResult = true;
			return result;
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SwordssmpModItems.THE_ABOMINATION) {
			if (world instanceof ServerLevel _level) {
				ProcedureUtils.runCommand(_level, x, y, z, "/gamemode creative @p");
			}
			SwordssmpMod.queueServerWork(1200, () -> {
				if (world instanceof ServerLevel _level) {
					ProcedureUtils.runCommand(_level, x, y, z, "/gamemode survival @p");
				}
			});
		}
	}
}
