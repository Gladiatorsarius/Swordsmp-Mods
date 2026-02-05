/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;

import net.mcreator.swordssmp.SwordssmpMod;

public class SwordssmpModPotions {
	public static Holder<Potion> PV_PPOTION;
	public static Holder<Potion> CORROSIVE_FIRE_POTION;
	public static Holder<Potion> INSTANT_XP_POTION;

	public static void load() {
		PV_PPOTION = register("pv_ppotion",
				new Potion("pv_ppotion", new MobEffectInstance(MobEffects.SPEED, 6000, 1, false, true), new MobEffectInstance(MobEffects.STRENGTH, 6000, 1, false, true), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, true)));
		CORROSIVE_FIRE_POTION = register("corrosive_fire_potion", new Potion("corrosive_fire_potion", new MobEffectInstance(SwordssmpModMobEffects.CORROSIVE_FIRE, 600, 1, false, true)));
		INSTANT_XP_POTION = register("instant_xp_potion", new Potion("instant_xp_potion", new MobEffectInstance(SwordssmpModMobEffects.INSTANT_XP, 200, 1, false, true)));
	}

	private static Holder<Potion> register(String registryname, Potion element) {
		return Holder.direct(Registry.register(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, registryname), element));
	}
}
