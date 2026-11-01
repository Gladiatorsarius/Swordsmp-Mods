package net.mcreator.swordssmp.item;

import net.minecraft.world.item.Item;

public class TestRodItem extends ModFishingRodItem {
	public TestRodItem(Item.Properties properties) {
		super(properties.durability(100).repairable(ModToolMaterials.repairTag("test_rod_repair_items")).enchantable(2));
	}
}
