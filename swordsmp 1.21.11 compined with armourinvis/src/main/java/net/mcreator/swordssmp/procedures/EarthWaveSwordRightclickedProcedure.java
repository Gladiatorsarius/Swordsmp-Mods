package net.mcreator.swordssmp.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.swordssmp.network.SwordssmpModVariables;
import net.mcreator.swordssmp.init.SwordssmpModItems;
import net.mcreator.swordssmp.SwordssmpMod;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class EarthWaveSwordRightclickedProcedure {
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
			CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
			CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
			boolean tagChanged = false;
			if (entity instanceof Player player) {
				String ownerId = cooldownTag.getString("EarthWaveCooldownOwner").orElse("");
				if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
					cooldownTag.remove("EarthWaveCooldownUntil");
					cooldownTag.remove("EarthWaveCooldownOwner");
					tagChanged = true;
				}
			}
			long now = 0L;
			if (world instanceof Level _level) {
				now = _level.getGameTime();
			}
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
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
			// block if native cooldown overlay is active
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack)) {
				return;
			}
			// execute ability (no per-player variable gating)
			{
				// Summon expanding rings of dripstone block displays from inside out
				if (world instanceof ServerLevel _level) {
					// Spawn 5 rings with increasing radius, each spawning with a delay
					for (int ringIndex = 0; ringIndex < 5; ringIndex++) {
						final int ring = ringIndex;
						final double radius = (ringIndex + 1) * 1.5; // Rings at radius 1.5, 3.0, 4.5, 6.0, 7.5
						final int blocksPerRing = 8 + (ringIndex * 4); // More blocks in outer rings
						
						SwordssmpMod.queueServerWork(ringIndex * 2, () -> {
							for (int i = 0; i < blocksPerRing; i++) {
								double angle = Math.toRadians(i * (360.0 / blocksPerRing));
								double dx = Math.cos(angle) * radius;
								double dz = Math.sin(angle) * radius;
								double sx = x + dx;
								double sz = z + dz;
								String cmd = String.format(java.util.Locale.ROOT, "/summon block_display %.3f %.3f %.3f {block_state:{Name:\"minecraft:pointed_dripstone\"}}", sx, y, sz);
								_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(sx, y, sz), Vec2.ZERO, _level, PermissionSet.ALL_PERMISSIONS, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
										cmd);
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
						for (Display.BlockDisplay display : _level.getEntitiesOfClass(Display.BlockDisplay.class, area.inflate(4))) {
							display.discard();
						}
					});
				}
				SwordssmpMod.queueServerWork(1, () -> {
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.mace.smash_ground")), SoundSource.NEUTRAL, 3, 1);
						} else {
							_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("item.mace.smash_ground")), SoundSource.NEUTRAL, 3, 1, false);
						}
					}
				});
				// add native cooldown overlay
				if (entity instanceof Player _player && !_player.level().isClientSide()) {
					_player.getCooldowns().addCooldown(_player.getMainHandItem(), 400);
				}

				cooldownTag.putLong("EarthWaveCooldownUntil", now + 400L);
				if (entity instanceof Player player) {
					cooldownTag.putString("EarthWaveCooldownOwner", player.getStringUUID());
				}
				mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
			}
		}
	}

	// Solve for initial upward velocity so vanilla drag/gravity (~0.08, drag 0.98) reaches the target height.
	private static double computeLaunchVelocity(double targetHeight) {
		double low = 0;
		double high = 10;
		for (int i = 0; i < 24; i++) {
			double mid = (low + high) * 0.5;
			double height = simulatedApex(mid);
			if (height >= targetHeight) {
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
