package combat.log.report.swordssmp.whitelist;

import combat.log.report.swordssmp.socket.SocketClient;
import combat.log.report.swordssmp.socket.WhitelistAddMessage;
import combat.log.report.swordssmp.socket.WhitelistConfirmationMessage;
import combat.log.report.swordssmp.linking.PlayerLinkingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import com.mojang.authlib.GameProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
            // Parse UUID
            UUID playerUuid = UUID.fromString(message.getPlayerUuid());
            
            // Create game profile
            GameProfile profile = new GameProfile(playerUuid, message.getPlayerName());
            
            // Add to whitelist
            UserWhiteList whitelist = server.getPlayerList().getWhiteList();
            UserWhiteListEntry entry = new UserWhiteListEntry(profile);
            whitelist.add(entry);
            
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
