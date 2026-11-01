package net.mcreator.swordssmp.network;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers the Invisibility-amplifier sync channel. Effect changes are pushed
 * by LivingEntityInvisSyncMixin; this handles late trackers (join, dimension
 * change, coming into range) so they receive the current value up front.
 */
public final class InvisibilityAmplifierSync {
	private InvisibilityAmplifierSync() {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(InvisibilityAmplifierSyncMessage.TYPE, InvisibilityAmplifierSyncMessage.STREAM_CODEC);
		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
			// Only send when there is something to say: no -1 spam for every
			// tracked mob. Stale client entries are dropped on entity unload
			// (ClientEntityEvents.ENTITY_UNLOAD), so "absent means -1" holds.
			if (trackedEntity instanceof LivingEntity living) {
				MobEffectInstance effect = living.getEffect(MobEffects.INVISIBILITY);
				if (effect != null && ServerPlayNetworking.canSend(player, InvisibilityAmplifierSyncMessage.TYPE)) {
					ServerPlayNetworking.send(player, new InvisibilityAmplifierSyncMessage(living.getId(), effect.getAmplifier()));
				}
			}
		});
	}
}
