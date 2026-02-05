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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import net.mcreator.swordssmp.init.SwordssmpModItems;

public class AncientVoidRelicToolInHandTickProcedure {
	public static boolean eventResult = true;

	private static final long COOLDOWN_TICKS = 12000L;
	private static final long ACTIVE_WINDOW_TICKS = 200L;
	private static final long WARNING_TICKS = 200L;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack relicStack = ItemStack.EMPTY;
		boolean isHeld = false;
		if (entity instanceof LivingEntity livingEntity) {
			ItemStack mainHand = livingEntity.getMainHandItem();
			ItemStack offHand = livingEntity.getOffhandItem();
			if (mainHand.getItem() == SwordssmpModItems.ANCIENT_VOID_RELIC) {
				relicStack = mainHand;
				isHeld = true;
			} else if (offHand.getItem() == SwordssmpModItems.ANCIENT_VOID_RELIC) {
				relicStack = offHand;
				isHeld = true;
			}
		}
		if (relicStack.isEmpty()) {
			return;
		}
		CustomData customData = relicStack.get(DataComponents.CUSTOM_DATA);
		CompoundTag relicTag = customData != null ? customData.copyTag() : new CompoundTag();
		if (entity instanceof Player player) {
			String ownerId = relicTag.getString("VoidRelicOwner").orElse("");
			if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
				relicTag.remove("VoidRelicCooldownUntil");
				relicTag.remove("VoidRelicActiveUntil");
				relicTag.remove("VoidRelicWarned");
				relicTag.remove("VoidRelicOwner");
			}
		}
		long now = 0L;
		if (world instanceof Level _level) {
			now = _level.getGameTime();
		}
		long cooldownRemaining = relicTag.getLong("VoidRelicCooldownRemaining").orElse(0L);
		long activeUntil = relicTag.getLong("VoidRelicActiveUntil").orElse(0L);
		boolean warned = relicTag.getBoolean("VoidRelicWarned").orElse(false);
		boolean initialized = relicTag.getBoolean("VoidRelicInitialized").orElse(false);
		if (activeUntil > 0L && now > activeUntil) {
			activeUntil = 0L;
			cooldownRemaining = COOLDOWN_TICKS;
			relicTag.putLong("VoidRelicActiveUntil", activeUntil);
			relicTag.putLong("VoidRelicCooldownRemaining", cooldownRemaining);
			relicTag.putBoolean("VoidRelicWarned", false);
			relicStack.set(DataComponents.CUSTOM_DATA, CustomData.of(relicTag));
			return;
		}
		if (cooldownRemaining <= 0L && activeUntil <= 0L) {
			if (!initialized) {
				cooldownRemaining = COOLDOWN_TICKS;
				relicTag.putLong("VoidRelicCooldownRemaining", cooldownRemaining);
				relicTag.putBoolean("VoidRelicWarned", false);
				relicTag.putBoolean("VoidRelicInitialized", true);
			}
			if (entity instanceof Player player) {
				relicTag.putString("VoidRelicOwner", player.getStringUUID());
			}
			relicStack.set(DataComponents.CUSTOM_DATA, CustomData.of(relicTag));
			return;
		}
		if (!isHeld) {
			return;
		}
		if (cooldownRemaining > 0L) {
			cooldownRemaining = Math.max(0L, cooldownRemaining - 1L);
			relicTag.putLong("VoidRelicCooldownRemaining", cooldownRemaining);
			if (!warned && cooldownRemaining <= WARNING_TICKS) {
				if (entity instanceof ServerPlayer serverPlayer) {
					serverPlayer.displayClientMessage(Component.literal("10 seconds until AncientVoidRelic is ready."), true);
				}
				relicTag.putBoolean("VoidRelicWarned", true);
			}
			relicStack.set(DataComponents.CUSTOM_DATA, CustomData.of(relicTag));
		}
	}
}
