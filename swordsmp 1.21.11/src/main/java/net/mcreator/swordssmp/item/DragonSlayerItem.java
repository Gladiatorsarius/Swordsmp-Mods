package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.BlockTags;

import net.mcreator.swordssmp.procedures.DragonSlayerLivingEntityIsHitWithToolProcedure;

public class DragonSlayerItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = ModToolMaterials.of(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 12500, 2, "dragon_slayer_repair_items");

	public DragonSlayerItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -2.4f));
	}

	@Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		super.hurtEnemy(itemstack, entity, sourceentity);
		DragonSlayerLivingEntityIsHitWithToolProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}
}
