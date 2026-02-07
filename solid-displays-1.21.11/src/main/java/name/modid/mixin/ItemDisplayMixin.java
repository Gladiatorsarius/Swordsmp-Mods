package name.modid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import name.modid.ItemDisplayHpData;
import name.modid.ItemDisplayHPManager;
import name.modid.ItemDisplayHPGamerule;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.Display;

/**
 * Mixin für Item Display Konstruktor
 */
@Mixin(Display.ItemDisplay.class)
public class ItemDisplayMixin implements ItemDisplayHpData {

    @Unique
    private boolean solidDisplays$hasData;

    @Unique
    private float solidDisplays$hp;

    @Unique
    private float solidDisplays$maxHp;
    
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Item Display wurde erstellt, lade NBT-Daten
        Display.ItemDisplay display = (Display.ItemDisplay) (Object) this;
        // Only initialize HP system if gamerule enabled
        // registration/loading will occur after NBT is read (see readAdditionalSaveData)
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onSave(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("solidDisplaysHasData", solidDisplays$hasData);
        if (solidDisplays$hasData) {
            output.putFloat("solidDisplaysHp", solidDisplays$hp);
            output.putFloat("solidDisplaysMaxHp", solidDisplays$maxHp);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onLoad(ValueInput input, CallbackInfo ci) {
        solidDisplays$hasData = input.getBooleanOr("solidDisplaysHasData", false);
        if (solidDisplays$hasData) {
            solidDisplays$hp = input.getFloatOr("solidDisplaysHp", 0.0F);
            solidDisplays$maxHp = input.getFloatOr("solidDisplaysMaxHp", 0.0F);
        }
        // After NBT has been read, register or load HP so we capture the entity's ItemStack
        try {
            Display.ItemDisplay display = (Display.ItemDisplay) (Object) this;
            if (!ItemDisplayHPGamerule.isEnabled(display.level())) return;

            // New behavior: if the entity NBT contains a boolean "solid":true then
            // treat this display as a solid display — register it (server-side)
            // and spawn an ArmorStand proxy with a 1x1 hitbox.
            boolean solidTag = input.getBooleanOr("solid", false);
            if (solidTag) {
                // register with default HP (100) so the manager can drop the item on proxy death
                if (!display.level().isClientSide()) {
                    ItemDisplayHPManager.registerDisplay(display, 100.0f);
                    ItemDisplayHPManager.spawnProxyForSolid(display);
                }
            } else if (solidDisplays$hasData) {
                // backward compatibility: if we have explicit HP NBT, load it
                ItemDisplayHPManager.loadFromNBT(display);
                ItemDisplayHPManager.spawnProxyIfTagged(display);
            }
        } catch (Throwable t) {
            // non-fatal
        }
    }

    @Override
    public boolean solidDisplays$hasData() {
        return solidDisplays$hasData;
    }

    @Override
    public float solidDisplays$getHp() {
        return solidDisplays$hp;
    }

    @Override
    public float solidDisplays$getMaxHp() {
        return solidDisplays$maxHp;
    }

    @Override
    public void solidDisplays$setData(float hp, float maxHp) {
        solidDisplays$hp = hp;
        solidDisplays$maxHp = maxHp;
        solidDisplays$hasData = true;
    }

    @Override
    public void solidDisplays$clearData() {
        solidDisplays$hasData = false;
    }
}
