package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;

public class SmelterPickaxeItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = ModToolMaterials.of(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 15000, 15f, 208, "smelter_pickaxe_repair_items");

	public SmelterPickaxeItem(Item.Properties properties) {
		super(properties.pickaxe(TOOL_MATERIAL, 2f, -1f));
	}
}
