package net.mcreator.swordssmp.procedures;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

public class ChaosCrystalItemInInventoryTickProcedure {
	public static boolean eventResult = true;

	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player player) {
			int size = player.getInventory().getContainerSize();
			for (int i = 0; i < size; i++) {
				ItemStack stack = player.getInventory().getItem(i);
				if (stack == null)
					continue;
				// Attempt to clear native cooldown overlay for this stack (ItemStack overload)
				try {
					// Setting cooldown to 0 clears the cooldown in this mapping
					player.getCooldowns().addCooldown(stack, 0);
				} catch (Throwable ignored) {
					// If this mapping doesn't support ItemStack overload, ignore.
				}
				// Remove known custom cooldown NBT keys (non-native metadata)
				CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
				if (custom != null) {
					CompoundTag tag = custom.copyTag();
					boolean changed = false;
					// Known cooldown keys used across items
					String[] knownKeys = new String[] { "ChorusSlayerCooldownUntil", "GhostBladeCooldownUntil", "EarthWaveCooldownUntil", "GodsViewCooldownUntil", "PhantomBladeCooldownUntil", "WindBladeCooldownUntil", "WardenBlasterCooldownUntil", "TNTSwordCooldownUntil", "ThunderCooldownUntil" };
					for (String k : knownKeys) {
						if (tag.contains(k)) {
							tag.remove(k);
							changed = true;
						}
					}
					if (changed) {
						stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
					}
				}
			}
			// Player-variables removed; Chaos Crystal no longer manipulates PlayerVariables.
		}
	}
}
