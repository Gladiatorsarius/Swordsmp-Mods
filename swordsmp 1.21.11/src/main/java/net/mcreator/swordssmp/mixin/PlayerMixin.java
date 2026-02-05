package net.mcreator.swordssmp.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.player.Player;

import net.mcreator.swordssmp.event.PlayerEvents;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void playerTick(CallbackInfo ci) {
		PlayerEvents.END_PLAYER_TICK.invoker().onEndTick((Player) (Object) this);
	}

	@Inject(method = "giveExperiencePoints", at = @At("HEAD"))
	private void playerGiveExperiencePoints(int amount, CallbackInfo ci) {
		PlayerEvents.XP_CHANGE.invoker().onXpChange((Player) (Object) this, amount);
	}

	@Inject(method = "giveExperienceLevels", at = @At("HEAD"))
	private void playerGiveExperienceLevels(int amount, CallbackInfo ci) {
		PlayerEvents.LEVEL_CHANGE.invoker().onLevelChange((Player) (Object) this, amount);
	}
}
