package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.Iterator;
import java.util.Map.Entry;

public class TNTSword2RightclickedOnBlockProcedure {
	public static final Map<UUID, Integer> TNT_GROUND_TICKS = new HashMap<>();
	public static final Set<UUID> BIG_TNT_UUIDS = new HashSet<>();

	public TNTSword2RightclickedOnBlockProcedure() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<Entry<UUID, Integer>> iterator = TNT_GROUND_TICKS.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<UUID, Integer> entry = iterator.next();
				UUID uuid = entry.getKey();

				PrimedTnt tnt = null;
				for (ServerLevel level : server.getAllLevels()) {
					Entity entity = level.getEntity(uuid);
					if (entity instanceof PrimedTnt primedTnt) {
						tnt = primedTnt;
						break;
					}
				}

				if (tnt == null || !tnt.isAlive()) {
					iterator.remove();
					BIG_TNT_UUIDS.remove(uuid);
					continue;
				}

				if (tnt.onGround()) {
					int ticks = entry.getValue() + 1;
					if (ticks >= 5) {
						tnt.setFuse(0);
						iterator.remove();
					} else {
						entry.setValue(ticks);
					}
				} else {
					entry.setValue(0);
					tnt.setFuse(99999);
				}
			}
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() == SwordssmpModItems.TNT_SWORD_2) {
			CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
			boolean tagChanged = false;
			if (entity instanceof Player player) {
				String ownerId = cooldownTag.getString("TNTSword2CooldownOwner").orElse("");
				if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
					cooldownTag.remove("TNTSword2CooldownUntil");
					cooldownTag.remove("TNTSword2CooldownOwner");
					tagChanged = true;
				}
			}
			long now = 0L;
			if (world instanceof Level _level) {
				now = _level.getGameTime();
			}
			long cooldownUntil = cooldownTag.getLong("TNTSword2CooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("TNTSword2CooldownUntil");
				cooldownTag.remove("TNTSword2CooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
				return;
			{
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.tnt.primed")), SoundSource.NEUTRAL, 50, 1);
					} else {
						_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.tnt.primed")), SoundSource.NEUTRAL, 50, 1, false);
					}
				}
				if (world instanceof ServerLevel serverLevel && entity instanceof Player player) {
					Vec3 lookAngle = player.getLookAngle();
					Vec3 spawnPos = player.getEyePosition().add(lookAngle.scale(0.5));
					PrimedTnt tnt = EntityType.TNT.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
					if (tnt != null) {
						tnt.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
						tnt.setDeltaMovement(lookAngle.x * 2.5, lookAngle.y * 2.5, lookAngle.z * 2.5);
						tnt.setFuse(99999);
						serverLevel.addFreshEntity(tnt);
						TNT_GROUND_TICKS.put(tnt.getUUID(), 0);
						BIG_TNT_UUIDS.add(tnt.getUUID());
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getMainHandItem(), 300);
				}

				cooldownTag.putLong("TNTSword2CooldownUntil", now + 300L);
				if (entity instanceof Player player) {
					cooldownTag.putString("TNTSword2CooldownOwner", player.getStringUUID());
				}
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
		}
	}
}
