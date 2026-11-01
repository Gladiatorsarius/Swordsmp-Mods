/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.swordssmp.init;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

import net.mcreator.swordssmp.SwordssmpMod;

public class SwordssmpModGameRules {
	public static GameRule<Boolean> LIFE_STEALER_HARDCORE;
	// Id used up to mod version 1.0.7. Registered only so worlds saved by older
	// versions still load their stored value; migrated to the swordssmp: id on
	// world load and then reset. Do not read this in gameplay code.
	@Deprecated
	public static GameRule<Boolean> LIFE_STEALER_HARDCORE_LEGACY;

	public static void load() {
		LIFE_STEALER_HARDCORE = GameRuleBuilder.forBoolean(false)
				.category(GameRuleCategory.PLAYER)
				.buildAndRegister(Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, "life_stealer_hardcore"));
		LIFE_STEALER_HARDCORE_LEGACY = GameRuleBuilder.forBoolean(false)
				.category(GameRuleCategory.PLAYER)
				.buildAndRegister(Identifier.fromNamespaceAndPath("minecraft", "life_stealer_hardcore"));
		ServerWorldEvents.LOAD.register((server, world) -> {
			GameRules rules = world.getGameRules();
			if (Boolean.TRUE.equals(rules.get(LIFE_STEALER_HARDCORE_LEGACY))) {
				if (!Boolean.TRUE.equals(rules.get(LIFE_STEALER_HARDCORE))) {
					rules.set(LIFE_STEALER_HARDCORE, true, server);
					SwordssmpMod.LOGGER.info("Migrated life_stealer_hardcore value from the legacy minecraft:-namespaced gamerule");
				}
				rules.set(LIFE_STEALER_HARDCORE_LEGACY, false, server);
			}
		});
	}
}
