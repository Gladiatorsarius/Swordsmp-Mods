/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;

import net.mcreator.swordssmp.potion.InstantXPMobEffect;
import net.mcreator.swordssmp.potion.CorrosiveFireMobEffect;
import net.mcreator.swordssmp.SwordssmpMod;

import java.util.function.Supplier;

public class SwordssmpModMobEffects {
	public static Holder<MobEffect> CORROSIVE_FIRE;
	public static Holder<MobEffect> INSTANT_XP;

	public static void load() {
		CORROSIVE_FIRE = register("corrosive_fire", CorrosiveFireMobEffect::new);
		INSTANT_XP = register("instant_xp", InstantXPMobEffect::new);
	}

	private static Holder<MobEffect> register(String registryname, Supplier<MobEffect> element) {
		return Holder.direct(Registry.register(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, registryname), element.get()));
	}
}
