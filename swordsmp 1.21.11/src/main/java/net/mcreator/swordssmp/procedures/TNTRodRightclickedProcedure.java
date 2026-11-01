package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

public class TNTRodRightclickedProcedure {

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		// Capture the rod stack now so the delayed durability cost always lands on the
		// rod, not on whatever the player happens to hold when the timer fires.
		ItemStack rodStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (rodStack.getItem() == SwordssmpModItems.TNT_ROD) {
			for (int i = 1; i <= 5; i++) {
				SwordssmpMod.queueServerWork(10 * i, () -> {
					if (world instanceof ServerLevel _level) {
						ProcedureUtils.runCommand(_level, x, y, z, "/execute at @e[type=minecraft:fishing_bobber] run summon tnt ~ ~ ~");
						rodStack.hurtAndBreak(100, _level, null, _stkprov -> {
						});
					}
				});
			}
		}
	}
}
