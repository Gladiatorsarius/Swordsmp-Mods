package combat.log.report.swordssmp.incident;

/**
 * Status of a combat log incident
 */
public enum IncidentStatus {
    PENDING,           // Waiting for clip submission
    CLIP_UPLOADED,     // Clip submitted, awaiting admin review
    APPROVED,          // Admin approved, no punishment
    DENIED,            // Admin denied, punishment confirmed
    AUTO_DENIED,       // Timeout expired, auto-punishment
    EXPIRED            // Old incident, archived
}
