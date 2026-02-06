package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatHeadManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockBreakMixin {
    
    @Inject(method = "playerWillDestroy", at = @At("HEAD"), cancellable = true)
    private void onBlockBreak(Level world, BlockPos pos, BlockState state, Player player, 
                             CallbackInfoReturnable<BlockState> cir) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Check if this is a player head
            if (state.is(Blocks.PLAYER_HEAD) || state.is(Blocks.PLAYER_WALL_HEAD)) {
                CombatHeadManager headManager = CombatHeadManager.getInstance();
                
                if (headManager.isHeadLocation(pos)) {
                    if (!headManager.canAccess(pos, serverPlayer)) {
                        serverPlayer.displayClientMessage(
                            Component.literal("§c§lYou cannot break this combat log head yet!"),
                            true
                        );
                        // Cancel the break
                        cir.setReturnValue(state);
                    } else {
                        // Allow breaking and remove from tracking
                        headManager.removeHead(pos);
                        serverPlayer.sendSystemMessage(
                            Component.literal("§e§lCombat log head destroyed")
                        );
                    }
                }
            }
        }
    }
}
