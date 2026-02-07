package name.modid;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.Level;

/**
 * GameRule for the Item Display HP system
 */
public class ItemDisplayHPGamerule {
    
    public static GameRule<Boolean> ITEM_DISPLAY_HP;
    private static boolean enabled = true;

    public static void register() {
        // Register world-specific game rule `solid_displays` with default true
        ITEM_DISPLAY_HP = GameRuleBuilder.forBoolean(true)
            .category(GameRuleCategory.MISC)
            .buildAndRegister(Identifier.fromNamespaceAndPath(SolidDisplays.MOD_ID, "solid_displays"));
    }

    public static boolean isEnabled(Level level) {
        // Prefer the GameRule value on server levels; fall back to manual override
        if (level instanceof ServerLevel serverLevel) {
            try {
                Object ruleVal = serverLevel.getGameRules().get(ITEM_DISPLAY_HP);
                if (ruleVal != null) return Boolean.TRUE.equals(ruleVal);
            } catch (Exception e) {
                // fall through to override
            }
        }
        return enabled;
    }

    public static void setEnabled(boolean value) {
        // allow manual override for development/testing
        enabled = value;
    }
}
