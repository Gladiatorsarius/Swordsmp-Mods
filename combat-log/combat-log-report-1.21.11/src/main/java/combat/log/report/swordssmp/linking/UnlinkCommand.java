package combat.log.report.swordssmp.linking;

import combat.log.report.swordssmp.socket.SocketClient;
import combat.log.report.swordssmp.socket.UnlinkMessage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to allow players to unlink their Discord account
 */
public class UnlinkCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnlinkCommand.class);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("unlink")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(UnlinkCommand::executeUnlink)
        );
    }

    private static int executeUnlink(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        String playerUuid = player.getUUID().toString();
        String playerName = player.getName().getString();

        // Check if player is linked
        PlayerLinkingManager linkManager = PlayerLinkingManager.getInstance();
        if (linkManager.getLinkByUuid(playerUuid).isEmpty()) {
            player.sendSystemMessage(Component.literal("§cYou don't have a linked Discord account."));
            return 0;
        }

        // Remove link locally
        linkManager.removeLink(playerUuid);
        LOGGER.info("Player {} ({}) unlinked their Discord account", playerName, playerUuid);

        // Send unlink message to Discord bot via WebSocket
        SocketClient socketClient = SocketClient.getInstance();
        UnlinkMessage unlinkMsg = new UnlinkMessage(playerUuid, playerName);
        socketClient.sendMessage(unlinkMsg);

        // Remove from whitelist
        context.getSource().getServer().execute(() -> {
            context.getSource().getServer().getCommands().performPrefixedCommand(
                context.getSource().getServer().createCommandSourceStack(),
                "whitelist remove " + playerName
            );
        });

        player.sendSystemMessage(Component.literal("§aYour Discord account has been unlinked."));
        player.sendSystemMessage(Component.literal("§eYou have been removed from the whitelist."));
        player.sendSystemMessage(Component.literal("§eYou can relink by requesting whitelist again in Discord."));

        return 1;
    }
}
