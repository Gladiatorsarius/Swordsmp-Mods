package armour.invis.swordsmp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Set;

public class ArmourInvisClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Each client manages its own tag: if the local player has Invisibility II (amplifier >= 1),
		// add the "Armour invis" tag to the player so other clients can detect it and hide armor.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			try {
				if (client.player != null) {
					MobEffectInstance invis = client.player.getEffect(MobEffects.INVISIBILITY);
					boolean shouldHaveTag = (invis != null && invis.getAmplifier() >= 1);
					Set<String> tags = client.player.getTags();
					if (shouldHaveTag) {
						if (!tags.contains("Armour invis")) client.player.addTag("Armour invis");
					} else {
						if (tags.contains("Armour invis")) client.player.removeTag("Armour invis");
					}
				}
			} catch (Throwable ignored) {
				// Avoid crashing client on unexpected errors
			}
		});
	}
}