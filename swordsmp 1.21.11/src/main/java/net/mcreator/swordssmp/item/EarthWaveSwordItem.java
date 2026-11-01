package net.mcreator.swordssmp.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;

public class EarthWaveSwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = ModToolMaterials.of(BlockTags.INCORRECT_FOR_IRON_TOOL, 450, 2, "earth_wave_sword_repair_items");

	public EarthWaveSwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -2.4f));
	}
}
