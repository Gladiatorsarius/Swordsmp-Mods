/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;

public class SwordssmpModGameRules {
	public static GameRule<Boolean> LIFE_STEALER_HARDCORE;

	public static void load() {
		LIFE_STEALER_HARDCORE = GameRuleBuilder.forBoolean(false)
				.category(GameRuleCategory.PLAYER)
				.buildAndRegister(Identifier.fromNamespaceAndPath("minecraft", "life_stealer_hardcore"));
	}
}
