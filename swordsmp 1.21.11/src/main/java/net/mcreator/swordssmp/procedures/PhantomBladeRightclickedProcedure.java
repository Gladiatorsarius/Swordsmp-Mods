package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class PhantomBladeRightclickedProcedure {
	public static boolean eventResult = true;

	public PhantomBladeRightclickedProcedure() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (hand == player.getUsedItemHand())
				execute(level, player);
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});
	}

	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		ItemStack offhandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY);
		if (offhandStack.getItem() == SwordssmpModItems.PHANTOM_BLADE) {
			CustomData customData = offhandStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
			boolean tagChanged = false;
			if (entity instanceof Player player) {
				String ownerId = cooldownTag.getString("PhantomBladeCooldownOwner").orElse("");
				if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
					cooldownTag.remove("PhantomBladeCooldownUntil");
					cooldownTag.remove("PhantomBladeCooldownOwner");
					tagChanged = true;
				}
			}
			long now = 0L;
			if (world instanceof Level _level) {
				now = _level.getGameTime();
			}
			long cooldownUntil = cooldownTag.getLong("PhantomBladeCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("PhantomBladeCooldownUntil");
				cooldownTag.remove("PhantomBladeCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				offhandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			if (entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES).PhantomInvissCooldown == 0) {
				if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
					_entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 1, false, false));
				}
				{
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.PhantomInvissCooldown = 1;
					_vars.markSyncDirty();
				}
				
				// Add native cooldown overlay
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getOffhandItem(), 600); // 30 seconds
				}

				cooldownTag.putLong("PhantomBladeCooldownUntil", now + 600L);
				if (entity instanceof Player player) {
					cooldownTag.putString("PhantomBladeCooldownOwner", player.getStringUUID());
				}
				offhandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
				
				// Reset variable after cooldown
				SwordssmpMod.queueServerWork(600, () -> {
					{
						SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
						_vars.PhantomInvissCooldown = 0;
						_vars.markSyncDirty();
					}
				});
			}
		}
	}
}
