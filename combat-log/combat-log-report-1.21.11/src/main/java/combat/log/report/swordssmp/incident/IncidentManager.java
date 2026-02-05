package combat.log.report.swordssmp.incident;

import combat.log.report.swordssmp.CombatLogReport;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Manages combat log incidents
 */
public class IncidentManager {
    private static final IncidentManager INSTANCE = new IncidentManager();
    private final Map<UUID, CombatLogIncident> incidents = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToIncident = new ConcurrentHashMap<>();

    private IncidentManager() {}

    public static IncidentManager getInstance() {
        return INSTANCE;
    }

    /**
     * Create a new incident for a player who combat logged
     */
    public CombatLogIncident createIncident(UUID playerUuid, String playerName, double combatTimeRemaining) {
        CombatLogIncident incident = new CombatLogIncident(playerUuid, playerName, combatTimeRemaining);
        incidents.put(incident.getId(), incident);
        playerToIncident.put(playerUuid, incident.getId());
        
        CombatLogReport.LOGGER.info("Created incident {} for player {} (UUID: {})", 
            incident.getId(), playerName, playerUuid);
        
        return incident;
    }

    /**
     * Get an incident by its ID
     */
    public CombatLogIncident getIncident(UUID incidentId) {
        return incidents.get(incidentId);
    }

    /**
     * Get the active incident for a player
     */
    public CombatLogIncident getPlayerIncident(UUID playerUuid) {
        UUID incidentId = playerToIncident.get(playerUuid);
        return incidentId != null ? incidents.get(incidentId) : null;
    }

    /**
     * Update incident status
     */
    public void updateIncidentStatus(UUID incidentId, IncidentStatus newStatus) {
        CombatLogIncident incident = incidents.get(incidentId);
        if (incident != null) {
            incident.setStatus(newStatus);
            CombatLogReport.LOGGER.info("Updated incident {} status to {}", incidentId, newStatus);
        }
    }

    /**
     * Clear incident for a player (after resolution)
     */
    public void clearPlayerIncident(UUID playerUuid) {
        UUID incidentId = playerToIncident.remove(playerUuid);
        if (incidentId != null) {
            CombatLogIncident incident = incidents.get(incidentId);
            if (incident != null) {
                incident.setStatus(IncidentStatus.EXPIRED);
            }
            CombatLogReport.LOGGER.info("Cleared incident {} for player {}", incidentId, playerUuid);
        }
    }

    /**
     * Get all pending incidents
     */
    public Collection<CombatLogIncident> getPendingIncidents() {
        return incidents.values().stream()
            .filter(i -> i.getStatus() == IncidentStatus.PENDING || 
                        i.getStatus() == IncidentStatus.CLIP_UPLOADED)
            .collect(Collectors.toList());
    }

    /**
     * Check for expired incidents and auto-deny them
     */
    public void checkExpiredIncidents(long timeoutMinutes) {
        long currentTime = System.currentTimeMillis();
        
        incidents.values().stream()
            .filter(i -> i.getStatus() == IncidentStatus.PENDING || 
                        i.getStatus() == IncidentStatus.CLIP_UPLOADED)
            .filter(i -> i.isExpired(timeoutMinutes))
            .forEach(i -> {
                i.setStatus(IncidentStatus.AUTO_DENIED);
                CombatLogReport.LOGGER.warn("Auto-denied incident {} for player {} (timeout expired)", 
                    i.getId(), i.getPlayerName());
            });
    }

    /**
     * Get all incidents (for persistence)
     */
    public Collection<CombatLogIncident> getAllIncidents() {
        return incidents.values();
    }

    /**
     * Load incident from persistence
     */
    public void loadIncident(CombatLogIncident incident) {
        incidents.put(incident.getId(), incident);
        if (incident.getStatus() == IncidentStatus.PENDING || 
            incident.getStatus() == IncidentStatus.CLIP_UPLOADED) {
            playerToIncident.put(incident.getPlayerUuid(), incident.getId());
        }
    }

    /**
     * Clear all incidents (for testing/reloading)
     */
    public void clearAll() {
        incidents.clear();
        playerToIncident.clear();
    }
}
