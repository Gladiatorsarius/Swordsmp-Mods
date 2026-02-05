/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;

public class SwordssmpModBrewingRecipes {
	public static void load() {
		FabricBrewingRecipeRegistryBuilder.BUILD.register((builder) -> {
			builder.registerPotionRecipe(Potions.WATER, Ingredient.of(Items.NETHER_STAR), SwordssmpModPotions.PV_PPOTION);
			builder.registerPotionRecipe(Potions.WATER, Ingredient.of(SwordssmpModItems.CORROSIVE_DUST), SwordssmpModPotions.CORROSIVE_FIRE_POTION);
			builder.registerPotionRecipe(Potions.WATER, Ingredient.of(SwordssmpModItems.XP_ORB), SwordssmpModPotions.INSTANT_XP_POTION);
		});
	}
}
