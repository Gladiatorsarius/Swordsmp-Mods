package combat.log.report.swordssmp.socket;

import combat.log.report.swordssmp.CombatLogReport;
import combat.log.report.swordssmp.incident.IncidentManager;
import combat.log.report.swordssmp.incident.IncidentStatus;
import combat.log.report.swordssmp.punishment.PunishmentManager;
import combat.log.report.swordssmp.linking.PlayerLinkingManager;
import combat.log.report.swordssmp.whitelist.WhitelistCommandHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for communicating with Discord bot
 */
public class SocketClient {
    private static final SocketClient INSTANCE = new SocketClient();
    private final Gson gson = new Gson();
    private final OkHttpClient httpClient;
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    
    private WebSocket webSocket;
    private String serverUrl = "ws://localhost:8080/combat-log"; // Default, can be configured
    private boolean connected = false;
    private boolean reconnecting = false;
    private WhitelistCommandHandler whitelistHandler;

    private SocketClient() {
        this.httpClient = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    }

    public static SocketClient getInstance() {
        return INSTANCE;
    }

    /**
     * Configure the WebSocket server URL
     */
    public void configure(String serverUrl) {
        this.serverUrl = serverUrl;
        CombatLogReport.LOGGER.info("Configured socket server URL: {}", serverUrl);
    }
    
    /**
     * Set the whitelist command handler
     */
    public void setWhitelistHandler(WhitelistCommandHandler handler) {
        this.whitelistHandler = handler;
    }

