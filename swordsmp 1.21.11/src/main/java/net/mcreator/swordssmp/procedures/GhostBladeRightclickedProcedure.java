package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class GhostBladeRightclickedProcedure {
	public static boolean eventResult = true;

	public GhostBladeRightclickedProcedure() {
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
		if (mainHandStack.getItem() == SwordssmpModItems.GHOST_BLADE) {
			CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
			boolean tagChanged = false;
			if (entity instanceof Player player) {
				String ownerId = cooldownTag.getString("GhostBladeCooldownOwner").orElse("");
				if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
					cooldownTag.remove("GhostBladeCooldownUntil");
					cooldownTag.remove("GhostBladeCooldownOwner");
					tagChanged = true;
				}
			}
			long now = 0L;
			if (world instanceof Level _level) {
				now = _level.getGameTime();
			}
			long cooldownUntil = cooldownTag.getLong("GhostBladeCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("GhostBladeCooldownUntil");
				cooldownTag.remove("GhostBladeCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			if (entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES).GhostBladeDash == 0) {
				{
					SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
					_vars.GhostBladeDash = 1;
					_vars.markSyncDirty();
				}
				entity.push((entity.getLookAngle().x * 2), 0.2, (entity.getLookAngle().z * 2));

				// Ambient cave sound on activation with slight pitch randomization
				if (world instanceof Level _level) {
					float pitch = 0.7f + (_level.getRandom().nextFloat() * 0.6f);
					if (!_level.isClientSide()) {
						_level.playSound(null, x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("ambient.cave")), SoundSource.PLAYERS, 1.2f, pitch);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("ambient.cave")), SoundSource.PLAYERS, 1.2f, pitch, false);
					}
				}

				// Damage every entity intersected along the dash path using fell_out_of_world (bypasses totems)
				if (world instanceof ServerLevel _level) {
					Vec3 start = entity.position();
					Vec3 end = start.add(entity.getLookAngle().scale(4.0));
					AABB pathBox = new AABB(start, end).inflate(1.0);
					for (Entity target : _level.getEntities(entity, pathBox, e -> e.isAlive() && e != entity)) {
						if (target instanceof LivingEntity _living) {
							_living.hurt(_level.damageSources().fellOutOfWorld(), 4.0f);
						}
					}
				}
				if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
					_entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 1, false, true));
				}
				
				// Add native cooldown overlay
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getMainHandItem(), 200); // 10 seconds
				}

				cooldownTag.putLong("GhostBladeCooldownUntil", now + 200L);
				if (entity instanceof Player player) {
					cooldownTag.putString("GhostBladeCooldownOwner", player.getStringUUID());
				}
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
				
				// Reset variable after cooldown
				SwordssmpMod.queueServerWork(200, () -> {
					{
						SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
						_vars.GhostBladeDash = 0;
						_vars.markSyncDirty();
					}
				});
			}
		}
	}
}
