package combat.log.report.swordssmp.socket;

import com.google.gson.annotations.SerializedName;

/**
 * Confirmation message sent from Minecraft back to Discord after whitelist command
 */
public class WhitelistConfirmationMessage extends SocketMessage {
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("playerName")
    private String playerName;
    
    @SerializedName("error")
    private String error;

    public WhitelistConfirmationMessage(String requestId, boolean success, String playerName, String error) {
        setType("whitelist_confirmation");
        setTimestamp(System.currentTimeMillis());
        this.requestId = requestId;
        this.success = success;
        this.playerName = playerName;
        this.error = error;
    }

    public String getRequestId() {
        return requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getError() {
        return error;
    }
}
