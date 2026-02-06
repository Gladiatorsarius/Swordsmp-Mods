package combat.log.report.swordssmp;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {
    private static final CombatManager INSTANCE = new CombatManager();
    private final Map<UUID, Long> combatTimers = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> combatOpponents = new ConcurrentHashMap<>();
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
            player.displayClientMessage(Component.literal("§c§lCOMBAT MODE"), true);
            CombatLogReport.LOGGER.info("Player {} entered combat", player.getName().getString());
        }
    }

    public void tagPlayer(ServerPlayer attacker, ServerPlayer victim) {
        UUID attackerId = attacker.getUUID();
        UUID victimId = victim.getUUID();
        long currentTime = System.currentTimeMillis();
        
        boolean attackerWasInCombat = isInCombat(attackerId);
        boolean victimWasInCombat = isInCombat(victimId);
        
        combatTimers.put(attackerId, currentTime + COMBAT_DURATION);
        combatTimers.put(victimId, currentTime + COMBAT_DURATION);
        
        combatOpponents.computeIfAbsent(attackerId, k -> ConcurrentHashMap.newKeySet()).add(victimId);
        combatOpponents.computeIfAbsent(victimId, k -> ConcurrentHashMap.newKeySet()).add(attackerId);
        
        if (!attackerWasInCombat) {
            attacker.displayClientMessage(Component.literal("§c§lCOMBAT MODE"), true);
            CombatLogReport.LOGGER.info("Player {} entered combat with {}", attacker.getName().getString(), victim.getName().getString());
        }
        if (!victimWasInCombat) {
            victim.displayClientMessage(Component.literal("§c§lCOMBAT MODE"), true);
            CombatLogReport.LOGGER.info("Player {} entered combat with {}", victim.getName().getString(), attacker.getName().getString());
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
        combatOpponents.remove(playerId);
    }

    public Set<UUID> getOpponents(UUID playerId) {
        return combatOpponents.getOrDefault(playerId, Set.of());
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
        combatTimers.entrySet().removeIf(entry -> {
            if (entry.getValue() <= currentTime) {
                combatOpponents.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}
