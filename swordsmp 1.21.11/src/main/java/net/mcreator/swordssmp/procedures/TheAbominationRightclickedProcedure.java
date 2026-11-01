package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class TheAbominationRightclickedProcedure {

	public TheAbominationRightclickedProcedure() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockentity) -> {
			execute(world, player);
			return true;
		});
	}

	/**
	 * Grants the player actually holding The Abomination 60 seconds of Creative,
	 * then restores whatever game mode they had before. Players already in
	 * Creative are left untouched (the old /gamemode commands demoted them to
	 * Survival and could target the nearest player instead of the user).
	 */
	public static void execute(LevelAccessor world, Entity entity) {
		if (!(entity instanceof ServerPlayer player))
			return;
		if (player.getMainHandItem().getItem() != SwordssmpModItems.THE_ABOMINATION)
			return;
		if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)
			return;
		GameType previousMode = player.gameMode.getGameModeForPlayer();
		player.setGameMode(GameType.CREATIVE);
		SwordssmpMod.queueServerWork(1200, () -> {
			if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
				player.setGameMode(previousMode);
			}
		});
	}
}
