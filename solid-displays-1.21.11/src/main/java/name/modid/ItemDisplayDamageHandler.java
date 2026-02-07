package name.modid;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;

/**
 * Behandelt Schaden an Item Displays durch Angriffe
 */
public class ItemDisplayDamageHandler {
    
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Nur auf Server-Seite
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }
            
            // Prüfe ob das Ziel ein Item Display ist
            if (!(entity instanceof Display.ItemDisplay)) {
                return InteractionResult.PASS;
            }
            
            Display.ItemDisplay display = (Display.ItemDisplay) entity;
            
            // Prüfe ob GameRule aktiviert ist
            if (!ItemDisplayHPGamerule.isEnabled(world)) {
                return InteractionResult.PASS;
            }
            
            // Berechne Schaden vom Spieler
            float damage = calculateDamage(player);
            
            // Füge Schaden hinzu
            ItemDisplayHPManager.damageDisplay(display, damage);
            
            // Spiele Treffereffekte ab
            playHurtEffects(display, world);
            
            // Verhindere weitere Verarbeitung
            return InteractionResult.SUCCESS;
        });
    }
    
    /**
     * Berechnet Schaden basierend auf dem Weapon des Spielers
     */
    private static float calculateDamage(Player player) {
        ItemStack weapon = player.getMainHandItem();
        
        // Base damage vom Spieler
        float baseDamage = 1.0f; // Faust-Schaden
        
        // Hole attack damage attribute vom Item
        var attackDamage = weapon.getAttributeModifiers()
            .get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        
        if (attackDamage != null && !attackDamage.isEmpty()) {
            // Summiere alle Damage Modifier
            for (var modifier : attackDamage) {
                baseDamage += modifier.amount();
            }
        }
        
        // Kritischer Treffer wenn Spieler fällt
        if (player.fallDistance > 0.0f && !player.onGround() && !player.isInWater()) {
            baseDamage *= 1.5f;
        }
        
        return baseDamage;
    }
    
    /**
     * Spielt Schadeneffekte ab (Sound und Partikel)
     */
    private static void playHurtEffects(Display.ItemDisplay display, net.minecraft.world.level.Level world) {
        if (world instanceof ServerLevel serverLevel) {
            // Spiele Treffergeräusch
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
            serverLevel.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                display.getX(),
                display.getY() + 0.5,
                display.getZ(),
                3, // Anzahl der Partikel
                0.1, 0.1, 0.1, // Spread
                0.0 // Speed
            );
        }
    }
}
