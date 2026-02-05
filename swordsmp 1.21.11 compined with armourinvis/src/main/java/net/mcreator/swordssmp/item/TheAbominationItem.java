package net.mcreator.swordssmp.item;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;

import net.mcreator.swordssmp.procedures.TheAbominationRightclickedProcedure;

public class TheAbominationItem extends Item {
	public TheAbominationItem(Item.Properties properties) {
		super(properties.stacksTo(99).fireResistant());
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
		return ItemUseAnimation.SPEAR;
	}

	@Override
	public float getDestroySpeed(ItemStack itemstack, BlockState state) {
		return 999f;
	}

	@Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		super.hurtEnemy(itemstack, entity, sourceentity);
		TheAbominationRightclickedProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
	}
}
