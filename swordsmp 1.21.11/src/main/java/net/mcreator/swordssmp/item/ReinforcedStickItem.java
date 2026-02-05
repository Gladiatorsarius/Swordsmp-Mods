package net.mcreator.swordssmp.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class ReinforcedStickItem extends Item {
	public ReinforcedStickItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}
