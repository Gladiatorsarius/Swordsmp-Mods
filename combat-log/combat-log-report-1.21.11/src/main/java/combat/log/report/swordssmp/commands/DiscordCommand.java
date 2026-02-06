package combat.log.report.swordssmp.commands;

import combat.log.report.swordssmp.socket.SocketClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command to force the Discord bot WebSocket reconnect.
 */
public class DiscordCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("discord")
                .requires(source -> isOpaiz(source))
                .executes(DiscordCommand::executeReconnect)
        );
    }

    private static int executeReconnect(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Attempting to reconnect to the Discord bot..."), false);
        SocketClient.getInstance().forceReconnect();
        return 1;
    }

    private static boolean isOpaiz(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return false;
        }

        return "Opaiz".equalsIgnoreCase(player.getName().getString());
    }
}
