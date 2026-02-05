package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.procedures.AmethystSwordLivingEntityIsHitWithToolProcedure;

public class AmethystSwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4620, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:amethyst_sword_repair_items")));

	public AmethystSwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 9f, -2.4f));
	}

	@Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		super.hurtEnemy(itemstack, entity, sourceentity);
		AmethystSwordLivingEntityIsHitWithToolProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}
}
