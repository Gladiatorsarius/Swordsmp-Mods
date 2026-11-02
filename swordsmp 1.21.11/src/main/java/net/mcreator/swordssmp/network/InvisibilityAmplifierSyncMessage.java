package net.mcreator.swordssmp.network;

import net.mcreator.swordssmp.SwordssmpMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> tracker clients: current Invisibility amplifier of a living entity.
 * Vanilla only sends mob-effect packets to the affected player (and passengers),
 * so observers cannot read other entities' amplifiers. -1 means no effect.
 */
public record InvisibilityAmplifierSyncMessage(int entityId, int amplifier) implements CustomPacketPayload {
	public static final Type<InvisibilityAmplifierSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, "invisibility_amplifier_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, InvisibilityAmplifierSyncMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, InvisibilityAmplifierSyncMessage::entityId,
			ByteBufCodecs.VAR_INT, InvisibilityAmplifierSyncMessage::amplifier,
			InvisibilityAmplifierSyncMessage::new);

	@Override
	public Type<InvisibilityAmplifierSyncMessage> type() {
		return TYPE;
	}
}
