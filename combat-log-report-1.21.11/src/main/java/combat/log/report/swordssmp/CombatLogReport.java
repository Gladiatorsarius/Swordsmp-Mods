package combat.log.report.swordssmp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombatLogReport implements ModInitializer {
	public static final String MOD_ID = "combat-log-report";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Combat Log Report mod initialized!");
		LOGGER.info("Combat logging tracking is now active");
		LOGGER.info("Players who disconnect during combat will be reported in chat");
		
		// Register damage event to track combat
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			// Check if the entity being damaged is a player
			if (entity instanceof ServerPlayer victim) {
				// Check if the damage source is from another player
				if (source.getEntity() instanceof ServerPlayer attacker) {
					// Tag both players in combat
					CombatManager.getInstance().tagPlayer(attacker);
					CombatManager.getInstance().tagPlayer(victim);
				}
			}
			return true; // Allow the damage to proceed
		});
	}
}