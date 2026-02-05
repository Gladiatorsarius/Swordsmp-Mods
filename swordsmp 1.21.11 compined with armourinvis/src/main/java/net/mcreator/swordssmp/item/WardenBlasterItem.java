package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class WardenBlasterItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 7900, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:warden_blaster_repair_items")));

	public WardenBlasterItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 8f, -3.1f));
	}
}
