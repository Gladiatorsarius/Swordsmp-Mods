package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class EarthWaveSwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 450, 4f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:earth_wave_sword_repair_items")));

	public EarthWaveSwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 8f, -2.4f));
	}
}
