package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;
import net.minecraft.world.InteractionResult;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.mcreator.swordssmp.event.PlayerEvents;

public class WindBladeRightclickedProcedure {
	public static boolean eventResult = true;

	public WindBladeRightclickedProcedure() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (hand == player.getUsedItemHand())
				execute(level, player.getX(), player.getY(), player.getZ(), player);
			boolean result = eventResult;
			eventResult = true;
			return result ? InteractionResult.PASS : InteractionResult.FAIL;
		});

		// Register per-player tick to apply pending cooldown when player lands
		PlayerEvents.END_PLAYER_TICK.register((player) -> {
			if (player == null || player.level().isClientSide())
				return;
			SwordssmpModVariables.PlayerVariables vars = player.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
			if (vars.WindBladeBautaReset > 0 && player.onGround()) {
				ItemStack main = player.getMainHandItem();
				if (main.getItem() == SwordssmpModItems.WIND_BLADE) {
					long now = 0L;
					if (player.level() instanceof Level _lvl)
						now = _lvl.getGameTime();
					player.getCooldowns().addCooldown(main, 100);
					CustomData customData = main.get(DataComponents.CUSTOM_DATA);
					CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
					cooldownTag.putLong("WindBladeCooldownUntil", now + 100L);
					cooldownTag.putString("WindBladeCooldownOwner", player.getStringUUID());
					main.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
				}
				vars.WindBladeBautaReset = 0;
				vars.Djump = 0;
				vars.markSyncDirty();
			}
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		// Sneak to clear the lingering slow falling effect after using the blade
		if (entity.isShiftKeyDown() && entity instanceof LivingEntity _liv && _liv.hasEffect(MobEffects.SLOW_FALLING)) {
			_liv.removeEffect(MobEffects.SLOW_FALLING);
			return;
		}
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() != SwordssmpModItems.WIND_BLADE) {
			return;
		}

		// Read item custom cooldown tag safely
		CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
		CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
		boolean tagChanged = false;
		if (entity instanceof Player player) {
			String ownerId = "";
			if (cooldownTag.contains("WindBladeCooldownOwner"))
				ownerId = cooldownTag.getString("WindBladeCooldownOwner").orElse("");
			if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
				cooldownTag.remove("WindBladeCooldownUntil");
				cooldownTag.remove("WindBladeCooldownOwner");
				tagChanged = true;
			}
		}
		long now = 0L;
		if (world instanceof Level _level) {
			now = _level.getGameTime();
		}
		long cooldownUntil = 0L;
		if (cooldownTag.contains("WindBladeCooldownUntil"))
			cooldownUntil = cooldownTag.getLong("WindBladeCooldownUntil").orElse(0L);
		if (cooldownUntil > 0L) {
			if (now < cooldownUntil) {
				return;
			}
			cooldownTag.remove("WindBladeCooldownUntil");
			cooldownTag.remove("WindBladeCooldownOwner");
			tagChanged = true;
		}
		if (tagChanged) {
			mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
		}

		// use native cooldown as an additional gate for this ability
		if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
			return;

		// Server-side per-player state for Wind Blade extra jumps and pending cooldown
		if (entity instanceof Player player && !player.level().isClientSide()) {
			SwordssmpModVariables.PlayerVariables vars = player.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);
			boolean onGround = player.onGround();

			// If used on ground and no extra-jumps left OR a pending cooldown, apply immediate cooldown
			if (onGround && (vars.Djump <= 0 || vars.WindBladeBautaReset > 0)) {
				// Apply the usual effect then immediate cooldown
				entity.push(0, 1, 0);
				if (world instanceof ServerLevel _srl) {
					_srl.sendParticles(ParticleTypes.CLOUD, x, y, z, 50, 1, 3, 1, 1);
				}
				if (world instanceof Level _lvl) {
					if (!_lvl.isClientSide()) {
						_lvl.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40);
					} else {
						_lvl.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40, false);
					}
				}
				if (entity instanceof LivingEntity livingEntity) {
					livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
				}

				// immediate native cooldown
				player.getCooldowns().addCooldown(player.getMainHandItem(), 100);
				cooldownTag.putLong("WindBladeCooldownUntil", now + 100L);
				cooldownTag.putString("WindBladeCooldownOwner", player.getStringUUID());
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
				// clear pending and remaining jumps so next ground-use reinitializes
				vars.WindBladeBautaReset = 0;
				vars.Djump = 0;
				vars.markSyncDirty();
				return;
			}

			// If used on ground with extra-jumps available, initialize extra-jumps (4) and do effect without native cooldown now
			if (onGround) {
				vars.Djump = 4; // give 4 mid-air uses
				vars.WindBladeBautaReset = 0; // no pending cooldown
				vars.markSyncDirty();

				entity.push(0, 1, 0);
				if (world instanceof ServerLevel _srl) {
					_srl.sendParticles(ParticleTypes.CLOUD, x, y, z, 50, 1, 3, 1, 1);
				}
				if (world instanceof Level _lvl) {
					if (!_lvl.isClientSide()) {
						_lvl.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40);
					} else {
						_lvl.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40, false);
					}
				}
				if (entity instanceof LivingEntity livingEntity) {
					livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
				}
				// do not apply native cooldown yet; it will trigger when extra jumps exhausted and player lands
				return;
			}

			// Airborne use: consume one extra jump if available
			if (!onGround) {
				if (vars.Djump > 0) {
					vars.Djump = vars.Djump - 1;
					if (vars.Djump < 0)
						vars.Djump = 0;
					// If this use exhausted the extra jumps, mark pending cooldown
					if (vars.Djump == 0) {
						vars.WindBladeBautaReset = 1; // pending
					}
					vars.markSyncDirty();

					entity.push(0, 1, 0);
					if (world instanceof ServerLevel _srl) {
						_srl.sendParticles(ParticleTypes.CLOUD, x, y, z, 50, 1, 3, 1, 1);
					}
					if (world instanceof Level _lvl) {
						if (!_lvl.isClientSide()) {
							_lvl.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40);
						} else {
							_lvl.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40, false);
						}
					}
					if (entity instanceof LivingEntity livingEntity) {
						livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
					}
					// native cooldown not applied until landing
					return;
				}
				// no extra jumps available while airborne -> do nothing
				return;
			}
		}

		// Fallback: if not a player or client-side, apply original single-use behavior
		entity.push(0, 1, 0);
		if (world instanceof ServerLevel _level) {
			_level.sendParticles(ParticleTypes.CLOUD, x, y, z, 50, 1, 3, 1, 1);
		}
		if (world instanceof Level _level) {
			if (!_level.isClientSide()) {
				_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40);
			} else {
				_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.armor.equip_elytra")), SoundSource.PLAYERS, 3, 40, false);
			}
		}
		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
		}

		if (entity instanceof Player _player && !_player.level().isClientSide()) {
			_player.getCooldowns().addCooldown(_player.getMainHandItem(), 100);
		}

		cooldownTag.putLong("WindBladeCooldownUntil", now + 100L);
		if (entity instanceof Player player) {
			cooldownTag.putString("WindBladeCooldownOwner", player.getStringUUID());
		}
		mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
	}
}