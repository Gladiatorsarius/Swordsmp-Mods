package combat.log.discord.integration;

import combat.log.discord.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service to integrate with DiscordSRV for player-Discord linking
 */
public class DiscordSRVService {
    private static final Logger logger = LoggerFactory.getLogger(DiscordSRVService.class);
    
    private final BotConfig.DiscordSRVSettings settings;
    private final Map<String, CachedLink> linkCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(5);
    
    public DiscordSRVService(BotConfig.DiscordSRVSettings settings) {
        this.settings = settings;
        
        if (settings.enabled) {
            logger.info("DiscordSRV integration enabled ({})", settings.databaseType);
            testConnection();
        } else {
            logger.info("DiscordSRV integration disabled");
        }
    }
    
    /**
     * Test database connection
     */
    private void testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                logger.info("Successfully connected to DiscordSRV database");
                conn.close();
            }
        } catch (Exception e) {
            logger.warn("Failed to connect to DiscordSRV database: {}", e.getMessage());
        }
    }
    
    /**
     * Get Discord ID from Minecraft UUID
     */
    public String getDiscordId(String minecraftUuid) {
        if (!settings.enabled) {
            return null;
        }
        
        // Check cache
        CachedLink cached = linkCache.get(minecraftUuid);
        if (cached != null && !cached.isExpired()) {
            return cached.discordId;
        }
        
        // Query database
        try {
            String discordId = queryDiscordId(minecraftUuid);
            
            // Cache result (even if null)
            linkCache.put(minecraftUuid, new CachedLink(discordId));
            
            if (discordId != null) {
                logger.debug("Found Discord link for {}: {}", minecraftUuid, discordId);
            } else {
                logger.debug("No Discord link found for {}", minecraftUuid);
            }
            
            return discordId;
        } catch (Exception e) {
            logger.error("Failed to query Discord link for {}: {}", minecraftUuid, e.getMessage());
            return null;
        }
    }
    
    /**
     * Query Discord ID from database
     */
    private String queryDiscordId(String minecraftUuid) throws Exception {
        Connection conn = getConnection();
        if (conn == null) {
            return null;
        }
        
        try {
            // Try DiscordSRV v1/v2 schema
            String sql = "SELECT discord FROM discordsrv_accounts WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, minecraftUuid);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("discord");
                }
            } catch (Exception e) {
                // Try alternative schema
                sql = "SELECT discordid FROM accounts WHERE uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, minecraftUuid);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return rs.getString("discordid");
                    }
                }
            }
            
            return null;
        } finally {
            conn.close();
        }
    }
    
    /**
     * Get database connection
     */
    private Connection getConnection() throws Exception {
        if ("sqlite".equalsIgnoreCase(settings.databaseType)) {
            return DriverManager.getConnection("jdbc:sqlite:" + settings.databasePath);
        } else if ("mysql".equalsIgnoreCase(settings.databaseType)) {
            String url = String.format("jdbc:mysql://%s:%d/%s", 
                settings.mysql.host, 
                settings.mysql.port, 
                settings.mysql.database);
            return DriverManager.getConnection(url, 
                settings.mysql.username, 
                settings.mysql.password);
        } else {
            logger.error("Unknown database type: {}", settings.databaseType);
            return null;
        }
    }
    
    /**
     * Clear cache for a specific player
     */
    public void clearCache(String minecraftUuid) {
        linkCache.remove(minecraftUuid);
    }
    
    /**
     * Clear entire cache
     */
    public void clearAllCache() {
        linkCache.clear();
    }
    
    /**
     * Cached link entry
     */
    private static class CachedLink {
        final String discordId;
        final long timestamp;
        
        CachedLink(String discordId) {
            this.discordId = discordId;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}