    /**
     * Connect to the Discord bot WebSocket server
     */
    public void connect() {
        if (connected || reconnecting) {
            return;
        }

        reconnecting = true;
        Request request = new Request.Builder()
            .url(serverUrl)
            .build();

        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                connected = true;
                reconnecting = false;
                CombatLogReport.LOGGER.info("Connected to Discord bot WebSocket server");
                
                // Send any queued messages
                String queuedMessage;
                while ((queuedMessage = messageQueue.poll()) != null) {
                    webSocket.send(queuedMessage);
                    CombatLogReport.LOGGER.info("Sent queued message");
                }
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                handleMessage(text);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                webSocket.close(1000, null);
                connected = false;
                CombatLogReport.LOGGER.warn("WebSocket closing: {} - {}", code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                connected = false;
                reconnecting = false;
                CombatLogReport.LOGGER.error("WebSocket connection failed: {}", t.getMessage());
                
                // Schedule reconnection after 30 seconds
                scheduleReconnect();
            }
        });
    }

    /**
     * Send incident to Discord bot
     */
    public void sendIncident(UUID incidentId, UUID playerUuid, String playerName, double combatTimeRemaining) {
        CombatLogIncidentMessage message = new CombatLogIncidentMessage(
            incidentId.toString(),
            playerUuid.toString(),
            playerName,
            combatTimeRemaining
        );

        String json = gson.toJson(message);
        sendMessageJson(json);
    }

    /**
     * Send a message to the Discord bot (now public for whitelist use)
     */
    public void sendMessage(SocketMessage message) {
        String json = gson.toJson(message);
        sendMessageJson(json);
    }
    
    /**
     * Send a JSON message to the Discord bot
     */
    private void sendMessageJson(String message) {
        if (connected && webSocket != null) {
            webSocket.send(message);
            CombatLogReport.LOGGER.info("Sent message to Discord bot");
        } else {
            // Queue message for later delivery
            messageQueue.offer(message);
            CombatLogReport.LOGGER.warn("Discord bot not connected, message queued ({} in queue)", messageQueue.size());
            
            // Try to connect if not already trying
            if (!reconnecting) {
                connect();
            }
        }
    }

    /**
     * Handle incoming message from Discord bot
     */
    private void handleMessage(String text) {
        try {
            JsonObject obj = JsonParser.parseString(text).getAsJsonObject();
            String type = obj.has("type") ? obj.get("type").getAsString() : null;
            if (type == null || type.isBlank()) {
                CombatLogReport.LOGGER.warn("Received message without type: {}", text);
                return;
            }

            if ("incident_decision".equals(type)) {
                IncidentDecisionMessage decision = gson.fromJson(text, IncidentDecisionMessage.class);
                handleIncidentDecision(decision);
            } else if ("whitelist_add".equals(type)) {
                WhitelistAddMessage whitelistMsg = gson.fromJson(text, WhitelistAddMessage.class);
                handleWhitelistAdd(whitelistMsg);
            } else if ("link_player".equals(type)) {
                PlayerLinkMessage linkMsg = gson.fromJson(text, PlayerLinkMessage.class);
                handlePlayerLink(linkMsg);
            } else if ("unlink_player".equals(type)) {
                UnlinkMessage unlinkMsg = gson.fromJson(text, UnlinkMessage.class);
                handleUnlink(unlinkMsg);
            } else {
                CombatLogReport.LOGGER.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            CombatLogReport.LOGGER.error("Failed to parse message from Discord bot: {}", e.getMessage());
        }
    }

    /**
     * Handle incident decision from Discord bot
     */
    private void handleIncidentDecision(IncidentDecisionMessage decision) {
        try {
            UUID incidentId = UUID.fromString(decision.getIncidentId());
            IncidentStatus status = IncidentStatus.valueOf(decision.getStatus());
            
            CombatLogReport.LOGGER.info("Received decision for incident {}: {} by {}",
                incidentId, status, decision.getAdminName());
            
            // Update incident status
            IncidentManager incidentManager = IncidentManager.getInstance();
            incidentManager.updateIncidentStatus(incidentId, status);
            
            // Update punishment status
            var incident = incidentManager.getIncident(incidentId);
            if (incident != null) {
                PunishmentManager punishmentManager = PunishmentManager.getInstance();
                punishmentManager.updatePunishmentStatus(incident.getPlayerUuid(), status);
            }
        } catch (Exception e) {
            CombatLogReport.LOGGER.error("Failed to handle incident decision: {}", e.getMessage());
        }
    }
    
    /**
     * Handle whitelist add command from Discord bot
     */
    private void handleWhitelistAdd(WhitelistAddMessage message) {
        CombatLogReport.LOGGER.info("Received whitelist add command for: {}", message.getPlayerName());
        if (whitelistHandler != null) {
            whitelistHandler.handleWhitelistAdd(message);
        } else {
            CombatLogReport.LOGGER.warn("WhitelistCommandHandler not set, ignoring whitelist command");
        }
    }
    
    /**
     * Handle player link message from Discord bot
     */
    private void handlePlayerLink(PlayerLinkMessage message) {
        CombatLogReport.LOGGER.info("Received player link: Discord {} <-> Minecraft {} ({})", 
            message.getDiscordId(), message.getPlayerName(), message.getPlayerUuid());
        
        PlayerLinkingManager linkManager = PlayerLinkingManager.getInstance();
        linkManager.addLink(
            message.getDiscordId(),
            message.getPlayerUuid(),
            message.getPlayerName(),
            message.isWhitelisted()
        );
    }

    /**
     * Handle unlink message from Discord bot
     */
    private void handleUnlink(UnlinkMessage message) {
        CombatLogReport.LOGGER.info("Received unlink command for: {} ({})", 
            message.getPlayerName(), message.getPlayerUuid());
        
        // Remove from linking manager
        PlayerLinkingManager linkManager = PlayerLinkingManager.getInstance();
        linkManager.removeLink(message.getPlayerUuid());

        if (whitelistHandler != null) {
            whitelistHandler.handleWhitelistRemove(message);
        } else {
            CombatLogReport.LOGGER.warn("WhitelistCommandHandler not set, cannot remove from whitelist");
        }
    }

    /**
     * Schedule reconnection attempt
     */
    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(30000); // Wait 30 seconds
                if (!connected && !reconnecting) {
                    CombatLogReport.LOGGER.info("Attempting to reconnect to Discord bot...");
                    connect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Server shutting down");
            connected = false;
        }
    }

    /**
     * Force a disconnect/reconnect cycle to re-establish the WebSocket.
     */
    public synchronized void forceReconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Manual reconnect");
            webSocket = null;
        }

        connected = false;
        reconnecting = false;
        CombatLogReport.LOGGER.info("Manual reconnect requested; reconnecting to Discord bot...");
        connect();
    }

    public boolean isConnected() {
        return connected;
    }

    public int getQueuedMessageCount() {
        return messageQueue.size();
    }
}
