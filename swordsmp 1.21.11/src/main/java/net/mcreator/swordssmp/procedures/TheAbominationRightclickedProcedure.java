package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.UUIDUtil;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import com.mojang.serialization.Codec;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TheAbominationRightclickedProcedure {
	// Players currently borrowing Creative, mapped to the game mode to restore.
	// Persisted as world SavedData so neither a disconnect nor a server restart
	// inside the 60-second window can strand a player in Creative: the restore
	// lands when the timer fires (online) or on their next join (offline/restart).
	private static final SavedDataType<PendingRestores> PENDING_RESTORES_TYPE = new SavedDataType<>("swordssmp_abomination_pending_restores", () -> new PendingRestores(new ConcurrentHashMap<>()),
			Codec.unboundedMap(UUIDUtil.CODEC, GameType.CODEC).xmap(m -> new PendingRestores(new ConcurrentHashMap<>(m)), data -> data.restores), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private static final class PendingRestores extends SavedData {
		private final Map<UUID, GameType> restores;

		private PendingRestores(Map<UUID, GameType> restores) {
			this.restores = restores;
		}
	}

	private static PendingRestores data(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(PENDING_RESTORES_TYPE);
	}

	public TheAbominationRightclickedProcedure() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockentity) -> {
			execute(world, player);
			return true;
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PendingRestores data = data(server);
			GameType previousMode = data.restores.remove(handler.player.getUUID());
			if (previousMode != null) {
				data.setDirty();
				if (handler.player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
					handler.player.setGameMode(previousMode);
				}
			}
		});
	}

	/**
	 * Grants the player actually holding The Abomination 60 seconds of Creative,
	 * then restores whatever game mode they had before. Players already in
	 * Creative are left untouched. Relogging ends the borrowed window early
	 * (fail-safe direction); a restart can never leave Creative behind.
	 */
	public static void execute(LevelAccessor world, Entity entity) {
		if (!(entity instanceof ServerPlayer player))
			return;
		if (player.getMainHandItem().getItem() != SwordssmpModItems.THE_ABOMINATION)
			return;
		MinecraftServer server = player.level().getServer();
		if (server == null)
			return;
		PendingRestores data = data(server);
		UUID uuid = player.getUUID();
		if (data.restores.containsKey(uuid))
			return; // already borrowing Creative; keep the original mode to restore
		GameType current = player.gameMode.getGameModeForPlayer();
		if (current == GameType.CREATIVE)
			return;
		data.restores.put(uuid, current);
		data.setDirty();
		player.setGameMode(GameType.CREATIVE);
		SwordssmpMod.queueServerWork(1200, () -> {
			ServerPlayer online = server.getPlayerList().getPlayer(uuid);
			if (online != null) {
				PendingRestores d = data(server);
				GameType previousMode = d.restores.remove(uuid);
				if (previousMode != null) {
					d.setDirty();
					if (online.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
						online.setGameMode(previousMode);
					}
				}
			}
			// If offline, the persisted entry stays until the JOIN handler restores it on rejoin.
		});
	}
}
