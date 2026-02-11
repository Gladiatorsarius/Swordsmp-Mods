package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
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

		CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
		CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
		boolean tagChanged = false;

		if (entity instanceof Player player) {
			String ownerId = cooldownTag.getString("WindBladeCooldownOwner").orElse("");
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
		long cooldownUntil = cooldownTag.getLong("WindBladeCooldownUntil").orElse(0L);
		if (cooldownUntil > 0L && now < cooldownUntil) {
			return;
		}
		if (cooldownUntil > 0L) {
			cooldownTag.remove("WindBladeCooldownUntil");
			cooldownTag.remove("WindBladeCooldownOwner");
			tagChanged = true;
		}
		if (tagChanged) {
			mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
		}

		SwordssmpModVariables.PlayerVariables _vars = entity.getAttachedOrCreate(SwordssmpModVariables.PLAYER_VARIABLES);

		if (_vars.WindBladeCooldown == 0 && _vars.Djump < 4) {
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
			if (entity instanceof LivingEntity _livEnt) {
				_livEnt.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 1)); // 10 seconds
			}
			_vars.Djump++;
			_vars.markSyncDirty();
		} else if (_vars.Djump == 4 && _vars.WindBladeBautaReset == 0) {
			if (world.getBlockState(BlockPos.containing(x, y - 1, z)).canOcclude()) {
				_vars.WindBladeBautaReset = 1;
				_vars.markSyncDirty();

				// Native cooldown after 4 jumps
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getMainHandItem(), 100);
				}
				cooldownTag.putLong("WindBladeCooldownUntil", now + 100L);
				if (entity instanceof Player player) {
					cooldownTag.putString("WindBladeCooldownOwner", player.getStringUUID());
				}
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));

				// Reset state after cooldown
				SwordssmpMod.queueServerWork(100, () -> {
					_vars.Djump = 0;
					_vars.WindBladeBautaReset = 0;
					_vars.markSyncDirty();
				});
			}
		}
	}
}
