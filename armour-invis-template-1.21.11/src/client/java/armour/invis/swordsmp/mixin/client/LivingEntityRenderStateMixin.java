package armour.invis.swordsmp.mixin.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import armour.invis.swordsmp.accessor.InvisibilityAmplifierAccessor;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements InvisibilityAmplifierAccessor {
	@Unique
	private int armourInvis$invisibilityAmplifier = -1;

	@Override
	public int armourInvis$getInvisibilityAmplifier() {
		return this.armourInvis$invisibilityAmplifier;
	}

	@Override
	public void armourInvis$setInvisibilityAmplifier(int amplifier) {
		this.armourInvis$invisibilityAmplifier = amplifier;
	}
}
