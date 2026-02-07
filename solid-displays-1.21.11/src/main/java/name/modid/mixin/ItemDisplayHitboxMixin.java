package name.modid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.damagesource.DamageSource;
import name.modid.ItemDisplayHPGamerule;
import name.modid.ItemDisplayHPManager;

/**
 * Mixin um Item Displays eine Hitbox zu geben und Schaden zu ermöglichen
 */
@Mixin(Display.ItemDisplay.class)
public abstract class ItemDisplayHitboxMixin {
    
    /**
     * Gibt ItemDisplays eine physische Hitbox
     */
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void addHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        Display.ItemDisplay display = (Display.ItemDisplay) (Object) this;
        
        // Nur wenn GameRule aktiviert ist
        if (ItemDisplayHPGamerule.isEnabled(display.level())) {
            // Größere Hitbox basierend auf dem Item (durchschnittliche Item-Größe)
            // Standard Minecraft Items sind etwa 0.5-1.0 Blöcke groß
            EntityDimensions dimensions = EntityDimensions.fixed(0.75f, 0.75f);
            cir.setReturnValue(dimensions);
        }
    }
    
    /**
     * Macht ItemDisplays verletzbar
     */
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void makeVulnerable(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Display.ItemDisplay display = (Display.ItemDisplay) (Object) this;
        
        // Nur wenn GameRule aktiviert ist
        if (ItemDisplayHPGamerule.isEnabled(display.level())) {
            // Erlaube Schaden
            ItemDisplayHPManager.damageDisplay(display, amount);
            cir.setReturnValue(true);
        }
    }
}
