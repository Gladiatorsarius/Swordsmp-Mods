package net.mcreator.swordssmp.procedures;

import java.util.Set;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

public class ChaosCrystalItemInInventoryTickProcedure {
	private static final Set<String> COOLDOWN_KEYS = Set.of("ChorusSlayerCooldownUntil", "GhostBladeCooldownUntil", "EarthWaveCooldownUntil", "GodsViewCooldownUntil", "PhantomBladeCooldownUntil", "WindBladeCooldownUntil",
			"WardenBlasterCooldownUntil", "TNTSwordCooldownUntil", "TNTSword2CooldownUntil", "TNTSword2CooldownOwner", "ThunderCooldownUntil");

	public static void execute(Entity entity) {
		if (!(entity instanceof Player player))
			return;
		int size = player.getInventory().getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack == null)
				continue;
			// Setting cooldown to 0 clears the native cooldown overlay for this stack.
			try {
				player.getCooldowns().addCooldown(stack, 0);
			} catch (Throwable ignored) {
				// If this mapping doesn't support the ItemStack overload, ignore.
			}
			// Remove known custom cooldown NBT keys.
			CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
			if (custom != null) {
				CompoundTag tag = custom.copyTag();
				boolean changed = false;
				for (String key : COOLDOWN_KEYS) {
					if (tag.contains(key)) {
						tag.remove(key);
						changed = true;
					}
				}
				if (changed) {
					stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				}
			}
		}
	}
}
