package net.mcreator.swordssmp;

import org.jetbrains.annotations.Nullable;

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

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.EnvType;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandle;

public class SwordssmpMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger(SwordssmpMod.class);
	public static final String MODID = "swordssmp";

	@Override
	public void onInitialize() {
		// Start of user code block mod constructor
		// End of user code block mod constructor
		LOGGER.info("Initializing SwordssmpMod");

		SwordssmpModTabs.load();
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
		// Start of user code block mod init
		// End of user code block mod init
	}

	// Start of user code block mod methods
	// End of user code block mod methods
	private static final Collection<Tuple<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		workQueue.add(new Tuple<>(action, tick));
	}

	private void tick() {
		ServerTickEvents.END_SERVER_TICK.register((server) -> {
			List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setB(work.getB() - 1);
				if (work.getB() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getA().run());
			workQueue.removeAll(actions);
		});
	}

	private static void clearThunderBladeCooldown(Player player) {
		if (player == null) {
			return;
		}
		int size = player.getInventory().getContainerSize();
		for (int slot = 0; slot < size; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			clearThunderBladeCooldown(stack);
		}
	}

	private static void clearThunderBladeCooldown(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return;
		}
		if (stack.getItem() != SwordssmpModItems.THUNDER_SWORD) {
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

	private static Object minecraft;
	private static MethodHandle playerHandle;

	@Nullable
	public static Player clientPlayer() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			try {
				if (minecraft == null || playerHandle == null) {
					Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
					minecraft = MethodHandles.publicLookup().findStatic(minecraftClass, "getInstance", MethodType.methodType(minecraftClass)).invoke();
					playerHandle = MethodHandles.publicLookup().findGetter(minecraftClass, "player", Class.forName("net.minecraft.client.player.LocalPlayer"));
				}
				return (Player) playerHandle.invoke(minecraft);
			} catch (Throwable e) {
				LOGGER.error("Failed to get client player", e);
				return null;
			}
		} else {
			return null;
		}
	}
}
