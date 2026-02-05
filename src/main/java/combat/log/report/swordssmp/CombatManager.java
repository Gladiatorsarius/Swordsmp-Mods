package combat.log.report.swordssmp;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {
    private static final CombatManager INSTANCE = new CombatManager();
    private final Map<UUID, Long> combatTimers = new ConcurrentHashMap<>();
    private static final long COMBAT_DURATION = 15000; // 15 seconds in milliseconds

    private CombatManager() {}

    public static CombatManager getInstance() {
        return INSTANCE;
    }

    public void tagPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        
        boolean wasInCombat = isInCombat(playerId);
        combatTimers.put(playerId, currentTime + COMBAT_DURATION);
        
        if (!wasInCombat) {
            player.sendSystemMessage(Component.literal("§c§lYou are now in combat! Do not log out for 15 seconds!"));
            CombatLogReport.LOGGER.info("Player {} entered combat", player.getName().getString());
        }
    }

    public boolean isInCombat(UUID playerId) {
        Long endTime = combatTimers.get(playerId);
        if (endTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= endTime) {
            combatTimers.remove(playerId);
            return false;
        }
        
        return true;
    }

    public void removePlayer(UUID playerId) {
        combatTimers.remove(playerId);
    }

    public long getRemainingTime(UUID playerId) {
        Long endTime = combatTimers.get(playerId);
        if (endTime == null) {
            return 0;
        }
        
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    public void tick() {
        // Clean up expired combat tags
        long currentTime = System.currentTimeMillis();
        combatTimers.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
    }
}
