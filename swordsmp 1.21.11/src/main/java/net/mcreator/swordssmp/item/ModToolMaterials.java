package net.mcreator.swordssmp.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

final class ModToolMaterials {
	private ModToolMaterials() {
	}

	static TagKey<Item> repairTag(String name) {
		return TagKey.create(Registries.ITEM, Identifier.parse("swordssmp:" + name));
	}

	static ToolMaterial of(TagKey<Block> incorrectFor, int durability, int enchantability, String repairTagName) {
		return of(incorrectFor, durability, 4f, enchantability, repairTagName);
	}

	static ToolMaterial of(TagKey<Block> incorrectFor, int durability, float speed, int enchantability, String repairTagName) {
		return new ToolMaterial(incorrectFor, durability, speed, 0, enchantability, repairTag(repairTagName));
	}
}
