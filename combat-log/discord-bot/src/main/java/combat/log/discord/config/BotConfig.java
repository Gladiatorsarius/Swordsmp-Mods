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
    
    // Discord settings
    public DiscordSettings discord = new DiscordSettings();
    
    // WebSocket settings
    public WebSocketSettings websocket = new WebSocketSettings();
    
    // Ticket settings
    public TicketSettings ticket = new TicketSettings();
    
    // DiscordSRV integration settings
    public DiscordSRVSettings discordSRV = new DiscordSRVSettings();

    public static class DiscordSettings {
        public String token = "YOUR_BOT_TOKEN_HERE";
        public String guildId = "YOUR_GUILD_ID";
        public String ticketChannelId = "YOUR_CHANNEL_ID";
        public String staffRoleId = "YOUR_STAFF_ROLE_ID";
        public boolean useForumChannel = true; // Use forum channels for tickets
    }

    public static class WebSocketSettings {
        public int port = 8080;
        public String host = "0.0.0.0";
    }

    public static class TicketSettings {
        public long timeoutMinutes = 60;
        public boolean autoDenyEnabled = true;
        public boolean privateThreads = true; // Create private threads for linked players
        public String[] acceptedProofPlatforms = {
            "youtube.com", "youtu.be",
            "twitch.tv", "clips.twitch.tv",
            "streamable.com",
            "medal.tv",
            "discord.com/attachments"
        };
    }
    
    public static class DiscordSRVSettings {
        public boolean enabled = false;
        public String databaseType = "sqlite"; // "sqlite" or "mysql"
        public String databasePath = "/path/to/plugins/DiscordSRV/accounts.db";
        public MySQLSettings mysql = new MySQLSettings();
    }
    
    public static class MySQLSettings {
        public String host = "localhost";
        public int port = 3306;
        public String database = "discordsrv";
        public String username = "root";
        public String password = "password";
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
