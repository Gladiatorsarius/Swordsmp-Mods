package combat.log.report.swordssmp.socket;

import com.google.gson.annotations.SerializedName;

/**
 * Message from Discord to Minecraft to store player link
 */
public class PlayerLinkMessage extends SocketMessage {
    @SerializedName("discordId")
    private String discordId;
    
    @SerializedName("playerUuid")
    private String playerUuid;
    
    @SerializedName("playerName")
    private String playerName;
    
    @SerializedName("whitelisted")
    private boolean whitelisted;

    public String getDiscordId() {
        return discordId;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }
}
