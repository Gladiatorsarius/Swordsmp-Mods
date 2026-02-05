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
			resetPlayerVariables(vars);
			vars.markSyncDirty();
			ServerPlayNetworking.send(player, new PlayerVariablesSyncMessage(player.getAttachedOrCreate(PLAYER_VARIABLES)));
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			PlayerVariables vars = newPlayer.getAttachedOrCreate(PLAYER_VARIABLES);
			resetPlayerVariables(vars);
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
				clone.WindBladeCooldown = original.WindBladeCooldown;
				clone.Djump = original.Djump;
				clone.WindBladeBautaReset = original.WindBladeBautaReset;
				clone.TP = original.TP;
				clone.removeBlock = original.removeBlock;
				clone.ThunderCooldown = original.ThunderCooldown;
				clone.phantomwings = original.phantomwings;
				clone.TNTCooldown = original.TNTCooldown;
				clone.GhostBladeDash = original.GhostBladeDash;
				clone.VoidRelicTimer = original.VoidRelicTimer;
				clone.WardenBlasterCharge = original.WardenBlasterCharge;
				clone.DripstoneCooldown = original.DripstoneCooldown;
				clone.godsviewCooldown = original.godsviewCooldown;
				clone.PhantomInvissCooldown = original.PhantomInvissCooldown;
				clone.Abomination = original.Abomination;
			}
			newPlayer.setAttached(PLAYER_VARIABLES, clone);
		});
	}

	private static void resetPlayerVariables(PlayerVariables vars) {
		vars.WindBladeCooldown = 0;
		vars.Djump = 0;
		vars.WindBladeBautaReset = 0;
		vars.TP = 0;
		vars.removeBlock = false;
		vars.ThunderCooldown = 0;
		vars.phantomwings = 0;
		vars.TNTCooldown = 0;
		vars.GhostBladeDash = 0;
		vars.VoidRelicTimer = 1200;
		vars.WardenBlasterCharge = 0;
		vars.DripstoneCooldown = 0;
		vars.godsviewCooldown = 0;
		vars.PhantomInvissCooldown = 0;
		vars.Abomination = 0;
	}

	public static class PlayerVariables {
		public static final Codec<PlayerVariables> CODEC = RecordCodecBuilder.create(builder -> builder.group(Codec.DOUBLE.fieldOf("WindBladeCooldown").orElse(0d).forGetter((vars) -> vars.WindBladeCooldown),
				Codec.DOUBLE.fieldOf("Djump").orElse(0d).forGetter((vars) -> vars.Djump), Codec.DOUBLE.fieldOf("WindBladeBautaReset").orElse(0d).forGetter((vars) -> vars.WindBladeBautaReset),
				Codec.DOUBLE.fieldOf("TP").orElse(0d).forGetter((vars) -> vars.TP), Codec.BOOL.fieldOf("removeBlock").orElse(false).forGetter((vars) -> vars.removeBlock),
				Codec.DOUBLE.fieldOf("ThunderCooldown").orElse(0d).forGetter((vars) -> vars.ThunderCooldown), Codec.DOUBLE.fieldOf("phantomwings").orElse(0d).forGetter((vars) -> vars.phantomwings),
				Codec.DOUBLE.fieldOf("TNTCooldown").orElse(0d).forGetter((vars) -> vars.TNTCooldown), Codec.DOUBLE.fieldOf("GhostBladeDash").orElse(0d).forGetter((vars) -> vars.GhostBladeDash),
				Codec.DOUBLE.fieldOf("VoidRelicTimer").orElse(0d).forGetter((vars) -> vars.VoidRelicTimer), Codec.DOUBLE.fieldOf("WardenBlasterCharge").orElse(0d).forGetter((vars) -> vars.WardenBlasterCharge),
				Codec.DOUBLE.fieldOf("DripstoneCooldown").orElse(0d).forGetter((vars) -> vars.DripstoneCooldown), Codec.DOUBLE.fieldOf("godsviewCooldown").orElse(0d).forGetter((vars) -> vars.godsviewCooldown),
				Codec.DOUBLE.fieldOf("PhantomInvissCooldown").orElse(0d).forGetter((vars) -> vars.PhantomInvissCooldown), Codec.DOUBLE.fieldOf("Abomination").orElse(0d).forGetter((vars) -> vars.Abomination)).apply(builder, PlayerVariables::new));
		boolean _syncDirty = false;
		public double WindBladeCooldown = 0;
		public double Djump = 0;
		public double WindBladeBautaReset = 0;
		public double TP = 0;
		public boolean removeBlock = false;
		public double ThunderCooldown = 0;
		public double phantomwings = 0;
		public double TNTCooldown = 0;
		public double GhostBladeDash = 0;
		public double VoidRelicTimer = 0;
		public double WardenBlasterCharge = 0;
		public double DripstoneCooldown = 0;
		public double godsviewCooldown = 0;
		public double PhantomInvissCooldown = 0;
		public double Abomination = 0;

		public PlayerVariables() {
		}

		public PlayerVariables(double WindBladeCooldown, double Djump, double WindBladeBautaReset, double TP, boolean removeBlock, double ThunderCooldown, double phantomwings, double TNTCooldown, double GhostBladeDash, double VoidRelicTimer,
				double WardenBlasterCharge, double DripstoneCooldown, double godsviewCooldown, double PhantomInvissCooldown, double Abomination) {
			this.WindBladeCooldown = WindBladeCooldown;
			this.Djump = Djump;
			this.WindBladeBautaReset = WindBladeBautaReset;
			this.TP = TP;
			this.removeBlock = removeBlock;
			this.ThunderCooldown = ThunderCooldown;
			this.phantomwings = phantomwings;
			this.TNTCooldown = TNTCooldown;
			this.GhostBladeDash = GhostBladeDash;
			this.VoidRelicTimer = VoidRelicTimer;
			this.WardenBlasterCharge = WardenBlasterCharge;
			this.DripstoneCooldown = DripstoneCooldown;
			this.godsviewCooldown = godsviewCooldown;
			this.PhantomInvissCooldown = PhantomInvissCooldown;
			this.Abomination = Abomination;
		}

		public void serialize(ValueOutput output) {
			output.putDouble("WindBladeCooldown", WindBladeCooldown);
			output.putDouble("Djump", Djump);
			output.putDouble("WindBladeBautaReset", WindBladeBautaReset);
			output.putDouble("TP", TP);
			output.putBoolean("removeBlock", removeBlock);
			output.putDouble("ThunderCooldown", ThunderCooldown);
			output.putDouble("phantomwings", phantomwings);
			output.putDouble("TNTCooldown", TNTCooldown);
			output.putDouble("GhostBladeDash", GhostBladeDash);
			output.putDouble("VoidRelicTimer", VoidRelicTimer);
			output.putDouble("WardenBlasterCharge", WardenBlasterCharge);
			output.putDouble("DripstoneCooldown", DripstoneCooldown);
			output.putDouble("godsviewCooldown", godsviewCooldown);
			output.putDouble("PhantomInvissCooldown", PhantomInvissCooldown);
			output.putDouble("Abomination", Abomination);
		}

		public void deserialize(ValueInput input) {
			WindBladeCooldown = input.getDoubleOr("WindBladeCooldown", 0);
			Djump = input.getDoubleOr("Djump", 0);
			WindBladeBautaReset = input.getDoubleOr("WindBladeBautaReset", 0);
			TP = input.getDoubleOr("TP", 0);
			removeBlock = input.getBooleanOr("removeBlock", false);
			ThunderCooldown = input.getDoubleOr("ThunderCooldown", 0);
			phantomwings = input.getDoubleOr("phantomwings", 0);
			TNTCooldown = input.getDoubleOr("TNTCooldown", 0);
			GhostBladeDash = input.getDoubleOr("GhostBladeDash", 0);
			VoidRelicTimer = input.getDoubleOr("VoidRelicTimer", 0);
			WardenBlasterCharge = input.getDoubleOr("WardenBlasterCharge", 0);
			DripstoneCooldown = input.getDoubleOr("DripstoneCooldown", 0);
			godsviewCooldown = input.getDoubleOr("godsviewCooldown", 0);
			PhantomInvissCooldown = input.getDoubleOr("PhantomInvissCooldown", 0);
			Abomination = input.getDoubleOr("Abomination", 0);
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
