package combat.log.discord.api;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service for interacting with Mojang API to validate Minecraft usernames and get UUIDs
 */
public class MojangAPIService {
    private static final Logger logger = LoggerFactory.getLogger(MojangAPIService.class);
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    
    private final HttpClient httpClient;
    private final Gson gson;
    private final Map<String, CachedProfile> cache;
    private final long cacheDurationMs;

    public MojangAPIService(long cacheDurationMinutes) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.gson = new Gson();
        this.cache = new ConcurrentHashMap<>();
        this.cacheDurationMs = cacheDurationMinutes * 60 * 1000;
    }

    /**
     * Validate username format
     */
    public boolean isValidUsernameFormat(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Get Minecraft profile from username
     * Returns Optional.empty() if username not found or API error
     */
    public Optional<MojangProfile> getProfile(String username) {
        if (!isValidUsernameFormat(username)) {
            logger.warn("Invalid username format: {}", username);
            return Optional.empty();
        }

        // Check cache first
        String lowerUsername = username.toLowerCase();
        CachedProfile cached = cache.get(lowerUsername);
        if (cached != null && !cached.isExpired()) {
            logger.debug("Cache hit for username: {}", username);
            return Optional.of(cached.profile);
        }

        // Query Mojang API
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOJANG_API_URL + username))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                MojangProfile profile = gson.fromJson(response.body(), MojangProfile.class);
                
                // Cache the result
                cache.put(lowerUsername, new CachedProfile(profile, System.currentTimeMillis() + cacheDurationMs));
                
                logger.info("Found Minecraft profile: {} -> {}", username, profile.getFormattedUuid());
                return Optional.of(profile);
            } else if (response.statusCode() == 404) {
                logger.info("Minecraft username not found: {}", username);
                return Optional.empty();
            } else {
                logger.warn("Mojang API returned status {}: {}", response.statusCode(), response.body());
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to query Mojang API for username: {}", username, e);
            return Optional.empty();
        }
    }

    /**
     * Clear expired entries from cache
     */
    public void cleanCache() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
    }

    /**
     * Cached profile entry
     */
    private static class CachedProfile {
        final MojangProfile profile;
        final long expiresAt;

        CachedProfile(MojangProfile profile, long expiresAt) {
            this.profile = profile;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
