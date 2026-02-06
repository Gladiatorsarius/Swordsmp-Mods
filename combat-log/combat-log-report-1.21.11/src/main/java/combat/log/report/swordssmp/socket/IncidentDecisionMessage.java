package combat.log.report.swordssmp.socket;

import com.google.gson.annotations.SerializedName;

/**
 * Message sent from Discord to Minecraft with admin decision
 */
public class IncidentDecisionMessage extends SocketMessage {
    @SerializedName("incidentId")
    private String incidentId;
    
    @SerializedName("status")
    private String status;  // APPROVED, DENIED, AUTO_DENIED
    
    @SerializedName("adminName")
    private String adminName;
    
    @SerializedName("reason")
    private String reason;

    public IncidentDecisionMessage() {
        super("incident_decision");
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getStatus() {
        return status;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getReason() {
        return reason;
    }
}
