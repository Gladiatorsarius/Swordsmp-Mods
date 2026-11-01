package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;

public class WardenBlasterItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = ModToolMaterials.of(BlockTags.INCORRECT_FOR_IRON_TOOL, 7900, 2, "warden_blaster_repair_items");

	public WardenBlasterItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 8f, -3.1f));
	}
}
