package name.modid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.damagesource.DamageSource;
import name.modid.ItemDisplayHPGamerule;
import name.modid.SolidDisplays;
import name.modid.ItemDisplayHPManager;

/**
 * Mixin um Item Displays eine Hitbox zu geben und Schaden zu ermöglichen
 */

@Mixin(Entity.class)
public abstract class ItemDisplayHitboxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void addHitbox(CallbackInfoReturnable<net.minecraft.world.phys.AABB> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Display.ItemDisplay)) return;
        Display.ItemDisplay display = (Display.ItemDisplay) self;

        if (ItemDisplayHPGamerule.isEnabled(display.level())) {
            double cx = display.getX();
            double cy = display.getY();
            double cz = display.getZ();
            double half = 0.5d;
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                cx - half, cy - half, cz - half,
                cx + half, cy + half, cz + half
            );
            cir.setReturnValue(box);
        }
    }

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void makePickable(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Display.ItemDisplay)) return;
        Display.ItemDisplay display = (Display.ItemDisplay) self;

        if (ItemDisplayHPGamerule.isEnabled(display.level())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void makeVulnerable(DamageSource source, float amount, CallbackInfo cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Display.ItemDisplay)) return;
        Display.ItemDisplay display = (Display.ItemDisplay) self;

        if (ItemDisplayHPGamerule.isEnabled(display.level())) {
            ItemDisplayHPManager.damageDisplay(display, amount);
            cir.cancel();
        }
    }
}
