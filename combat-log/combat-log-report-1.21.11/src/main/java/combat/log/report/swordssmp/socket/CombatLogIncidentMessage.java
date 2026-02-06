package combat.log.report.swordssmp.socket;

import com.google.gson.annotations.SerializedName;

/**
 * Message sent from Minecraft to Discord when a player combat logs
 */
public class CombatLogIncidentMessage extends SocketMessage {
    @SerializedName("incidentId")
    private final String incidentId;
    
    @SerializedName("playerUuid")
    private final String playerUuid;
    
    @SerializedName("playerName")
    private final String playerName;
    
    @SerializedName("combatTimeRemaining")
    private final double combatTimeRemaining;

    public CombatLogIncidentMessage(String incidentId, String playerUuid, String playerName, double combatTimeRemaining) {
        super("combat_log_incident");
        this.incidentId = incidentId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.combatTimeRemaining = combatTimeRemaining;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getCombatTimeRemaining() {
        return combatTimeRemaining;
    }
}
