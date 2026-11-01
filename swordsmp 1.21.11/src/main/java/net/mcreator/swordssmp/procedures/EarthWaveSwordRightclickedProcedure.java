package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class EarthWaveSwordRightclickedProcedure {
	private static final java.util.concurrent.atomic.AtomicLong CAST_COUNTER = new java.util.concurrent.atomic.AtomicLong();
	public static boolean eventResult = true;

	public EarthWaveSwordRightclickedProcedure() {
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
		if (mainHandStack.getItem() == SwordssmpModItems.EARTH_WAVE_SWORD) {
			CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
			boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "EarthWave", entity);
			long now = ProcedureUtils.gameTime(world);
			long cooldownUntil = cooldownTag.getLong("EarthWaveCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("EarthWaveCooldownUntil");
				cooldownTag.remove("EarthWaveCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
			}
			// block if native cooldown overlay is active
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack)) {
				return;
			}
			// execute ability (no per-player variable gating)
			if (world instanceof ServerLevel _level) {
				// Unique tag per cast so the cleanup only removes THIS cast's displays - a
				// simultaneous second caster's displays were previously discarded early too.
				final String castTag = "swordssmp_earthwave_cast_" + CAST_COUNTER.getAndIncrement();
				// Summon expanding rings of dripstone block displays from inside out
				for (int ringIndex = 0; ringIndex < 5; ringIndex++) {
					final int ring = ringIndex;
					final double radius = (ringIndex + 1) * 1.5; // Rings at radius 1.5, 3.0, 4.5, 6.0, 7.5
					final int blocksPerRing = 8 + (ringIndex * 4); // More blocks in outer rings

					SwordssmpMod.queueServerWork(ringIndex * 2, () -> {
						for (int i = 0; i < blocksPerRing; i++) {
							double angle = Math.toRadians(i * (360.0 / blocksPerRing));
							double sx = x + Math.cos(angle) * radius;
							double sz = z + Math.sin(angle) * radius;
							String cmd = String.format(java.util.Locale.ROOT, "/summon block_display %.3f %.3f %.3f {block_state:{Name:\"minecraft:pointed_dripstone\"},Tags:[\"swordssmp_earthwave\",\"" + castTag + "\"]}", sx, y, sz);
							ProcedureUtils.runCommand(_level, sx, y, sz, cmd);
						}
					});
				}

				// Catapult everything in radius upward toward ~25 blocks high
				double launchVelocity = computeLaunchVelocity(25.0);
				AABB area = new AABB(x - 9, y - 2, z - 9, x + 9, y + 6, z + 9);
				for (Entity target : _level.getEntities((Entity) null, area, e -> !(e instanceof Display.BlockDisplay) && e != entity)) {
					Vec3 motion = target.getDeltaMovement();
					target.setDeltaMovement(motion.x, launchVelocity, motion.z);
					target.hurtMarked = true;
					if (target instanceof LivingEntity _living) {
						_living.fallDistance = 0;
					}
				}

				// Clean up the spawned displays shortly after
				SwordssmpMod.queueServerWork(30, () -> {
					for (Display.BlockDisplay display : _level.getEntitiesOfClass(Display.BlockDisplay.class, area.inflate(4), e -> e.getTags().contains(castTag))) {
						display.discard();
					}
				});
			}
			SwordssmpMod.queueServerWork(1, () -> ProcedureUtils.playBlockSound(world, x, y, z, "item.mace.smash_ground", SoundSource.NEUTRAL, 3, 1));

			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "EarthWave", entity, now, 400L);
		}
	}

	// Solve for initial upward velocity so vanilla drag/gravity (~0.08, drag 0.98) reaches the target height.
	private static double computeLaunchVelocity(double targetHeight) {
		double low = 0;
		double high = 10;
		for (int i = 0; i < 24; i++) {
			double mid = (low + high) * 0.5;
			if (simulatedApex(mid) >= targetHeight) {
				high = mid;
			} else {
				low = mid;
			}
		}
		return high;
	}

	private static double simulatedApex(double initialVelocity) {
		double y = 0;
		double vy = initialVelocity;
		while (vy > 0) {
			y += vy;
			vy = (vy - 0.08) * 0.98;
		}
		return y;
	}
}
