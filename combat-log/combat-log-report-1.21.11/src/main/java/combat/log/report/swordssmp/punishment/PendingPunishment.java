package combat.log.report.swordssmp.punishment;

import java.util.UUID;

/**
 * Represents a pending punishment for a player
 */
public class PendingPunishment {
    private final UUID playerUuid;
    private final UUID incidentId;
    private final long createdAt;
    private boolean shouldBan;  // Ban while ticket is pending
    private boolean shouldKillOnLogin;  // Kill when resolved as DENIED
    private String reason;

    public PendingPunishment(UUID playerUuid, UUID incidentId, boolean shouldBan, boolean shouldKillOnLogin) {
        this.playerUuid = playerUuid;
        this.incidentId = incidentId;
        this.createdAt = System.currentTimeMillis();
        this.shouldBan = shouldBan;
        this.shouldKillOnLogin = shouldKillOnLogin;
        this.reason = "You have a pending combat log ticket in Discord";
    }

    // Getters
    public UUID getPlayerUuid() { return playerUuid; }
    public UUID getIncidentId() { return incidentId; }
    public long getCreatedAt() { return createdAt; }
    public boolean shouldBan() { return shouldBan; }
    public boolean shouldKillOnLogin() { return shouldKillOnLogin; }
    public String getReason() { return reason; }

    // Setters
    public void setShouldBan(boolean shouldBan) {
        this.shouldBan = shouldBan;
    }

    public void setShouldKillOnLogin(boolean shouldKillOnLogin) {
        this.shouldKillOnLogin = shouldKillOnLogin;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
