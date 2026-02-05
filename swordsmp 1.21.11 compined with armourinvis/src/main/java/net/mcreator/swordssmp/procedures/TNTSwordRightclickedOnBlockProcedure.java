package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class TNTSwordRightclickedOnBlockProcedure {
	public static boolean eventResult = true;

	public TNTSwordRightclickedOnBlockProcedure() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (hand == player.getUsedItemHand())
				execute(level, hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ(), player);
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() == SwordssmpModItems.TNT_SWORD) {
			CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
			boolean tagChanged = false;
			if (entity instanceof Player player) {
				String ownerId = cooldownTag.getString("TNTSwordCooldownOwner").orElse("");
				if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
					cooldownTag.remove("TNTSwordCooldownUntil");
					cooldownTag.remove("TNTSwordCooldownOwner");
					tagChanged = true;
				}
			}
			long now = 0L;
			if (world instanceof Level _level) {
				now = _level.getGameTime();
			}
			long cooldownUntil = cooldownTag.getLong("TNTSwordCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("TNTSwordCooldownUntil");
				cooldownTag.remove("TNTSwordCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			if (entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES).TNTCooldown < 1) {
				{
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.TNTCooldown = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES).TNTCooldown + 1;
					_vars.markSyncDirty();
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.tnt.primed")), SoundSource.NEUTRAL, 50, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.tnt.primed")), SoundSource.NEUTRAL, 50, 1, false);
					}
				}
				if (world instanceof ServerLevel _level) {
					Entity entityToSpawn_3 = EntityType.TNT_MINECART.spawn(_level, BlockPos.containing(x, y, z), EntitySpawnReason.MOB_SUMMONED);
					if (entityToSpawn_3 != null) {
						entityToSpawn_3.setDeltaMovement(0, 1.5, 0);
					}
				}
				// replace chat countdowns with native cooldown overlay
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getMainHandItem(), 300); // 15 seconds
				}

				cooldownTag.putLong("TNTSwordCooldownUntil", now + 300L);
				if (entity instanceof Player player) {
					cooldownTag.putString("TNTSwordCooldownOwner", player.getStringUUID());
				}
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
				// reset TNT cooldown after 15 seconds
				SwordssmpMod.queueServerWork(300, () -> {
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.TNTCooldown = 0;
					_vars.markSyncDirty();
				});
			}
		}
	}
}
