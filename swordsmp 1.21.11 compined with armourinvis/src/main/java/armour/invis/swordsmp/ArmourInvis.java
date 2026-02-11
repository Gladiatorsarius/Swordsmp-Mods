package armour.invis.swordsmp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmourInvis implements ModInitializer {
	public static final String MOD_ID = "armour-invis";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Armourinvis (server-side tag sync)");

		// Ensure the server keeps a synced "Armour invis" tag on players who have
		// Invisibility II (amplifier >= 1). This tag is visible to all clients so
		// client-side rendering mixins can detect and hide armour for those players.
		ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
			try {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					MobEffectInstance invis = player.getEffect(MobEffects.INVISIBILITY);
					boolean shouldHaveTag = (invis != null && invis.getAmplifier() >= 1);
					if (shouldHaveTag) {
						if (!player.getTags().contains("Armour invis")) player.addTag("Armour invis");
					} else {
						if (player.getTags().contains("Armour invis")) player.removeTag("Armour invis");
					}
				}
			} catch (Throwable ignored) {
				// Avoid crashing the server on unexpected errors
			}
		});
	}
}