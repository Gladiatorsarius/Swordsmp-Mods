/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import net.mcreator.swordssmp.item.*;
import net.mcreator.swordssmp.SwordssmpMod;

import java.util.function.Function;

public class SwordssmpModItems {
	public static Item WIND_BLADE;
	public static Item CHORUS_SLAYER;
	public static Item SMELTER_PICKAXE;
	public static Item THUNDER_SWORD;
	public static Item PHANTOM_BLADE;
	public static Item TNT_SWORD;
	public static Item WARDEN_BLASTER;
	public static Item DRAGON_SLAYER;
	public static Item GRAY_APPLE;
	public static Item LIFE_STEALER;
	public static Item EARTH_WAVE_SWORD;
	public static Item LEVITATION_WAND;
	public static Item REINFORCED_STICK;
	public static Item TNT_ROD;
	public static Item GODS_VIEW;
	public static Item BREACHER;
	public static Item GHOST_BLADE;
	public static Item BERSERK_HAND;
	public static Item ANCIENT_VOID_RELIC;
	public static Item AMETHYST_SWORD;
	public static Item CORROSIVE_DUST;
	public static Item XP_ORB;
	public static Item SHARPENED_AMETHYST_SHARD;
	public static Item CHAOS_CRYSTAL;
	public static Item TEST_ROD;
	public static Item THE_ABOMINATION;
	public static Item DUAL_BLADE_2;
	public static Item DUAL_BLADE_1;

	public static void load() {
		WIND_BLADE = register("wind_blade", WindBladeItem::new);
		CHORUS_SLAYER = register("chorus_slayer", ChorusSlayerItem::new);
		SMELTER_PICKAXE = register("smelter_pickaxe", SmelterPickaxeItem::new);
		THUNDER_SWORD = register("thunder_sword", ThunderSwordItem::new);
		PHANTOM_BLADE = register("phantom_blade", PhantomBladeItem::new);
		TNT_SWORD = register("tnt_sword", TNTSwordItem::new);
		WARDEN_BLASTER = register("warden_blaster", WardenBlasterItem::new);
		DRAGON_SLAYER = register("dragon_slayer", DragonSlayerItem::new);
		GRAY_APPLE = register("gray_apple", GrayAppleItem::new);
		LIFE_STEALER = register("life_stealer", LifeStealerItem::new);
		EARTH_WAVE_SWORD = register("earth_wave_sword", EarthWaveSwordItem::new);
		LEVITATION_WAND = register("levitation_wand", LevitationWandItem::new);
		REINFORCED_STICK = register("reinforced_stick", ReinforcedStickItem::new);
		TNT_ROD = register("tnt_rod", TNTRodItem::new);
		GODS_VIEW = register("gods_view", GodsViewItem::new);
		BREACHER = register("breacher", BreacherItem::new);
		GHOST_BLADE = register("ghost_blade", GhostBladeItem::new);
		BERSERK_HAND = register("berserk_hand", BerserkHandItem::new);
		ANCIENT_VOID_RELIC = register("ancient_void_relic", AncientVoidRelicItem::new);
		AMETHYST_SWORD = register("amethyst_sword", AmethystSwordItem::new);
		CORROSIVE_DUST = register("corrosive_dust", CorrosiveDustItem::new);
		XP_ORB = register("xp_orb", XPOrbItem::new);
		SHARPENED_AMETHYST_SHARD = register("sharpened_amethyst_shard", SharpenedAmethystShardItem::new);
		CHAOS_CRYSTAL = register("chaos_crystal", ChaosCrystalItem::new);
		TEST_ROD = register("test_rod", TestRodItem::new);
		THE_ABOMINATION = register("the_abomination", TheAbominationItem::new);
		DUAL_BLADE_2 = register("dual_blade_2", DualBlade2Item::new);
		DUAL_BLADE_1 = register("dual_blade_1", DualBlade1Item::new);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> I register(String name, Function<Item.Properties, ? extends I> supplier) {
		return (I) Items.registerItem(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, name)), (Function<Item.Properties, Item>) supplier);
	}
}
