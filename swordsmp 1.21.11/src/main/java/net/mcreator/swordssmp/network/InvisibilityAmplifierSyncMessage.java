package net.mcreator.swordssmp.network;

import net.mcreator.swordssmp.SwordssmpMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Server -> tracker clients: current Invisibility amplifier of a living entity.
 * Vanilla only sends mob-effect packets to the affected player (and passengers),
 * so observing clients cannot read other entities' amplifiers from
 * LivingEntity#getEffect. Amplifier -1 means "no Invisibility effect".
 */
public record InvisibilityAmplifierSyncMessage(int entityId, int amplifier) implements CustomPacketPayload {
	public static final Type<InvisibilityAmplifierSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, "invisibility_amplifier_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, InvisibilityAmplifierSyncMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, InvisibilityAmplifierSyncMessage message) -> {
		buffer.writeVarInt(message.entityId);
		buffer.writeVarInt(message.amplifier);
	}, (RegistryFriendlyByteBuf buffer) -> new InvisibilityAmplifierSyncMessage(buffer.readVarInt(), buffer.readVarInt()));

	@Override
	public Type<InvisibilityAmplifierSyncMessage> type() {
		return TYPE;
	}

	public static void handleData(final InvisibilityAmplifierSyncMessage message, final ClientPlayNetworking.Context context) {
		context.client().execute(() -> InvisibilityAmplifierClientCache.set(message.entityId, message.amplifier));
	}
}
