package combat.log.discord;

import combat.log.discord.commands.TicketCommands;
import combat.log.discord.config.BotConfig;
import combat.log.discord.discord.TicketManager;
import combat.log.discord.websocket.CombatLogWebSocketServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main class for Combat Log Discord Bot
 */
public class CombatLogBot {
    private static final Logger logger = LoggerFactory.getLogger(CombatLogBot.class);
    
    private final BotConfig config;
    private final JDA jda;
    private final TicketManager ticketManager;
    private final CombatLogWebSocketServer webSocketServer;

    public CombatLogBot(File configFile) throws Exception {
        logger.info("Starting Combat Log Discord Bot...");
        
        // Load configuration
        this.config = BotConfig.load(configFile);
        
        if ("YOUR_BOT_TOKEN_HERE".equals(config.discord.token)) {
            logger.error("Please set your bot token in config.json!");
            throw new IllegalStateException("Bot token not configured");
        }
        
        // Initialize Discord bot
        logger.info("Connecting to Discord...");
        this.jda = JDABuilder.createDefault(config.discord.token)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS
            )
            .build();
        
        this.jda.awaitReady();
        logger.info("Discord bot connected as: {}", jda.getSelfUser().getName());
        
        // Initialize ticket manager
        this.ticketManager = new TicketManager(jda, config);
        
        // Register slash commands
        registerCommands();
        
        // Add command listener
        jda.addEventListener(new TicketCommands(ticketManager));
        
        // Start WebSocket server
        logger.info("Starting WebSocket server on port {}...", config.websocket.port);
        this.webSocketServer = new CombatLogWebSocketServer(config, ticketManager);
        ticketManager.setWebSocketServer(webSocketServer);
        webSocketServer.start();
        
        logger.info("Combat Log Discord Bot is ready!");
        logger.info("WebSocket server listening on {}:{}", config.websocket.host, config.websocket.port);
    }

    /**
     * Register slash commands
     */
    private void registerCommands() {
        jda.updateCommands().addCommands(
            Commands.slash("approve", "Approve a combat log appeal")
                .addOption(OptionType.STRING, "incident_id", "The incident ID", true)
                .addOption(OptionType.STRING, "reason", "Reason for approval", false),
            
            Commands.slash("deny", "Deny a combat log appeal")
                .addOption(OptionType.STRING, "incident_id", "The incident ID", true)
                .addOption(OptionType.STRING, "reason", "Reason for denial", false),
            
            Commands.slash("extend", "Extend the deadline for a ticket")
                .addOption(OptionType.STRING, "incident_id", "The incident ID", true)
                .addOption(OptionType.INTEGER, "minutes", "Minutes to extend", true),
            
            Commands.slash("info", "Get information about a ticket")
                .addOption(OptionType.STRING, "incident_id", "The incident ID", true)
        ).queue();
        
        logger.info("Registered slash commands");
    }

    /**
     * Shutdown the bot
     */
    public void shutdown() {
        logger.info("Shutting down...");
        
        try {
            ticketManager.shutdown();
        } catch (Exception e) {
            logger.error("Error shutting down ticket manager: {}", e.getMessage());
        }
        
        try {
            webSocketServer.stop();
        } catch (Exception e) {
            logger.error("Error stopping WebSocket server: {}", e.getMessage());
        }
        
        try {
            jda.shutdown();
        } catch (Exception e) {
            logger.error("Error shutting down JDA: {}", e.getMessage());
        }
        
        logger.info("Shutdown complete");
    }

    public static void main(String[] args) {
        File configFile = new File("config.json");
        
        try {
            CombatLogBot bot = new CombatLogBot(configFile);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
            
            // Keep running
            logger.info("Bot is running. Press Ctrl+C to stop.");
            
        } catch (Exception e) {
            logger.error("Failed to start bot: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
