package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TheAbominationRightclickedProcedure {
	// Players currently borrowing Creative, mapped to the game mode to restore.
	// Tracked by UUID so the restore still happens if the player disconnects
	// before the 60 seconds are up (applied on rejoin instead).
	private static final Map<UUID, GameType> PENDING_RESTORES = new ConcurrentHashMap<>();

	public TheAbominationRightclickedProcedure() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockentity) -> {
			execute(world, player);
			return true;
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			GameType previousMode = PENDING_RESTORES.remove(handler.player.getUUID());
			if (previousMode != null && handler.player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
				handler.player.setGameMode(previousMode);
			}
		});
	}

	/**
	 * Grants the player actually holding The Abomination 60 seconds of Creative,
	 * then restores whatever game mode they had before. Players already in
	 * Creative are left untouched.
	 */
	public static void execute(LevelAccessor world, Entity entity) {
		if (!(entity instanceof ServerPlayer player))
			return;
		if (player.getMainHandItem().getItem() != SwordssmpModItems.THE_ABOMINATION)
			return;
		UUID uuid = player.getUUID();
		if (PENDING_RESTORES.containsKey(uuid))
			return; // already borrowing Creative; keep the original mode to restore
		if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
			return;
		PENDING_RESTORES.put(uuid, player.gameMode.getGameModeForPlayer());
		player.setGameMode(GameType.CREATIVE);
		SwordssmpMod.queueServerWork(1200, () -> {
			ServerPlayer online = player.level().getServer() != null ? player.level().getServer().getPlayerList().getPlayer(uuid) : null;
			if (online != null) {
				GameType previousMode = PENDING_RESTORES.remove(uuid);
				if (previousMode != null && online.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
					online.setGameMode(previousMode);
				}
			}
			// If offline, the entry stays until the JOIN handler restores it on rejoin.
		});
	}
}
