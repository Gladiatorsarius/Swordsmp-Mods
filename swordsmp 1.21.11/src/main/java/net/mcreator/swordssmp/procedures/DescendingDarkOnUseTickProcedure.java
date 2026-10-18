package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class DescendingDarkOnUseTickProcedure {
	public static final Map<UUID, Float> DESCENDING_DARK_TNT_POWERS = new HashMap<>();

	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (!(entity instanceof Player player))
			return;

		if (player.onGround()) {
			boolean needsReset = false;
			double currentGravity = 0.08;
			if (player.getAttribute(Attributes.GRAVITY) != null) {
				currentGravity = player.getAttribute(Attributes.GRAVITY).getBaseValue();
				if (currentGravity != player.getAttribute(Attributes.GRAVITY).getAttribute().value().getDefaultValue()) {
					needsReset = true;
				}
			}
			if (player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER) != null) {
				if (player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER).getBaseValue() != player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER).getAttribute().value().getDefaultValue()) {
					needsReset = true;
				}
			}
			if (needsReset) {
				if (player.getAttribute(Attributes.GRAVITY) != null) {
					player.getAttribute(Attributes.GRAVITY).setBaseValue(player.getAttribute(Attributes.GRAVITY).getAttribute().value().getDefaultValue());
				}
				if (player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER) != null) {
					player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER).setBaseValue(player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER).getAttribute().value().getDefaultValue());
				}
				if (world instanceof ServerLevel serverLevel) {
					int fallTicks = Math.max(0, (int)((currentGravity - 0.08) / 0.004));
					int secondsFallen = fallTicks / 20;
					int explosionPower = Math.min(4 + secondsFallen, 14);
					// Apply Resistance V for 5 ticks before explosion
					player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 5, 4, false, false));
					PrimedTnt tnt = EntityType.TNT.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
					if (tnt != null) {
						tnt.setPos(player.getX(), player.getY(), player.getZ());
						tnt.setFuse(0);
						serverLevel.addFreshEntity(tnt);
						DESCENDING_DARK_TNT_POWERS.put(tnt.getUUID(), (float)explosionPower);
					}
				}
				ItemStack mainHandStack = player.getMainHandItem();
				if (mainHandStack.getItem() == SwordssmpModItems.DESCENDING_DARK) {
					CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
					CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
					long now = 0L;
					if (world instanceof Level _level) {
						now = _level.getGameTime();
					}
					cooldownTag.putLong("DescendingDarkCooldownUntil", now + 200L);
					cooldownTag.putString("DescendingDarkCooldownOwner", player.getStringUUID());
					mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
					if (!world.isClientSide()) {
						player.getCooldowns().addCooldown(mainHandStack, 200);
					}
				}
				player.stopUsingItem();
			}
		} else {
			if (player.getAttribute(Attributes.GRAVITY) != null) {
				double current = player.getAttribute(Attributes.GRAVITY).getBaseValue();
				player.getAttribute(Attributes.GRAVITY).setBaseValue(Math.min(current + 0.004, 0.5));
			}
			if (player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER) != null) {
				player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER).setBaseValue(0.0);
			}
		}
	}
}
