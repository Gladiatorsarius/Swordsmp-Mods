package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.core.BlockPos;

import net.minecraft.server.MinecraftServer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.util.datafix.DataFixTypes;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class TNTSword2RightclickedOnBlockProcedure {
	// Ground tick counter: short-lived runtime state, recomputed from the still-live
	// entity after a restart, so it stays in memory.
	public static final Map<UUID, Integer> TNT_GROUND_TICKS = new HashMap<>();

	// The set of UUIDs belonging to TNT Sword 2's thrown TNT lives in per-world
	// SavedData: the thrown PrimedTnt itself survives a server restart via its NBT, so
	// the tracking has to survive too. Before this, the set was a static in-memory
	// field - after a restart a parked TNT lost its landing detonation and eventually
	// exploded at normal size.
	public static class BigTntUuids extends SavedData {
		public final Set<UUID> uuids;

		public BigTntUuids(Set<UUID> uuids) {
			this.uuids = uuids;
		}
	}

	private static final SavedDataType<BigTntUuids> BIG_TNT_TYPE = new SavedDataType<>("swordssmp_tntsword2_big_tnts", () -> new BigTntUuids(new HashSet<>()),
			Codec.list(UUIDUtil.CODEC).xmap(uuids -> new BigTntUuids(new HashSet<>(uuids)), data -> java.util.List.copyOf(data.uuids)), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private static BigTntUuids data(MinecraftServer server) {
		DimensionDataStorage storage = server.overworld().getDataStorage();
		return storage.computeIfAbsent(BIG_TNT_TYPE);
	}

	// Looked up by PrimedTntMixin right before the TNT explodes.
	public static boolean isBigTnt(MinecraftServer server, UUID uuid) {
		return data(server).uuids.contains(uuid);
	}

	public static void consumeBigTnt(MinecraftServer server, UUID uuid) {
		BigTntUuids data = data(server);
		if (data.uuids.remove(uuid)) {
			data.setDirty();
		}
		TNT_GROUND_TICKS.remove(uuid);
	}

	public TNTSword2RightclickedOnBlockProcedure() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			BigTntUuids data = data(server);
			boolean dirty = false;
			for (UUID uuid : new HashSet<>(data.uuids)) {
				PrimedTnt tnt = null;
				for (ServerLevel level : server.getAllLevels()) {
					if (level.getEntity(uuid) instanceof PrimedTnt primedTnt) {
						tnt = primedTnt;
						break;
					}
				}

				// Not found usually means its chunk is not loaded right now (a UUID lookup
				// only sees loaded entities) - keep the entry: vanilla freezes the entity
				// with the chunk and resumes it on load. Purging here would silently drop
				// the tracking on every restart outside spawn-force-loaded chunks.
				if (tnt == null) {
					continue;
				}
				if (!tnt.isAlive()) {
					data.uuids.remove(uuid);
					TNT_GROUND_TICKS.remove(uuid);
					dirty = true;
					continue;
				}

				if (tnt.onGround()) {
					int ticks = TNT_GROUND_TICKS.getOrDefault(uuid, 0) + 1;
					if (ticks >= 5) {
						tnt.setFuse(0);
						TNT_GROUND_TICKS.remove(uuid);
					} else {
						TNT_GROUND_TICKS.put(uuid, ticks);
					}
				} else {
					TNT_GROUND_TICKS.put(uuid, 0);
					tnt.setFuse(99999);
				}
			}
			if (dirty) {
				data.setDirty();
			}
		});
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		ItemStack mainHandStack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
		if (mainHandStack.getItem() == SwordssmpModItems.TNT_SWORD_2) {
			CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
			boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "TNTSword2", entity);
			long now = ProcedureUtils.gameTime(world);
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
				ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
			}
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
				return;
			ProcedureUtils.playBlockSound(world, x, y, z, "entity.tnt.primed", SoundSource.NEUTRAL, 50, 1);
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
					BigTntUuids data = data(serverLevel.getServer());
					data.uuids.add(tnt.getUUID());
					data.setDirty();
				}
			}
			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "TNTSword2", entity, now, 300L);
		}
	}
}
