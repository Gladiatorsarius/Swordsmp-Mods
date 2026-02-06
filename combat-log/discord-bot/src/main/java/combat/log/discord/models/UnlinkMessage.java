package combat.log.discord.models;

import com.google.gson.annotations.SerializedName;

/**
 * Message sent from Discord bot to Minecraft to remove a player from whitelist
 */
public class UnlinkMessage extends SocketMessage {
    @SerializedName("playerUuid")
    private String playerUuid;
    
    @SerializedName("playerName")
    private String playerName;

    public UnlinkMessage(String playerUuid, String playerName) {
        setType("unlink_player");
        setTimestamp(System.currentTimeMillis());
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }
}
