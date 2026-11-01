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
					if (level.getEntity(uuid) instanceof PrimedTnt primedTnt) {
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
					BIG_TNT_UUIDS.add(tnt.getUUID());
				}
			}
			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "TNTSword2", entity, now, 300L);
		}
	}
}
