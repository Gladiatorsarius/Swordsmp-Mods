package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;

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
			// Capture the caster's own bobber: the ability rains TNT on YOUR cast,
			// not on every fishing bobber loaded anywhere in the world.
			FishingHook hook = (entity instanceof Player _player) ? _player.fishing : null;
			for (int i = 1; i <= 5; i++) {
				SwordssmpMod.queueServerWork(10 * i, () -> {
					if (world instanceof ServerLevel _level) {
						if (hook != null && hook.isAlive()) {
							ProcedureUtils.runCommand(_level, hook.getX(), hook.getY(), hook.getZ(), "/summon minecraft:tnt ~ ~ ~");
						}
						rodStack.hurtAndBreak(100, _level, (entity instanceof ServerPlayer _sp ? _sp : null), _stkprov -> {
						});
					}
				});
			}
		}
	}
}
