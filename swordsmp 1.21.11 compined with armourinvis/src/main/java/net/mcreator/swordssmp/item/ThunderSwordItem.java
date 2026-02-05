package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class ThunderSwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 5300, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:thunder_sword_repair_items")));

	public ThunderSwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 13f, -3.55f));
	}
}
