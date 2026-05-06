package net.mcreator.swordssmp.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;

public class SwordssmpCustomItemGroups {
	public static final ResourceKey<CreativeModeTab> SWORDSMP_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("swordssmp", "swordsmp_items"));
	public static CreativeModeTab SWORDSMP_TAB_INSTANCE;

	public static void register() {
		SWORDSMP_TAB_INSTANCE = FabricItemGroup.builder()
				.title(Component.literal("Swordsmp Items"))
				.icon(() -> new ItemStack(SwordssmpModItems.WIND_BLADE))
				.displayItems((parameters, output) -> {
					// Swordsmp Items
					output.accept(SwordssmpModItems.WIND_BLADE);
					output.accept(SwordssmpModItems.CHORUS_SLAYER);
					output.accept(SwordssmpModItems.SMELTER_PICKAXE);
					output.accept(SwordssmpModItems.THUNDER_SWORD);
					output.accept(SwordssmpModItems.PHANTOM_BLADE);
					output.accept(SwordssmpModItems.TNT_SWORD);
					output.accept(SwordssmpModItems.WARDEN_BLASTER);
					output.accept(SwordssmpModItems.DRAGON_SLAYER);
					output.accept(SwordssmpModItems.GRAY_APPLE);
					output.accept(SwordssmpModItems.LIFE_STEALER);
					output.accept(SwordssmpModItems.EARTH_WAVE_SWORD);
					output.accept(SwordssmpModItems.LEVITATION_WAND);
					output.accept(SwordssmpModItems.REINFORCED_STICK);
					output.accept(SwordssmpModItems.TNT_ROD);
					output.accept(SwordssmpModItems.GODS_VIEW);
					output.accept(SwordssmpModItems.BREACHER);
					output.accept(SwordssmpModItems.GHOST_BLADE);
					output.accept(SwordssmpModItems.BERSERK_HAND);
					output.accept(SwordssmpModItems.ANCIENT_VOID_RELIC);
					output.accept(SwordssmpModItems.AMETHYST_SWORD);
					output.accept(SwordssmpModItems.CORROSIVE_DUST);
					output.accept(SwordssmpModItems.XP_ORB);
					output.accept(SwordssmpModItems.SHARPENED_AMETHYST_SHARD);
					output.accept(SwordssmpModItems.TEST_ROD);
					output.accept(SwordssmpModItems.SWIFT_BLUE);
					output.accept(SwordssmpModItems.POWERFUL_RED);
					
					// Potions
					ItemStack pvPotion = new ItemStack(Items.POTION);
					pvPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.PV_PPOTION));
					output.accept(pvPotion);
					
					ItemStack pvSplashPotion = new ItemStack(Items.SPLASH_POTION);
					pvSplashPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.PV_PPOTION));
					output.accept(pvSplashPotion);
					
					ItemStack pvLingeringPotion = new ItemStack(Items.LINGERING_POTION);
					pvLingeringPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.PV_PPOTION));
					output.accept(pvLingeringPotion);
					
					ItemStack corrosivePotion = new ItemStack(Items.POTION);
					corrosivePotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.CORROSIVE_FIRE_POTION));
					output.accept(corrosivePotion);
					
					ItemStack corrosiveSplashPotion = new ItemStack(Items.SPLASH_POTION);
					corrosiveSplashPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.CORROSIVE_FIRE_POTION));
					output.accept(corrosiveSplashPotion);
					
					ItemStack corrosiveLingeringPotion = new ItemStack(Items.LINGERING_POTION);
					corrosiveLingeringPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.CORROSIVE_FIRE_POTION));
					output.accept(corrosiveLingeringPotion);
					
					ItemStack xpPotion = new ItemStack(Items.POTION);
					xpPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.INSTANT_XP_POTION));
					output.accept(xpPotion);
					
					ItemStack xpSplashPotion = new ItemStack(Items.SPLASH_POTION);
					xpSplashPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.INSTANT_XP_POTION));
					output.accept(xpSplashPotion);
					
					ItemStack xpLingeringPotion = new ItemStack(Items.LINGERING_POTION);
					xpLingeringPotion.set(DataComponents.POTION_CONTENTS, new PotionContents(SwordssmpModPotions.INSTANT_XP_POTION));
					output.accept(xpLingeringPotion);
				})
				.build();

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SWORDSMP_TAB, SWORDSMP_TAB_INSTANCE);
	}
}
