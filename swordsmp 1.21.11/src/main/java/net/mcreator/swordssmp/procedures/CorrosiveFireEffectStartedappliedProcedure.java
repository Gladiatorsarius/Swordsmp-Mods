package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.mcreator.swordssmp.SwordssmpMod;

public class CorrosiveFireEffectStartedappliedProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		damageGear(world, entity);
		// Server-side only: queued work never drains on a physical client.
		if (world instanceof ServerLevel) {
			SwordssmpMod.queueServerWork(200, () -> damageGear(world, entity));
		}
	}

	private static void damageGear(LevelAccessor world, Entity entity) {
		if (world instanceof ServerLevel level && entity instanceof LivingEntity living) {
			damage(living.getMainHandItem(), level, living);
			damage(living.getOffhandItem(), level, living);
			damage(living.getItemBySlot(EquipmentSlot.FEET), level, living);
			damage(living.getItemBySlot(EquipmentSlot.LEGS), level, living);
			damage(living.getItemBySlot(EquipmentSlot.CHEST), level, living);
			damage(living.getItemBySlot(EquipmentSlot.HEAD), level, living);
		}
	}

	private static void damage(ItemStack stack, ServerLevel level, LivingEntity owner) {
		// Pass the wearer so vanilla applies its Creative exemption (same class of fix as
		// the TNT Rod's creative durability): corrosive fire must not eat Creative gear.
		// Only players can be Creative; non-players keep the old no-owner behavior.
		stack.hurtAndBreak(50, level, (owner instanceof ServerPlayer _sp ? _sp : null), item -> {
		});
	}
}
