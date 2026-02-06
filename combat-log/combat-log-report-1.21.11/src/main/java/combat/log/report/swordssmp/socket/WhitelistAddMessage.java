package combat.log.report.swordssmp.socket;

import com.google.gson.annotations.SerializedName;

/**
 * Message from Discord to Minecraft to add player to whitelist
 */
public class WhitelistAddMessage extends SocketMessage {
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("playerName")
    private String playerName;
    
    @SerializedName("playerUuid")
    private String playerUuid;
    
    @SerializedName("discordId")
    private String discordId;
    
    @SerializedName("requestedBy")
    private String requestedBy;

    public String getRequestId() {
        return requestId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getDiscordId() {
        return discordId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }
}
