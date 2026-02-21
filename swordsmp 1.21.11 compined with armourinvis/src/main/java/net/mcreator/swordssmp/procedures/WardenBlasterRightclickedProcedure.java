package net.mcreator.swordssmp.procedures;

// ListTag no longer required; using direct AreaEffectCloud API
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import net.mcreator.swordssmp.init.SwordssmpModItems;


import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class WardenBlasterRightclickedProcedure {
    public static boolean eventResult = true;
// Removed unused ListTag import; using direct AreaEffectCloud API instead

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
            CustomData customData = mainHandStack.get(DataComponents.CUSTOM_DATA);
            CompoundTag cooldownTag = customData != null ? customData.copyTag() : new CompoundTag();
            boolean tagChanged = false;
            if (entity instanceof Player player) {
                String ownerId = cooldownTag.getString("WardenBlasterCooldownOwner").orElse("");
                if (!ownerId.isEmpty() && !ownerId.equals(player.getStringUUID())) {
                    cooldownTag.remove("WardenBlasterCooldownUntil");
                    cooldownTag.remove("WardenBlasterCooldownOwner");
                    tagChanged = true;
                }
            }
            long now = 0L;
            if (world instanceof Level _level) {
                now = _level.getGameTime();
            }
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
                mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
            }
            // native cooldown guard
            if (entity instanceof Player _player && _player.getCooldowns().isOnCooldown(mainHandStack))
                return;
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_charge")), SoundSource.PLAYERS, 3, 1);
                } else {
                    _level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_charge")), SoundSource.PLAYERS, 3, 1, false);
                }
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_boom")), SoundSource.PLAYERS, 3, 1);
                } else {
                    _level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("entity.warden.sonic_boom")), SoundSource.PLAYERS, 3, 1, false);
                }
            }
            if (world instanceof ServerLevel _level) {
                // Summon area_effect_cloud entities in the player's look direction
                Vec3 look = (entity instanceof LivingEntity _le) ? _le.getLookAngle() : new Vec3(0, 0, 1);
                for (int i = 1; i <= 10; i++) {
                    double px = x + look.x * i;
                    double py = y + 1;
                    double pz = z + look.z * i;
                    float radius = 1f;
                    int duration = 20;
                    int amplifier = (i <= 3) ? 4 : 3;
                    int effectDuration = (i == 10) ? 1 : 2;

                    net.minecraft.world.entity.AreaEffectCloud cloud = new net.minecraft.world.entity.AreaEffectCloud(_level, px, py, pz);
                    cloud.setRadius(radius);
                    cloud.setDuration(duration);
                    cloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.INSTANT_DAMAGE, effectDuration, amplifier, false, false));
                    _level.addFreshEntity(cloud);

                    // Spawn visible particle at the cloud position
                    _level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 1);
                }
            }
            // Add native cooldown overlay
            if (entity instanceof Player _player && !_player.level().isClientSide()) {
                _player.getCooldowns().addCooldown(_player.getMainHandItem(), 600);
            }

            cooldownTag.putLong("WardenBlasterCooldownUntil", now + 600L);
            if (entity instanceof Player player) {
                cooldownTag.putString("WardenBlasterCooldownOwner", player.getStringUUID());
            }
            mainHandStack.set(DataComponents.CUSTOM_DATA, CustomData.of(cooldownTag));
        }
    }
}

