package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.CombatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public class ServerTickMixin {
    private int tickCounter = 0;
    private final Map<UUID, Boolean> notifiedPlayers = new HashMap<>();

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void onServerTick(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        tickCounter++;
        
        // Check every 20 ticks (1 second)
        if (tickCounter >= 20) {
            tickCounter = 0;
            CombatManager manager = CombatManager.getInstance();
            manager.tick();
            
            // Update action bar for players in combat
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                boolean inCombat = manager.isInCombat(playerId);
                
                if (inCombat) {
                    long remaining = manager.getRemainingTime(playerId);
                    long remainingSeconds = remaining / 1000;
                    
                    // Update action bar every second with countdown
                    player.displayClientMessage(
                        Component.literal("§c§lCOMBAT §f" + remainingSeconds + "s"), 
                        true
                    );
                    
                    notifiedPlayers.put(playerId, true);
                } else if (notifiedPlayers.getOrDefault(playerId, false)) {
                    // Player just left combat - send chat message
                    player.sendSystemMessage(Component.literal("§a§lYou are no longer in combat!"));
                    notifiedPlayers.put(playerId, false);
                }
            }
        }
    }
}
