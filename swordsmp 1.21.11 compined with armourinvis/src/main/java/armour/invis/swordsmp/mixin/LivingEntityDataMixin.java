package armour.invis.swordsmp.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;

import armour.invis.swordsmp.accessor.ArmourInvisEntityAccessor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDataMixin implements ArmourInvisEntityAccessor {
    private static final EntityDataAccessor<Integer> ARMOUR_INVIS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);

    @Shadow
    public abstract SynchedEntityData getEntityData();

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void armourInvis$defineSynchedData(Builder builder, CallbackInfo ci) {
        try {
            builder.define(ARMOUR_INVIS, 0);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public int armourInvis$getTrackedAmplifier() {
        try {
            return this.getEntityData().get(ARMOUR_INVIS);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    @Override
    public void armourInvis$setTrackedAmplifier(int amplifier) {
        try {
            this.getEntityData().set(ARMOUR_INVIS, amplifier);
        } catch (Throwable ignored) {
        }
    }
}
