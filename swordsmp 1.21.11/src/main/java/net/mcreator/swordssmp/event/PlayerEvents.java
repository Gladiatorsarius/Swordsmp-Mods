package net.mcreator.swordssmp.event;

import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.Event;

import java.util.Arrays;

public class PlayerEvents {
	public static final Event<TickEnd> END_PLAYER_TICK = EventFactory.createArrayBacked(TickEnd.class, (callbacks) -> (entity) -> Arrays.stream(callbacks).forEach(callback -> callback.onEndTick(entity)));

	@FunctionalInterface
	public interface TickEnd {
		void onEndTick(Player player);
	}
}
