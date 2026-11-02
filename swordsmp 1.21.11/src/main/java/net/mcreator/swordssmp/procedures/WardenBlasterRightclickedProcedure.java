package net.mcreator.swordssmp.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import net.mcreator.swordssmp.init.SwordssmpModItems;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class WardenBlasterRightclickedProcedure {
	public static boolean eventResult = true;

	public WardenBlasterRightclickedProcedure() {
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
		if (mainHandStack.getItem() == SwordssmpModItems.WARDEN_BLASTER) {
			CompoundTag cooldownTag = ProcedureUtils.getCustomTag(mainHandStack);
			boolean tagChanged = ProcedureUtils.resetCooldownIfOwnerChanged(cooldownTag, "WardenBlaster", entity);
			long now = ProcedureUtils.gameTime(world);
			long cooldownUntil = cooldownTag.getLong("WardenBlasterCooldownUntil").orElse(0L);
			if (cooldownUntil > 0L) {
				if (now < cooldownUntil) {
					return;
				}
				cooldownTag.remove("WardenBlasterCooldownUntil");
				cooldownTag.remove("WardenBlasterCooldownOwner");
				tagChanged = true;
			}
			if (tagChanged) {
				ProcedureUtils.saveCustomTag(mainHandStack, cooldownTag);
			}
			// native cooldown guard
			if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
				return;
			ProcedureUtils.playBlockSound(world, x, y, z, "entity.warden.sonic_charge", SoundSource.PLAYERS, 3, 1);
			ProcedureUtils.playBlockSound(world, x, y, z, "entity.warden.sonic_boom", SoundSource.PLAYERS, 3, 1);
			if (world instanceof ServerLevel _level) {
				// Summon area_effect_cloud entities in the player's look direction
				Vec3 look = (entity instanceof LivingEntity _le) ? _le.getLookAngle() : new Vec3(0, 0, 1);
				for (int i = 1; i <= 10; i++) {
					double px = x + look.x * i;
					double py = y + 1;
					double pz = z + look.z * i;
					int amplifier = (i <= 3) ? 4 : 3;
					int effectDuration = (i == 10) ? 1 : 2;

					AreaEffectCloud cloud = new AreaEffectCloud(_level, px, py, pz);
					cloud.setRadius(1f);
					cloud.setDuration(20);
					cloud.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, effectDuration, amplifier, false, false));
					_level.addFreshEntity(cloud);

					_level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 1);
				}
			}
			ProcedureUtils.startCooldown(mainHandStack, cooldownTag, "WardenBlaster", entity, now, 600L);
		}
	}
}
