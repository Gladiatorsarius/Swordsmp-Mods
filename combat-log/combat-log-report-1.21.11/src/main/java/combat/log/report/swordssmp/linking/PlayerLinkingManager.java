package combat.log.report.swordssmp.linking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages Discord <-> Minecraft player links locally on the server
 */
public class PlayerLinkingManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerLinkingManager.class);
    private static PlayerLinkingManager instance;
    
    private final Map<String, PlayerLink> linksByUuid;  // Minecraft UUID -> PlayerLink
    private final Map<String, PlayerLink> linksByDiscord;  // Discord ID -> PlayerLink
    private final File linkFile;
    private final Gson gson;

    private PlayerLinkingManager(File configDir) {
        this.linksByUuid = new HashMap<>();
        this.linksByDiscord = new HashMap<>();
        this.linkFile = new File(configDir, "player-links.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadLinks();
    }

    public static void initialize(File configDir) {
        if (instance == null) {
            instance = new PlayerLinkingManager(configDir);
            LOGGER.info("Initialized PlayerLinkingManager");
        }
    }

    public static PlayerLinkingManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PlayerLinkingManager not initialized");
        }
        return instance;
    }

    /**
     * Add or update a player link
     */
    public void addLink(String discordId, String minecraftUuid, String minecraftName, boolean whitelisted) {
        PlayerLink link = new PlayerLink(discordId, minecraftUuid, minecraftName, whitelisted);
        linksByUuid.put(minecraftUuid, link);
        linksByDiscord.put(discordId, link);
        saveLinks();
        LOGGER.info("Added player link: Discord {} <-> Minecraft {} ({})", discordId, minecraftName, minecraftUuid);
    }

    /**
     * Remove a player link by Minecraft UUID
     */
    public void removeLink(String minecraftUuid) {
        PlayerLink link = linksByUuid.remove(minecraftUuid);
        if (link != null) {
            linksByDiscord.remove(link.getDiscordId());
            saveLinks();
            LOGGER.info("Removed player link for Minecraft UUID: {} ({})", minecraftUuid, link.getMinecraftName());
        }
    }

    /**
     * Get Discord ID from Minecraft UUID
     */
    public Optional<String> getDiscordId(String minecraftUuid) {
        PlayerLink link = linksByUuid.get(minecraftUuid);
        if (link != null && link.isWhitelisted()) {
            return Optional.of(link.getDiscordId());
        }
        return Optional.empty();
    }

    /**
     * Get Minecraft UUID from Discord ID
     */
    public Optional<String> getMinecraftUuid(String discordId) {
        PlayerLink link = linksByDiscord.get(discordId);
        if (link != null && link.isWhitelisted()) {
            return Optional.of(link.getMinecraftUuid());
        }
        return Optional.empty();
    }

    /**
     * Get player link by Minecraft UUID
     */
    public Optional<PlayerLink> getLinkByUuid(String minecraftUuid) {
        return Optional.ofNullable(linksByUuid.get(minecraftUuid));
    }

    /**
     * Get player link by Minecraft name (case-insensitive)
     */
    public Optional<PlayerLink> getLinkByName(String minecraftName) {
        if (minecraftName == null) {
            return Optional.empty();
        }
        for (PlayerLink link : linksByUuid.values()) {
            if (minecraftName.equalsIgnoreCase(link.getMinecraftName())) {
                return Optional.of(link);
            }
        }
        return Optional.empty();
    }

    /**
     * Get player link by Discord ID
     */
    public Optional<PlayerLink> getLinkByDiscord(String discordId) {
        return Optional.ofNullable(linksByDiscord.get(discordId));
    }

    /**
     * Load links from file
     */
    private void loadLinks() {
        if (!linkFile.exists()) {
            LOGGER.info("No existing link file found, starting fresh");
            return;
        }

        try (FileReader reader = new FileReader(linkFile)) {
            Type type = new TypeToken<Map<String, PlayerLink>>() {}.getType();
            Map<String, PlayerLink> loadedLinks = gson.fromJson(reader, type);
            
            if (loadedLinks != null) {
                for (PlayerLink link : loadedLinks.values()) {
                    linksByUuid.put(link.getMinecraftUuid(), link);
                    linksByDiscord.put(link.getDiscordId(), link);
                }
                LOGGER.info("Loaded {} player links from file", loadedLinks.size());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load player links", e);
        }
    }

    /**
     * Save links to file
     */
    private void saveLinks() {
        try {
            linkFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(linkFile)) {
                gson.toJson(linksByUuid, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save player links", e);
        }
    }
}
