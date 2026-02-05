package net.mcreator.swordssmp.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
	public void drop(boolean dropStack, CallbackInfo ci) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		Inventory inventory = self.getInventory();
		ItemStack itemstack = inventory.removeFromSelected(dropStack);
		self.containerMenu.findSlot(inventory, inventory.getSelectedSlot()).ifPresent(p_401732_ -> self.containerMenu.setRemoteSlot(p_401732_, inventory.getSelectedItem()));
		self.drop(itemstack, false, true);
		ci.cancel();
	}
}
