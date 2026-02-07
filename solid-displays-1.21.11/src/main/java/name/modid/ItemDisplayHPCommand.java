package name.modid;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
// BoolArgumentType removed because enable command was removed
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Display;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * Commands to manage Item Display HP
 */
public class ItemDisplayHPCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("displayhp");

        root.then(Commands.literal("set")
            .then(Commands.argument("target", EntityArgument.entity())
                .then(Commands.argument("hp", IntegerArgumentType.integer(1, 1000))
                    .executes(ItemDisplayHPCommand::setHP))));

        root.then(Commands.literal("get")
            .then(Commands.argument("target", EntityArgument.entity())
                .executes(ItemDisplayHPCommand::getHP)));

        root.then(Commands.literal("damage")
            .then(Commands.argument("target", EntityArgument.entity())
                .then(Commands.argument("damage", FloatArgumentType.floatArg(0))
                    .executes(ItemDisplayHPCommand::damageDisplay))));

        // 'enable' command removed; use /gamerule solid_displays to control the feature

        dispatcher.register(root);
    }

    private static int setHP(CommandContext<CommandSourceStack> context) {
        try {
            var entity = EntityArgument.getEntity(context, "target");
            int hp = IntegerArgumentType.getInteger(context, "hp");
            
            if (!(entity instanceof Display.ItemDisplay)) {
                context.getSource().sendFailure(
                    Component.literal("Error: target is not an Item Display")
                );
                return 0;
            }

            Display.ItemDisplay display = (Display.ItemDisplay) entity;
            
            // Check if GameRule is enabled
                if (!ItemDisplayHPGamerule.isEnabled(display.level())) {
                context.getSource().sendFailure(
                    Component.literal("Item Display HP system is disabled. Enable with: /gamerule solid_displays true")
                );
                return 0;
            }
            
            ItemDisplayHPManager.setHP(display, hp);
            
            context.getSource().sendSuccess(
                () -> Component.literal("Item Display HP set to " + hp),
                true
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("Error: " + e.getMessage())
            );
            return 0;
        }
    }

    private static int getHP(CommandContext<CommandSourceStack> context) {
        try {
            var entity = EntityArgument.getEntity(context, "target");
            
            if (!(entity instanceof Display.ItemDisplay)) {
                context.getSource().sendFailure(
                    Component.literal("Error: target is not an Item Display")
                );
                return 0;
            }

            Display.ItemDisplay display = (Display.ItemDisplay) entity;
            
            // Check if GameRule is enabled
            if (!ItemDisplayHPGamerule.isEnabled(display.level())) {
                context.getSource().sendFailure(
                    Component.literal("Item Display HP system is disabled. Enable with: /gamerule solid_displays true")
                );
                return 0;
            }
            
            float currentHP = ItemDisplayHPManager.getHP(display);
            float maxHP = ItemDisplayHPManager.getMaxHP(display);
            
            context.getSource().sendSuccess(
                () -> Component.literal(String.format("Item Display HP: %.1f / %.1f", currentHP, maxHP)),
                false
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("Error: " + e.getMessage())
            );
            return 0;
        }
    }

    private static int damageDisplay(CommandContext<CommandSourceStack> context) {
        try {
            var entity = EntityArgument.getEntity(context, "target");
            float damage = FloatArgumentType.getFloat(context, "damage");
            
            if (!(entity instanceof Display.ItemDisplay)) {
                context.getSource().sendFailure(
                    Component.literal("Error: target is not an Item Display")
                );
                return 0;
            }

            Display.ItemDisplay display = (Display.ItemDisplay) entity;
            
            // Check if GameRule is enabled
            if (!ItemDisplayHPGamerule.isEnabled(display.level())) {
                context.getSource().sendFailure(
                    Component.literal("Item Display HP system is disabled. Enable with: /gamerule solid_displays true")
                );
                return 0;
            }
            
            ItemDisplayHPManager.damageDisplay(display, damage);
            
            context.getSource().sendSuccess(
                () -> Component.literal(String.format("Item Display took %.1f damage", damage)),
                true
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("Error: " + e.getMessage())
            );
            return 0;
        }
    }

    // 'setEnabled' removed; use /gamerule solid_displays to control the feature
}
