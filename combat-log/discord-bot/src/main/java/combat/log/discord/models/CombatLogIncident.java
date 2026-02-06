package combat.log.discord.models;

import com.google.gson.annotations.SerializedName;

/**
 * Incident message received from Minecraft server
 */
public class CombatLogIncident extends SocketMessage {
    @SerializedName("incidentId")
    private String incidentId;
    
    @SerializedName("playerUuid")
    private String playerUuid;
    
    @SerializedName("playerName")
    private String playerName;
    
    @SerializedName("combatTimeRemaining")
    private double combatTimeRemaining;

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
