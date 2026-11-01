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
			if (trackedEntity instanceof LivingEntity living) {
				MobEffectInstance effect = living.getEffect(MobEffects.INVISIBILITY);
				ServerPlayNetworking.send(player, new InvisibilityAmplifierSyncMessage(living.getId(), effect != null ? effect.getAmplifier() : -1));
			}
		});
	}
}
