package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.SwordssmpMod;

public class CorrosiveFireEffectStartedappliedProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		damageGear(world, entity);
		SwordssmpMod.queueServerWork(200, () -> damageGear(world, entity));
	}

	private static void damageGear(LevelAccessor world, Entity entity) {
		if (world instanceof ServerLevel level && entity instanceof LivingEntity living) {
			damage(living.getMainHandItem(), level);
			damage(living.getOffhandItem(), level);
			damage(living.getItemBySlot(EquipmentSlot.FEET), level);
			damage(living.getItemBySlot(EquipmentSlot.LEGS), level);
			damage(living.getItemBySlot(EquipmentSlot.CHEST), level);
			damage(living.getItemBySlot(EquipmentSlot.HEAD), level);
		}
	}

	private static void damage(ItemStack stack, ServerLevel level) {
		stack.hurtAndBreak(50, level, null, item -> {
		});
	}
}
