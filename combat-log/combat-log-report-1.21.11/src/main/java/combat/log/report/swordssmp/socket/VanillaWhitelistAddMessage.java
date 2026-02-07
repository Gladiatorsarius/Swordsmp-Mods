package combat.log.report.swordssmp.socket;

import com.google.gson.annotations.SerializedName;

/**
 * Message from Minecraft to Discord when vanilla /whitelist add is used.
 */
public class VanillaWhitelistAddMessage extends SocketMessage {
    @SerializedName("playerName")
    private final String playerName;

    @SerializedName("playerUuid")
    private final String playerUuid;

    @SerializedName("sourceName")
    private final String sourceName;

    public VanillaWhitelistAddMessage(String playerName, String playerUuid, String sourceName) {
        super("vanilla_whitelist_add");
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.sourceName = sourceName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getSourceName() {
        return sourceName;
    }
}
