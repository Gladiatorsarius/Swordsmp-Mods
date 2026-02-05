package net.mcreator.swordssmp.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

import net.mcreator.swordssmp.procedures.GrayApplePlayerFinishesUsingItemProcedure;

public class GrayAppleItem extends Item {
	public GrayAppleItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).food((new FoodProperties.Builder()).nutrition(8).saturationModifier(1f).alwaysEdible().build(), Consumables.defaultFood().consumeSeconds(0.05F).build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		GrayApplePlayerFinishesUsingItemProcedure.execute(entity);
		return retval;
	}
}
