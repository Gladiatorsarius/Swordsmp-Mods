package combat.log.report.swordssmp.incident;

import java.util.UUID;

/**
 * Represents a combat logging incident
 */
public class CombatLogIncident {
    private final UUID id;
    private final UUID playerUuid;
    private final String playerName;
    private final long timestamp;
    private final double combatTimeRemaining;
    private String discordTicketId;
    private IncidentStatus status;
    private String clipUrl;
    private Long clipUploadTime;
    private String adminDecision;
    private UUID adminUuid;
    private Long decisionTime;
    private String notes;

    public CombatLogIncident(UUID playerUuid, String playerName, double combatTimeRemaining) {
        this.id = UUID.randomUUID();
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.timestamp = System.currentTimeMillis();
        this.combatTimeRemaining = combatTimeRemaining;
        this.status = IncidentStatus.PENDING;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public long getTimestamp() { return timestamp; }
    public double getCombatTimeRemaining() { return combatTimeRemaining; }
    public String getDiscordTicketId() { return discordTicketId; }
    public IncidentStatus getStatus() { return status; }
    public String getClipUrl() { return clipUrl; }
    public Long getClipUploadTime() { return clipUploadTime; }
    public String getAdminDecision() { return adminDecision; }
    public UUID getAdminUuid() { return adminUuid; }
    public Long getDecisionTime() { return decisionTime; }
    public String getNotes() { return notes; }

    // Setters
    public void setDiscordTicketId(String discordTicketId) {
        this.discordTicketId = discordTicketId;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public void setClipUrl(String clipUrl) {
        this.clipUrl = clipUrl;
        this.clipUploadTime = System.currentTimeMillis();
        if (this.status == IncidentStatus.PENDING) {
            this.status = IncidentStatus.CLIP_UPLOADED;
        }
    }

    public void setAdminDecision(String decision, UUID adminUuid) {
        this.adminDecision = decision;
        this.adminUuid = adminUuid;
        this.decisionTime = System.currentTimeMillis();
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getDeadline(long timeoutMinutes) {
        return timestamp + (timeoutMinutes * 60 * 1000);
    }

    public boolean isExpired(long timeoutMinutes) {
        return System.currentTimeMillis() > getDeadline(timeoutMinutes);
    }
}
