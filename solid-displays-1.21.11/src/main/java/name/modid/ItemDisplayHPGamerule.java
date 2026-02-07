package name.modid;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.Level;

/**
 * GameRule für das Item Display HP-System
 */
public class ItemDisplayHPGamerule {
    
    public static GameRule<Boolean> ITEM_DISPLAY_HP;

    public static void register() {
        ITEM_DISPLAY_HP = GameRuleBuilder.forBoolean(false)
            .category(GameRuleCategory.MISC)
            .buildAndRegister(Identifier.fromNamespaceAndPath(SolidDisplays.MOD_ID, "itemDisplayHPEnabled"));
    }

    public static boolean isEnabled(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return serverLevel.getGameRules().getRule(ITEM_DISPLAY_HP).get();
    }
}
