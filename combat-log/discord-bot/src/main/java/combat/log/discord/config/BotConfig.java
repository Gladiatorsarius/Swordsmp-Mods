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
    
    // Whitelist system settings
    public WhitelistSettings whitelist = new WhitelistSettings();
    
    // Mojang API settings
    public MojangApiSettings mojangApi = new MojangApiSettings();

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
    
    public static class WhitelistSettings {
        public boolean enabled = true;
        public String whitelistChannelId = "YOUR_WHITELIST_CHANNEL_ID";
        public String reviewChannelId = "YOUR_REVIEW_CHANNEL_ID";
        public String staffRoleId = "YOUR_STAFF_ROLE_ID";
        public ButtonMessageSettings buttonMessage = new ButtonMessageSettings();
    }
    
    public static class ButtonMessageSettings {
        public String title = "🎫 Request Server Whitelist";
        public String description = "Click the button below to request access to our Minecraft server";
        public String color = "#00FF00";
    }
    
    public static class MojangApiSettings {
        public boolean enabled = true;
        public long cacheDurationMinutes = 5;
        public int timeoutSeconds = 5;
    }
    
    public static class LinkingSettings {
        public String databasePath = "./database/whitelist.db";
        public boolean allowMultipleMinecraftAccounts = false;
        public boolean allowMultipleDiscordAccounts = false;
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
