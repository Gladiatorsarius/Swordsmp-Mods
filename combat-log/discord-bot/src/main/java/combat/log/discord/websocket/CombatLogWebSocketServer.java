package combat.log.discord.websocket;

import combat.log.discord.config.BotConfig;
import combat.log.discord.database.LinkingDatabase;
import combat.log.discord.discord.TicketManager;
import combat.log.discord.models.CombatLogIncident;
import combat.log.discord.models.IncidentDecision;
import combat.log.discord.models.SocketMessage;
import combat.log.discord.models.UnlinkMessage;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * WebSocket server that receives combat log incidents from Minecraft
 */
public class CombatLogWebSocketServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(CombatLogWebSocketServer.class);
    private final Gson gson = new Gson();
    private final TicketManager ticketManager;
    private final BotConfig config;
    private final LinkingDatabase linkingDatabase;
    private WebSocket minecraftConnection;

    public CombatLogWebSocketServer(BotConfig config, TicketManager ticketManager, LinkingDatabase linkingDatabase) {
        super(new InetSocketAddress(config.websocket.host, config.websocket.port));
        this.config = config;
        this.ticketManager = ticketManager;
        this.linkingDatabase = linkingDatabase;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("New connection from: {}", conn.getRemoteSocketAddress());
        minecraftConnection = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.warn("Connection closed: {} - {}", code, reason);
        if (conn == minecraftConnection) {
            minecraftConnection = null;
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("Received message: {}", message);
        
        try {
            // Parse base message to get type
            SocketMessage baseMessage = gson.fromJson(message, SocketMessage.class);
            
            if ("combat_log_incident".equals(baseMessage.getType())) {
                CombatLogIncident incident = gson.fromJson(message, CombatLogIncident.class);
                handleIncident(incident);
            } else if ("whitelist_confirmation".equals(baseMessage.getType())) {
                // Handle whitelist confirmation from Minecraft
                logger.info("Received whitelist confirmation from Minecraft");
            } else if ("unlink_player".equals(baseMessage.getType())) {
                UnlinkMessage unlinkMsg = gson.fromJson(message, UnlinkMessage.class);
                handleUnlink(unlinkMsg);
            } else {
                logger.warn("Unknown message type: {}", baseMessage.getType());
            }
        } catch (Exception e) {
            logger.error("Failed to parse message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("WebSocket error: {}", ex.getMessage(), ex);
    }

    @Override
    public void onStart() {
        logger.info("WebSocket server started on {}:{}", config.websocket.host, config.websocket.port);
    }

    /**
     * Handle incoming combat log incident
     */
    private void handleIncident(CombatLogIncident incident) {
        logger.info("Processing incident {} for player {}", 
            incident.getIncidentId(), incident.getPlayerName());
        
        // Create Discord ticket
        ticketManager.createTicket(incident);
    }

    /**
     * Handle unlink message from Minecraft
     */
    private void handleUnlink(UnlinkMessage message) {
        logger.info("Processing unlink request for player {} ({})", 
            message.getPlayerName(), message.getPlayerUuid());
        
        try {
            // Remove link from database
            linkingDatabase.removeLink(message.getPlayerUuid());
            logger.info("Removed link for player {} from database", message.getPlayerName());
        } catch (Exception e) {
            logger.error("Failed to remove link for player {}: {}", message.getPlayerName(), e.getMessage(), e);
        }
    }

    /**
     * Send decision back to Minecraft server
     */
    public void sendDecision(IncidentDecision decision) {
        if (minecraftConnection != null && minecraftConnection.isOpen()) {
            String json = gson.toJson(decision);
            minecraftConnection.send(json);
            logger.info("Sent decision for incident {} to Minecraft", decision.getIncidentId());
        } else {
            logger.warn("Cannot send decision - Minecraft not connected");
        }
    }

    public boolean isMinecraftConnected() {
        return minecraftConnection != null && minecraftConnection.isOpen();
    }
    
    /**
     * Broadcast message to Minecraft server
     */
    public void broadcast(String message) {
        if (minecraftConnection != null && minecraftConnection.isOpen()) {
            minecraftConnection.send(message);
            logger.debug("Broadcasted message to Minecraft");
        } else {
            logger.warn("Cannot broadcast - Minecraft not connected");
        }
    }
}
