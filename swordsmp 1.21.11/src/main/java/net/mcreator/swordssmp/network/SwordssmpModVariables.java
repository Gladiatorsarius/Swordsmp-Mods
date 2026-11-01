package net.mcreator.swordssmp.network;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.mcreator.swordssmp.event.PlayerEvents;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;

public class SwordssmpModVariables {
	public static final AttachmentType<PlayerVariables> PLAYER_VARIABLES = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, "player_variables"),
			(builder) -> builder.persistent(PlayerVariables.CODEC).initializer(PlayerVariables::new));

	public static void variablesLoad() {
		PayloadTypeRegistry.playS2C().register(PlayerVariablesSyncMessage.TYPE, PlayerVariablesSyncMessage.STREAM_CODEC);
		ServerPlayerEvents.JOIN.register((player) -> {
			PlayerVariables vars = player.getAttachedOrCreate(PLAYER_VARIABLES);
			vars.Djump = 0;
			vars.WindBladeBautaReset = 0;
			vars.markSyncDirty();
			ServerPlayNetworking.send(player, new PlayerVariablesSyncMessage(player.getAttachedOrCreate(PLAYER_VARIABLES)));
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			PlayerVariables vars = newPlayer.getAttachedOrCreate(PLAYER_VARIABLES);
			vars.Djump = 0;
			vars.WindBladeBautaReset = 0;
			vars.markSyncDirty();
			ServerPlayNetworking.send(newPlayer, new PlayerVariablesSyncMessage(vars));
		});
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			if (!destination.isClientSide())
				ServerPlayNetworking.send(player, new PlayerVariablesSyncMessage(player.getAttachedOrCreate(PLAYER_VARIABLES)));
		});
		PlayerEvents.END_PLAYER_TICK.register((entity) -> {
			if (entity instanceof ServerPlayer player && player.getAttachedOrCreate(PLAYER_VARIABLES)._syncDirty) {
				ServerPlayNetworking.send(player, new PlayerVariablesSyncMessage(player.getAttachedOrCreate(PLAYER_VARIABLES)));
				player.getAttachedOrCreate(PLAYER_VARIABLES)._syncDirty = false;
			}
		});
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			PlayerVariables original = oldPlayer.getAttachedOrCreate(PLAYER_VARIABLES);
			PlayerVariables clone = new PlayerVariables();
			if (alive) {
				clone.Djump = original.Djump;
				clone.WindBladeBautaReset = original.WindBladeBautaReset;
			}
			newPlayer.setAttached(PLAYER_VARIABLES, clone);
		});
	}

	public static class PlayerVariables {
		public static final Codec<PlayerVariables> CODEC = RecordCodecBuilder.create(builder -> builder
				.group(Codec.DOUBLE.fieldOf("Djump").orElse(0d).forGetter((vars) -> vars.Djump), Codec.DOUBLE.fieldOf("WindBladeBautaReset").orElse(0d).forGetter((vars) -> vars.WindBladeBautaReset))
				.apply(builder, PlayerVariables::new));
		boolean _syncDirty = false;
		public double Djump = 0;
		public double WindBladeBautaReset = 0;

		public PlayerVariables() {
		}

		public PlayerVariables(double Djump, double WindBladeBautaReset) {
			this.Djump = Djump;
			this.WindBladeBautaReset = WindBladeBautaReset;
		}

		public void serialize(ValueOutput output) {
			output.putDouble("Djump", Djump);
			output.putDouble("WindBladeBautaReset", WindBladeBautaReset);
		}

		public void deserialize(ValueInput input) {
			Djump = input.getDoubleOr("Djump", 0);
			WindBladeBautaReset = input.getDoubleOr("WindBladeBautaReset", 0);
		}

		public void markSyncDirty() {
			_syncDirty = true;
		}
	}

	public record PlayerVariablesSyncMessage(PlayerVariables data) implements CustomPacketPayload {
		public static final Type<PlayerVariablesSyncMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SwordssmpMod.MODID, "player_variables_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PlayerVariablesSyncMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, PlayerVariablesSyncMessage message) -> {
			TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
			message.data.serialize(output);
			buffer.writeNbt(output.buildResult());
		}, (RegistryFriendlyByteBuf buffer) -> {
			PlayerVariablesSyncMessage message = new PlayerVariablesSyncMessage(new PlayerVariables());
			message.data.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, buffer.registryAccess(), buffer.readNbt()));
			return message;
		});

		@Override
		public Type<PlayerVariablesSyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final PlayerVariablesSyncMessage message, final ClientPlayNetworking.Context context) {
			if (message.data != null) {
				context.client().execute(() -> {
					TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.player().registryAccess());
					message.data.serialize(output);
					context.player().getAttachedOrCreate(PLAYER_VARIABLES).deserialize(TagValueInput.create(ProblemReporter.DISCARDING, context.player().registryAccess(), output.buildResult()));
				});
			}
		}
	}
}
