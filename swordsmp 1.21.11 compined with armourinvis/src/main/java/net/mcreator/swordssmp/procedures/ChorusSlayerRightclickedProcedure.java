package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class ChorusSlayerRightclickedProcedure {
	public static boolean eventResult = true;

	public ChorusSlayerRightclickedProcedure() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (hand == player.getUsedItemHand())
				execute(level, player.getX(), player.getY(), player.getZ(), player);
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() == SwordssmpModItems.CHORUS_SLAYER) {
			CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
			boolean tagChanged = false;
			if (entity instanceof Player player) {
				String ownerId = cooldownTag.getString("ChorusSlayerCooldownOwner").orElse("");
				if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
					cooldownTag.remove("ChorusSlayerCooldownUntil");
					cooldownTag.remove("ChorusSlayerCooldownOwner");
					tagChanged = true;
				}
			}
			long now = 0L;
			if (world instanceof Level _level) {
				now = _level.getGameTime();
			}
			long cooldownUntil = cooldownTag.getLong("ChorusSlayerCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("ChorusSlayerCooldownUntil");
				cooldownTag.remove("ChorusSlayerCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			if (entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES).TP == 0) {
				// Ability is ready - execute
				
				// Mark ability as used
				{
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.TP = 1;
					_vars.markSyncDirty();
				}
				
				entity.push((entity.getLookAngle().x * 10), 0.2, (entity.getLookAngle().z * 10));
				
				// Play sound (fixed volume and source)
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.chorus_fruit.teleport")), SoundSource.PLAYERS, 3, 40);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.chorus_fruit.teleport")), SoundSource.PLAYERS, 3, 40, false);
					}
				}
				
				// Spawn particles
				if (world instanceof ServerLevel _level) {
					_level.sendParticles(ParticleTypes.END_ROD, x, y, z, 80, 3, 0, 3, 5);
				}
				
				// Add native cooldown overlay
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getMainHandItem(), 1200); // 60 seconds
				}

				cooldownTag.putLong("ChorusSlayerCooldownUntil", now + 1200L);
				if (entity instanceof Player player) {
					cooldownTag.putString("ChorusSlayerCooldownOwner", player.getStringUUID());
				}
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
				
				// Reset variable after cooldown
				SwordssmpMod.queueServerWork(1200, () -> {
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.TP = 0;
					_vars.markSyncDirty();
				});
			}
		}
	}
}
