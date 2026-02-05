/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class SwordssmpModTabs {
	public static void load() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(tabData -> {
			tabData.accept(SwordssmpModItems.WIND_BLADE);
			tabData.accept(SwordssmpModItems.CHORUS_SLAYER);
			tabData.accept(SwordssmpModItems.THUNDER_SWORD);
			tabData.accept(SwordssmpModItems.PHANTOM_BLADE);
			tabData.accept(SwordssmpModItems.TNT_SWORD);
			tabData.accept(SwordssmpModItems.WARDEN_BLASTER);
			tabData.accept(SwordssmpModItems.DRAGON_SLAYER);
			tabData.accept(SwordssmpModItems.LIFE_STEALER);
			tabData.accept(SwordssmpModItems.EARTH_WAVE_SWORD);
			tabData.accept(SwordssmpModItems.GODS_VIEW);
			tabData.accept(SwordssmpModItems.BREACHER);
			tabData.accept(SwordssmpModItems.GHOST_BLADE);
			tabData.accept(SwordssmpModItems.BERSERK_HAND);
			tabData.accept(SwordssmpModItems.ANCIENT_VOID_RELIC);
			tabData.accept(SwordssmpModItems.AMETHYST_SWORD);
			tabData.accept(SwordssmpModItems.DUAL_BLADE_2);
			tabData.accept(SwordssmpModItems.DUAL_BLADE_1);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tabData -> {
			tabData.accept(SwordssmpModItems.SMELTER_PICKAXE);
			tabData.accept(SwordssmpModItems.LEVITATION_WAND);
			tabData.accept(SwordssmpModItems.TNT_ROD);
			tabData.accept(SwordssmpModItems.CHAOS_CRYSTAL);
			tabData.accept(SwordssmpModItems.TEST_ROD);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(tabData -> {
			tabData.accept(SwordssmpModItems.GRAY_APPLE);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(tabData -> {
			tabData.accept(SwordssmpModItems.REINFORCED_STICK);
			tabData.accept(SwordssmpModItems.CORROSIVE_DUST);
			tabData.accept(SwordssmpModItems.XP_ORB);
			tabData.accept(SwordssmpModItems.SHARPENED_AMETHYST_SHARD);
		});
	}
}
