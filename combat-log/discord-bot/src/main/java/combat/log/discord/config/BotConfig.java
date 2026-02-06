package combat.log.discord.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration for the Discord bot
 */
public class BotConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // Discord settings - Essential credentials
    public DiscordSettings discord = new DiscordSettings();
    
    // WebSocket settings
    public WebSocketSettings websocket = new WebSocketSettings();
    
    // Feature toggles
    public FeatureSettings features = new FeatureSettings();
    
    // Timeout settings
    public TimeoutSettings timeouts = new TimeoutSettings();
    
    // Channel IDs
    public ChannelSettings channels = new ChannelSettings();
    
    // Ticket-specific settings
    public TicketSettings ticket = new TicketSettings();
    
    // Whitelist-specific settings
    public WhitelistSettings whitelist = new WhitelistSettings();

    public static class DiscordSettings {
        public String token = "YOUR_BOT_TOKEN_HERE";
        public String guildId = "YOUR_GUILD_ID";
        public String staffRoleId = "YOUR_STAFF_ROLE_ID";
    }

    public static class WebSocketSettings {
        public int port = 8080;
        public String host = "0.0.0.0";
    }
    
    public static class FeatureSettings {
        public boolean useForumChannel = true;
        public boolean autoDenyEnabled = true;
        public boolean privateThreads = true;
        public boolean whitelistEnabled = true;
        public boolean mojangApiEnabled = true;
    }
    
    public static class TimeoutSettings {
        public long ticketTimeoutMinutes = 60;
        public long mojangCacheDurationMinutes = 5;
        public int mojangApiTimeoutSeconds = 5;
    }
    
    public static class ChannelSettings {
        public String ticketChannelId = "YOUR_CHANNEL_ID";
        public String whitelistChannelId = "YOUR_WHITELIST_CHANNEL_ID";
        public String reviewChannelId = "YOUR_REVIEW_CHANNEL_ID";
    }

    public static class TicketSettings {
        public String[] acceptedProofPlatforms = {
            "youtube.com", "youtu.be",
            "twitch.tv", "clips.twitch.tv",
            "streamable.com",
            "medal.tv",
            "discord.com/attachments"
        };
    }
    
    public static class WhitelistSettings {
        public ButtonMessageSettings buttonMessage = new ButtonMessageSettings();
    }
    
    public static class ButtonMessageSettings {
        public String title = "🎫 Request Server Whitelist";
        public String description = "Click the button below to request access to our Minecraft server";
        public String color = "#00FF00";
    }

    public static BotConfig load(File configFile) {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                return GSON.fromJson(reader, BotConfig.class);
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        }
        
        // Create default config
        BotConfig config = new BotConfig();
        save(configFile, config);
        return config;
    }

    public static void save(File configFile, BotConfig config) {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
                System.out.println("Saved configuration to " + configFile.getName());
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
}
