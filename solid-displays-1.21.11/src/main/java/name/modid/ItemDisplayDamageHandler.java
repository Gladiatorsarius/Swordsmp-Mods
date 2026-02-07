package name.modid;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.decoration.ArmorStand;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;

/**
 * Handles damage to Item Displays caused by player attacks
 */
public class ItemDisplayDamageHandler {
    
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Only on the server side
            if (world.isClientSide()) return InteractionResult.PASS;

            // If the target is a proxy ArmorStand, route damage to its display
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                UUID displayUuid = ItemDisplayHPManager.getDisplayUuidForProxy(stand.getUUID());
                if (displayUuid == null) return InteractionResult.PASS;

                Display.ItemDisplay display = ItemDisplayHPManager.getDisplayByUuid(world, displayUuid);
                if (display == null) return InteractionResult.PASS;

                if (!ItemDisplayHPGamerule.isEnabled(world)) return InteractionResult.PASS;

                float damage = calculateDamage(player);
                ItemDisplayHPManager.damageDisplay(display, damage);
                playHurtEffects(display, world);
                return InteractionResult.SUCCESS;
            }

            // Otherwise handle direct ItemDisplay hits
            if (!(entity instanceof Display.ItemDisplay)) return InteractionResult.PASS;
            Display.ItemDisplay display = (Display.ItemDisplay) entity;
            if (!ItemDisplayHPGamerule.isEnabled(world)) return InteractionResult.PASS;
            float damage = calculateDamage(player);
            ItemDisplayHPManager.damageDisplay(display, damage);
            playHurtEffects(display, world);
            return InteractionResult.SUCCESS;
        });
    }
    
    /**
     * Calculate damage based on the player's weapon and attributes
     */
    private static float calculateDamage(Player player) {
        ItemStack weapon = player.getMainHandItem();

        // Base damage from player: use the player's attack damage attribute (includes weapon)
        float baseDamage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        
        // Critical hit when player is falling
        if (player.fallDistance > 0.0f && !player.onGround() && !player.isInWater()) {
            baseDamage *= 1.5f;
        }
        
        return baseDamage;
    }
    
    /**
     * Play hurt effects (sound and particles)
     */
    private static void playHurtEffects(Display.ItemDisplay display, net.minecraft.world.level.Level world) {
        if (world instanceof ServerLevel serverLevel) {
            // Spiele Treffergeräusch
            // Play hit sound
            world.playSound(
                null,
                display.getX(),
                display.getY(),
                display.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
            );
            
            // Zeige Schadenpartikel
            // Show damage particles
            serverLevel.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                display.getX(),
                display.getY() + 0.5,
                display.getZ(),
                3, // particle count
                0.1, 0.1, 0.1, // spread
                0.0 // speed
            );
        }
    }

    
}
