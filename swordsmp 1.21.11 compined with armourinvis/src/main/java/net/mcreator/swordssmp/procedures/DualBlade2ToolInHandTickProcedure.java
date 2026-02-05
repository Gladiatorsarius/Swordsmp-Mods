package net.mcreator.swordssmp.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

import net.mcreator.swordssmp.init.SwordssmpModItems;

public class DualBlade2ToolInHandTickProcedure {
	public static boolean eventResult = true;

	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (SwordssmpModItems.DUAL_BLADE_1 == (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem()) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
				_entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 1, 2, false, false));
				_entity.addEffect(new MobEffectInstance(MobEffects.HASTE, 1, 4, false, false));
			}
		}
	}
}
