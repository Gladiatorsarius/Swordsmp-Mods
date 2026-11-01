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

import net.mcreator.swordssmp.init.SwordssmpModItems;

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
			CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
			boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "GhostBlade", entity);
			long now = ProcedureUtils.gameTime(world);
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
				ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
			}
			// native cooldown guard
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
				return;

			// perform dash (no PlayerVariables gating)
			entity.push((entity.getLookAngle().x * 2), 0.2, (entity.getLookAngle().z * 2));

			// Ambient cave sound on activation with slight pitch randomization
			if (world instanceof Level _level) {
				float pitch = 0.7f + (_level.getRandom().nextFloat() * 0.6f);
				ProcedureUtils.playSound(world, x, y, z, "ambient.cave", SoundSource.PLAYERS, 1.2f, pitch);
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

			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "GhostBlade", entity, now, 200L);
		}
	}
}
