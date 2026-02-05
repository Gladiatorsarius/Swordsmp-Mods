package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

import net.mcreator.swordssmp.init.SwordssmpModItems;

public class AncientVoidRelicRightclickedProcedure {
	public static boolean eventResult = true;

	private static final long COOLDOWN_TICKS = 12000L;
	private static final long ACTIVE_WINDOW_TICKS = 200L;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() != SwordssmpModItems.ANCIENT_VOID_RELIC) {
			return;
		}
		CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
		CompoundTag relicTag = customData != null ? customData.copyTag() : new CompoundTag();
		boolean tagChanged = false;
		if (entity instanceof Player player) {
			String ownerId = relicTag.getString("VoidRelicOwner").orElse("");
			if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
				relicTag.remove("VoidRelicCooldownUntil");
				relicTag.remove("VoidRelicActiveUntil");
				relicTag.remove("VoidRelicWarned");
				relicTag.remove("VoidRelicOwner");
				tagChanged = true;
			}
		}
		long now = 0L;
		if (world instanceof Level _level) {
			now = _level.getGameTime();
		}
		long cooldownRemaining = relicTag.getLong("VoidRelicCooldownRemaining").orElse(0L);
		long activeUntil = relicTag.getLong("VoidRelicActiveUntil").orElse(0L);
		boolean initialized = relicTag.getBoolean("VoidRelicInitialized").orElse(false);
		if (activeUntil > 0L && now > activeUntil) {
			activeUntil = 0L;
			cooldownRemaining = COOLDOWN_TICKS;
			relicTag.putLong("VoidRelicActiveUntil", activeUntil);
			relicTag.putLong("VoidRelicCooldownRemaining", cooldownRemaining);
			relicTag.putBoolean("VoidRelicWarned", false);
			tagChanged = true;
		}
		if (!initialized) {
			cooldownRemaining = COOLDOWN_TICKS;
			relicTag.putLong("VoidRelicCooldownRemaining", cooldownRemaining);
			relicTag.putBoolean("VoidRelicWarned", false);
			relicTag.putBoolean("VoidRelicInitialized", true);
			tagChanged = true;
		}
		if (cooldownRemaining > 0L) {
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(relicTag));
			}
			return;
		}
		if (activeUntil == 0L) {
			activeUntil = now + ACTIVE_WINDOW_TICKS;
			relicTag.putLong("VoidRelicActiveUntil", activeUntil);
			relicTag.putBoolean("VoidRelicWarned", false);
			if (entity instanceof Player player && !player.level().isClientSide()) {
				player.getCooldowns().addCooldown(mainHandStack, (int) ACTIVE_WINDOW_TICKS);
			}
			tagChanged = true;
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(relicTag));
			}
			return;
		}
		if (activeUntil > 0L && now <= activeUntil) {
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(relicTag));
			}
			return;
		}
		if (tagChanged) {
			mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(relicTag));
		}
	}
}
