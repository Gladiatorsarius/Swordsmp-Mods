package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class GhostBladeItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 6150, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:ghost_blade_repair_items")));

	public GhostBladeItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 10f, -2.8f));
	}
}
