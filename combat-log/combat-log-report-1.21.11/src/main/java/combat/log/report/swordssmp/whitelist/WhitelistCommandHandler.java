package combat.log.report.swordssmp.whitelist;

import combat.log.report.swordssmp.socket.SocketClient;
import combat.log.report.swordssmp.socket.WhitelistAddMessage;
import combat.log.report.swordssmp.socket.WhitelistConfirmationMessage;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles whitelist commands from Discord bot
 */
public class WhitelistCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistCommandHandler.class);
    private final MinecraftServer server;
    private final SocketClient socketClient;

    public WhitelistCommandHandler(MinecraftServer server, SocketClient socketClient) {
        this.server = server;
        this.socketClient = socketClient;
    }

    /**
     * Process a whitelist add command
     */
    public void handleWhitelistAdd(WhitelistAddMessage message) {
        LOGGER.info("Processing whitelist add for player: {} ({})", message.getPlayerName(), message.getPlayerUuid());

        try {
            // Execute whitelist command using the server's command dispatcher
            String command = String.format("whitelist add %s", message.getPlayerName());
            server.execute(() -> {
                server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    command
                );
            });
            
            LOGGER.info("Added {} to whitelist", message.getPlayerName());
            
            // Send confirmation back to Discord
            WhitelistConfirmationMessage confirmation = new WhitelistConfirmationMessage(
                message.getRequestId(),
                true,
                message.getPlayerName(),
                null
            );
            socketClient.sendMessage(confirmation);
            
        } catch (Exception e) {
            LOGGER.error("Failed to add player to whitelist: {}", message.getPlayerName(), e);
            
            // Send error confirmation back to Discord
            WhitelistConfirmationMessage confirmation = new WhitelistConfirmationMessage(
                message.getRequestId(),
                false,
                message.getPlayerName(),
                e.getMessage()
            );
            socketClient.sendMessage(confirmation);
        }
    }
}
