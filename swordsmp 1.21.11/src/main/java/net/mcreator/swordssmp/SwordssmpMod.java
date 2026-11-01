package net.mcreator.swordssmp;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.*;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.api.ModInitializer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

public class SwordssmpMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger(SwordssmpMod.class);
	public static final String MODID = "swordssmp";

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing SwordssmpMod");

		SwordssmpModTabs.load();
		SwordssmpCustomItemGroups.register();
		SwordssmpModVariables.variablesLoad();
		SwordssmpModItems.load();

		SwordssmpModBrewingRecipes.load();

		SwordssmpModMobEffects.load();
		SwordssmpModPotions.load();
		SwordssmpModGameRules.load();

		SwordssmpModProcedures.load();

		tick();
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			Player player = handler.player;
			if (player != null) {
				clearThunderBladeCooldown(player);
			}
		});
	}

	private static final Collection<Tuple<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		workQueue.add(new Tuple<>(action, tick));
	}

	private void tick() {
		ServerTickEvents.END_SERVER_TICK.register((server) -> {
			List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setB(work.getB() - 1);
				if (work.getB() <= 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getA().run());
			workQueue.removeAll(actions);
		});
	}

	private static void clearThunderBladeCooldown(Player player) {
		int size = player.getInventory().getContainerSize();
		for (int slot = 0; slot < size; slot++) {
			clearThunderBladeCooldown(player.getInventory().getItem(slot));
		}
	}

	private static void clearThunderBladeCooldown(ItemStack stack) {
		if (stack == null || stack.isEmpty() || stack.getItem() != SwordssmpModItems.THUNDER_SWORD) {
			return;
		}
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return;
		}
		CompoundTag tag = customData.copyTag();
		tag.remove("ThunderCooldownUntil");
		tag.remove("ThunderCooldownOwner");
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}
}
