package combat.log.report.swordssmp.linking;

import combat.log.report.swordssmp.socket.SocketClient;
import combat.log.report.swordssmp.socket.UnlinkMessage;
import combat.log.report.swordssmp.socket.WhitelistRemoveConfirmationMessage;
import combat.log.report.swordssmp.whitelist.WhitelistCommandGuard;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
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

        dispatcher.register(
            Commands.literal("unlinkother")
                .requires(source -> source.getEntity() == null
                    || (source.getEntity() instanceof ServerPlayer player
                        && source.getServer().getPlayerList().isOp(new NameAndId(player.getUUID(), player.getName().getString()))))
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(UnlinkCommand::executeUnlinkOther))
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
        UnlinkMessage unlinkMsg = new UnlinkMessage(playerUuid, playerName, "self");
        socketClient.sendMessage(unlinkMsg);

        // Remove from whitelist
        context.getSource().getServer().execute(() -> WhitelistCommandGuard.runIgnoringRemove(() -> {
            context.getSource().getServer().getCommands().performPrefixedCommand(
                context.getSource().getServer().createCommandSourceStack(),
                "whitelist remove " + playerName
            );

            WhitelistRemoveConfirmationMessage confirmation = new WhitelistRemoveConfirmationMessage(
                true,
                playerName,
                null
            );
            socketClient.sendMessage(confirmation);
        }));

        player.sendSystemMessage(Component.literal("§aYour Discord account has been unlinked."));
        player.sendSystemMessage(Component.literal("§eYou can relink by requesting whitelist again in Discord."));

        player.connection.disconnect(Component.literal("You unlinked your Discord. Re-link in Discord to rejoin."));

        return 1;
    }

    private static int executeUnlinkOther(CommandContext<CommandSourceStack> context) {
        String targetName = StringArgumentType.getString(context, "player");
        var server = context.getSource().getServer();

        PlayerLinkingManager linkManager = PlayerLinkingManager.getInstance();
        var linkOpt = linkManager.getLinkByName(targetName);
        if (linkOpt.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No linked Discord account found for that player."));
            return 0;
        }

        var link = linkOpt.get();
        String playerUuid = link.getMinecraftUuid();
        String playerName = link.getMinecraftName();

        linkManager.removeLink(playerUuid);
        LOGGER.info("Staff unlinked player {} ({})", playerName, playerUuid);

        SocketClient socketClient = SocketClient.getInstance();
        UnlinkMessage unlinkMsg = new UnlinkMessage(playerUuid, playerName, "admin");
        socketClient.sendMessage(unlinkMsg);

        server.execute(() -> WhitelistCommandGuard.runIgnoringRemove(() -> {
            server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack(),
                "whitelist remove " + playerName
            );

            WhitelistRemoveConfirmationMessage confirmation = new WhitelistRemoveConfirmationMessage(
                true,
                playerName,
                null
            );
            socketClient.sendMessage(confirmation);
        }));

        ServerPlayer target = server.getPlayerList().getPlayerByName(playerName);
        if (target != null) {
            target.connection.disconnect(Component.literal("A staff member unlinked your Discord. Go to Discord to re-link."));
        }

        context.getSource().sendSuccess(() -> Component.literal("Unlinked " + playerName + "."), true);
        return 1;
    }
}
