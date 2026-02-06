package combat.log.report.swordssmp;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/**
 * Custom game rules for combat log system
 */
public class CombatLogGameRules {
    /**
     * When true, bypasses the combat log system:
     * - No Discord incidents created
     * - No player heads spawned
     * - Items drop naturally like normal death
     * - No punishments applied
     */
    public static GameRule<Boolean> BYPASS_COMBAT_LOG_SYSTEM;
    
    /**
     * Initialize game rules (called during mod initialization)
     */
    public static void initialize() {
        BYPASS_COMBAT_LOG_SYSTEM = GameRuleBuilder.forBoolean(false)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Identifier.fromNamespaceAndPath("combat-log-report", "bypassCombatLogSystem"));
        
        CombatLogReport.LOGGER.info("Registered combat log game rules");
    }
}
