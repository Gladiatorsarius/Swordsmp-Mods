package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatHeadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class PlayerHeadInteractionMixin {
    
    @Inject(
        method = "useWithoutItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onHeadInteraction(Level world, Player player, BlockHitResult hit,
                                   CallbackInfoReturnable<InteractionResult> cir) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockState state = (BlockState) (Object) this;
            if (!state.is(Blocks.PLAYER_HEAD) && !state.is(Blocks.PLAYER_WALL_HEAD)) {
                return;
            }

            BlockPos pos = hit.getBlockPos();
            CombatHeadManager headManager = CombatHeadManager.getInstance();

            if (headManager.isHeadLocation(pos)) {
                if (!headManager.canAccess(pos, serverPlayer)) {
                    serverPlayer.displayClientMessage(
                        Component.literal("§c§lYou cannot access this combat log head yet!"),
                        true
                    );
                    cir.setReturnValue(InteractionResult.FAIL);
                } else {
                    // Allow access - could open inventory GUI here in future
                    serverPlayer.sendSystemMessage(
                        Component.literal("§a§lAccess granted to combat log head")
                    );
                    // For now, just allow normal interaction
                }
            }
        }
    }
}
