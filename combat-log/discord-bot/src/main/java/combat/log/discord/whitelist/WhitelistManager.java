package combat.log.discord.whitelist;

import combat.log.discord.api.MojangAPIService;
import combat.log.discord.api.MojangProfile;
import combat.log.discord.config.BotConfig;
import combat.log.discord.database.LinkingDatabase;
import combat.log.discord.models.PlayerLinkMessage;
import combat.log.discord.models.WhitelistAddMessage;
import combat.log.discord.websocket.CombatLogWebSocketServer;
import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages whitelist requests and approvals
 */
public class WhitelistManager {
    private static final Logger logger = LoggerFactory.getLogger(WhitelistManager.class);
    
    private final JDA jda;
    private final BotConfig config;
    private final LinkingDatabase database;
    private final MojangAPIService mojangAPI;
    private CombatLogWebSocketServer webSocketServer;

    public WhitelistManager(JDA jda, BotConfig config, LinkingDatabase database, MojangAPIService mojangAPI) {
        this.jda = jda;
        this.config = config;
        this.database = database;
        this.mojangAPI = mojangAPI;
    }

    public void setWebSocketServer(CombatLogWebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

    /**
     * Process a new whitelist request
     */
    public void processRequest(User user, String minecraftName) {
        logger.info("Processing whitelist request from {} for Minecraft name: {}", 
            user.getAsTag(), minecraftName);

        try {
            // Validate username format
            if (!mojangAPI.isValidUsernameFormat(minecraftName)) {
                sendErrorDM(user, "Invalid Minecraft username format. Username must be 3-16 characters long and contain only letters, numbers, and underscores.");
                return;
            }

            // Check if Discord user already has a pending request
            if (database.hasPendingRequest(user.getId())) {
                sendErrorDM(user, "You already have a pending whitelist request. Please wait for staff to review it.");
                return;
            }

            // Check if Discord user is already linked
            if (database.isDiscordLinked(user.getId())) {
                sendErrorDM(user, "Your Discord account is already linked to a Minecraft account.");
                return;
            }

            // Query Mojang API for UUID
            Optional<MojangProfile> profileOpt = mojangAPI.getProfile(minecraftName);
            if (profileOpt.isEmpty()) {
                sendErrorDM(user, "Minecraft username not found. Please check the spelling and try again.");
                return;
            }

            MojangProfile profile = profileOpt.get();
            String minecraftUuid = profile.getFormattedUuid();

            // Check if Minecraft account is already linked
            if (database.isMinecraftLinked(minecraftUuid)) {
                sendErrorDM(user, "This Minecraft account is already linked to another Discord account.");
                return;
            }

            // Create request object
            WhitelistRequest request = new WhitelistRequest(
                user.getId(),
                user.getAsTag(),
                profile.getName(),
                minecraftUuid
            );

            // Create staff review thread
            createReviewThread(request);

            // Send confirmation DM to user
            sendConfirmationDM(user, profile.getName());

        } catch (Exception e) {
            logger.error("Failed to process whitelist request", e);
            sendErrorDM(user, "An error occurred while processing your request. Please try again later.");
        }
    }

    /**
     * Create a review thread for staff
     */
    private void createReviewThread(WhitelistRequest request) {
        try {
            Guild guild = jda.getGuildById(config.discord.guildId);
            if (guild == null) {
                logger.error("Guild not found: {}", config.discord.guildId);
                return;
            }

            if (!config.whitelist.enabled || config.whitelist.reviewChannelId == null) {
                logger.warn("Whitelist not configured properly");
                return;
            }

            // Get review channel
            TextChannel reviewChannel = guild.getTextChannelById(config.whitelist.reviewChannelId);
            if (reviewChannel == null) {
                logger.error("Review channel not found: {}", config.whitelist.reviewChannelId);
                return;
            }

            // Create embed for review
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎫 New Whitelist Request")
                .setColor(Color.YELLOW)
                .addField("Discord User", "<@" + request.getDiscordId() + "> (" + request.getDiscordUsername() + ")", false)
                .addField("Minecraft Username", request.getMinecraftName(), true)
                .addField("Minecraft UUID", "`" + request.getMinecraftUuid() + "`", true)
                .addField("Request ID", "`" + request.getRequestId() + "`", false)
                .setTimestamp(Instant.ofEpochMilli(request.getRequestedAt()))
                .setFooter("Whitelist Request System");

            // Create buttons
            Button approveButton = Button.success("whitelist_approve:" + request.getRequestId(), "✅ Approve");
            Button denyButton = Button.danger("whitelist_deny:" + request.getRequestId(), "❌ Deny");

            // Create thread
            String threadName = "Whitelist: " + request.getMinecraftName();
            ThreadChannel thread = reviewChannel.createThreadChannel(threadName).complete();
            
            // Post message with buttons
            thread.sendMessageEmbeds(embed.build())
                .setActionRow(approveButton, denyButton)
                .complete();

            // Tag staff role if configured
            if (config.whitelist.staffRoleId != null) {
                thread.sendMessage("<@&" + config.whitelist.staffRoleId + ">").queue();
            }

            // Store request in database
            request.setThreadId(thread.getId());
            database.createRequest(
                request.getRequestId(),
                request.getDiscordId(),
                request.getDiscordUsername(),
                request.getMinecraftName(),
                request.getMinecraftUuid(),
                thread.getId()
            );

            logger.info("Created review thread for request: {}", request.getRequestId());

        } catch (SQLException e) {
            logger.error("Failed to create review thread", e);
        }
    }

    /**
     * Approve a whitelist request
     */
    public void approveRequest(String requestId, String staffId, String staffName) {
        logger.info("Approving whitelist request: {} by {}", requestId, staffName);

        try {
            // Get request from database
            Optional<LinkingDatabase.WhitelistRequestData> requestOpt = database.getRequest(requestId);
            if (requestOpt.isEmpty()) {
                logger.error("Request not found: {}", requestId);
                return;
            }

            LinkingDatabase.WhitelistRequestData request = requestOpt.get();

            if (!"PENDING".equals(request.status)) {
                logger.warn("Request {} is not pending (status: {})", requestId, request.status);
                return;
            }

            // Store link in database
            database.addLink(
                request.discordId,
                request.minecraftUuid,
                request.minecraftName,
                staffId,
                "Approved whitelist request"
            );

            // Update request status
            database.updateRequestStatus(requestId, "APPROVED", staffId, "Approved by " + staffName);

            // Send whitelist command to Minecraft
            if (webSocketServer != null && webSocketServer.isMinecraftConnected()) {
                WhitelistAddMessage whitelistMsg = new WhitelistAddMessage(
                    requestId,
                    request.minecraftName,
                    request.minecraftUuid,
                    request.discordId,
                    staffId
                );
                String json = new Gson().toJson(whitelistMsg);
                webSocketServer.broadcast(json);
                logger.info("Sent whitelist command to Minecraft for: {}", request.minecraftName);

                // Also send link message
                PlayerLinkMessage linkMsg = new PlayerLinkMessage(
                    request.discordId,
                    request.minecraftUuid,
                    request.minecraftName,
                    true
                );
                json = new Gson().toJson(linkMsg);
                webSocketServer.broadcast(json);
            } else {
                logger.warn("Cannot send whitelist command - Minecraft not connected");
            }

            // Send approval DM to user
            jda.retrieveUserById(request.discordId).queue(user -> {
                sendApprovalDM(user, request.minecraftName);
            });

            // Update thread
            updateReviewThread(request.threadId, true, staffName);

        } catch (SQLException e) {
            logger.error("Failed to approve request", e);
        }
    }

    /**
     * Deny a whitelist request
     */
    public void denyRequest(String requestId, String staffId, String staffName, String reason) {
        logger.info("Denying whitelist request: {} by {} - Reason: {}", requestId, staffName, reason);

        try {
            // Get request from database
            Optional<LinkingDatabase.WhitelistRequestData> requestOpt = database.getRequest(requestId);
            if (requestOpt.isEmpty()) {
                logger.error("Request not found: {}", requestId);
                return;
            }

            LinkingDatabase.WhitelistRequestData request = requestOpt.get();

            if (!"PENDING".equals(request.status)) {
                logger.warn("Request {} is not pending (status: {})", requestId, request.status);
                return;
            }

            // Update request status
            database.updateRequestStatus(requestId, "DENIED", staffId, reason);

            // Send denial DM to user
            jda.retrieveUserById(request.discordId).queue(user -> {
                sendDenialDM(user, request.minecraftName, reason);
            });

            // Update and close thread
            updateReviewThread(request.threadId, false, staffName);

        } catch (SQLException e) {
            logger.error("Failed to deny request", e);
        }
    }

    /**
     * Update review thread with decision
     */
    private void updateReviewThread(String threadId, boolean approved, String staffName) {
        try {
            ThreadChannel thread = jda.getThreadChannelById(threadId);
            if (thread == null) {
                logger.warn("Thread not found: {}", threadId);
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                .setTimestamp(Instant.now());

            if (approved) {
                embed.setTitle("✅ Request Approved")
                    .setColor(Color.GREEN)
                    .setDescription("This whitelist request has been approved by " + staffName)
                    .addField("Status", "Player whitelisted and link stored", false);
            } else {
                embed.setTitle("❌ Request Denied")
                    .setColor(Color.RED)
                    .setDescription("This whitelist request has been denied by " + staffName);
            }

            thread.sendMessageEmbeds(embed.build()).queue(msg -> {
                // Archive thread after posting
                thread.getManager().setArchived(true).queue();
            });

        } catch (Exception e) {
            logger.error("Failed to update review thread", e);
        }
    }

    /**
     * Send confirmation DM to user
     */
    private void sendConfirmationDM(User user, String minecraftName) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("🎫 Whitelist Request Submitted")
            .setColor(Color.BLUE)
            .setDescription("Your whitelist request has been submitted successfully!")
            .addField("Minecraft Username", minecraftName, false)
            .addField("Status", "Pending staff review", false)
            .setFooter("You will be notified when your request is reviewed");

        user.openPrivateChannel().queue(channel -> {
            channel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    /**
     * Send approval DM to user
     */
    private void sendApprovalDM(User user, String minecraftName) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("✅ Whitelist Request Approved")
            .setColor(Color.GREEN)
            .setDescription("Congratulations! Your whitelist request has been approved.")
            .addField("Minecraft Username", minecraftName, false)
            .addField("Next Steps", "You can now join the Minecraft server! Your Discord and Minecraft accounts are now linked.", false);

        user.openPrivateChannel().queue(channel -> {
            channel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    /**
     * Send denial DM to user
     */
    private void sendDenialDM(User user, String minecraftName, String reason) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("❌ Whitelist Request Denied")
            .setColor(Color.RED)
            .setDescription("Unfortunately, your whitelist request has been denied.")
            .addField("Minecraft Username", minecraftName, false);

        if (reason != null && !reason.isEmpty()) {
            embed.addField("Reason", reason, false);
        }

        user.openPrivateChannel().queue(channel -> {
            channel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    /**
     * Send error DM to user
     */
    private void sendErrorDM(User user, String error) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("⚠️ Whitelist Request Error")
            .setColor(Color.ORANGE)
            .setDescription(error);

        user.openPrivateChannel().queue(
            channel -> channel.sendMessageEmbeds(embed.build()).queue(),
            failure -> logger.warn("Could not send error DM to user: {}", user.getId())
        );
    }

    /**
     * Setup whitelist channel with button
     */
    public void setupWhitelistChannel(String channelId) {
        try {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                logger.error("Channel not found: {}", channelId);
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(config.whitelist.buttonMessage.title)
                .setDescription(config.whitelist.buttonMessage.description)
                .setColor(Color.decode(config.whitelist.buttonMessage.color))
                .setFooter("Click the button below to request whitelist access");

            Button requestButton = Button.primary("whitelist_request", "🎫 Request Whitelist");

            channel.sendMessageEmbeds(embed.build())
                .setActionRow(requestButton)
                .queue();

            logger.info("Setup whitelist channel: {}", channelId);

        } catch (Exception e) {
            logger.error("Failed to setup whitelist channel", e);
        }
    }
}
