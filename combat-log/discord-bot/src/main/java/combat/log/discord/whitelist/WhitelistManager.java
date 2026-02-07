package combat.log.discord.whitelist;

import combat.log.discord.api.MojangAPIService;
import combat.log.discord.api.MojangProfile;
import combat.log.discord.config.BotConfig;
import combat.log.discord.database.LinkingDatabase;
import combat.log.discord.models.PlayerLinkMessage;
import combat.log.discord.models.UnlinkMessage;
import combat.log.discord.models.WhitelistAddMessage;
import combat.log.discord.websocket.CombatLogWebSocketServer;
import combat.log.discord.util.MessageFormatter;
import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.Permission;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
    private final ConcurrentMap<String, PendingWhitelistRequest> pendingRequests = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, PendingUnlinkRequest> pendingUnlinkRequests = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> pendingWhitelistMessages = new ConcurrentLinkedQueue<>();
    // Message ID of the single status message listing whitelisted players
    private volatile String whitelistStatusMessageId;

    public WhitelistManager(JDA jda, BotConfig config, LinkingDatabase database, MojangAPIService mojangAPI) {
        this.jda = jda;
        this.config = config;
        this.database = database;
        this.mojangAPI = mojangAPI;
    }

    public void setWebSocketServer(CombatLogWebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

    public String message(String key, String fallback) {
        return config.message(key, fallback);
    }

    public void handleMinecraftConnected() {
        flushPendingWhitelistMessages();
        // Ensure the persistent whitelist status message exists when Minecraft connects (and bot ready)
        ensureWhitelistStatusMessageExists();
    }

    /**
     * Build an embed representing current whitelist status.
     */
    private net.dv8tion.jda.api.EmbedBuilder buildWhitelistStatusEmbed() {
        java.util.List<String> names = database.listWhitelistedNames();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📜 Whitelisted Players");
        embed.setColor(Color.GREEN);

        if (names.isEmpty()) {
            embed.setDescription("No players are currently whitelisted.");
        } else {
            // Join names with commas, but limit embed size
            String desc = names.stream().collect(Collectors.joining("\n"));
            if (desc.length() > 1900) {
                // Truncate if too long
                desc = desc.substring(0, 1896) + "...";
            }
            embed.setDescription(desc);
            embed.setFooter(String.format("Total: %d", names.size()));
        }

        embed.setTimestamp(Instant.now());
        return embed;
    }

    private void ensureWhitelistStatusMessageExists() {
        TextChannel channel = resolveWhitelistLogChannel();
        if (channel == null || !channel.canTalk()) return;

        // If we already have an ID, try to verify it
        if (whitelistStatusMessageId != null && !whitelistStatusMessageId.isBlank()) {
            try {
                channel.retrieveMessageById(whitelistStatusMessageId).queue(msg -> {
                    // exists — update it
                    updateWhitelistStatusMessage();
                }, err -> {
                    // not found — create new
                    channel.sendMessageEmbeds(buildWhitelistStatusEmbed().build()).queue(m -> whitelistStatusMessageId = m.getId());
                });
                return;
            } catch (Exception ignored) {
                // fallthrough to create
            }
        }

        // Try to find an existing bot-authored status message in recent history
        channel.getHistory().retrievePast(100).queue(list -> {
            for (var m : list) {
                if (m.getAuthor().getId().equals(jda.getSelfUser().getId()) && !m.getEmbeds().isEmpty()) {
                    var e = m.getEmbeds().get(0);
                    if (e.getTitle() != null && e.getTitle().contains("Whitelisted Players")) {
                        whitelistStatusMessageId = m.getId();
                        updateWhitelistStatusMessage();
                        return;
                    }
                }
            }
            // Not found, create one
            channel.sendMessageEmbeds(buildWhitelistStatusEmbed().build()).queue(m -> whitelistStatusMessageId = m.getId());
        }, err -> {
            // On error, attempt to create
            channel.sendMessageEmbeds(buildWhitelistStatusEmbed().build()).queue(m -> whitelistStatusMessageId = m.getId());
        });
    }

    private void updateWhitelistStatusMessage() {
        TextChannel channel = resolveWhitelistLogChannel();
        if (channel == null || !channel.canTalk()) return;

        var embed = buildWhitelistStatusEmbed().build();
        if (whitelistStatusMessageId != null && !whitelistStatusMessageId.isBlank()) {
            channel.editMessageEmbedsById(whitelistStatusMessageId, embed).queue(success -> {
                // updated
            }, err -> {
                // fallback: create new and store id
                channel.sendMessageEmbeds(embed).queue(m -> whitelistStatusMessageId = m.getId());
            });
        } else {
            channel.sendMessageEmbeds(embed).queue(m -> whitelistStatusMessageId = m.getId());
        }
    }

    /**
     * Process a new whitelist request
     */
    public WhitelistResult processRequest(User user, String minecraftName, InteractionHook hook) {
        logger.info("Processing whitelist request from {} for Minecraft name: {}", 
            user.getAsTag(), minecraftName);

        try {
            // Validate username format
            if (!mojangAPI.isValidUsernameFormat(minecraftName)) {
                return WhitelistResult.error(config.message("whitelist.modal.requestInvalidFormat",
                    "❌ Invalid Minecraft username format. Username must be 3-16 characters long and contain only letters, numbers, and underscores."));
            }

            // Check if Discord user already has a pending request (no longer needed with auto-approval, but keep for safety)
            if (database.hasPendingRequest(user.getId())) {
                return WhitelistResult.error(config.message("whitelist.modal.requestAlreadyPending",
                    "❌ You already have a whitelist request being processed. Please wait a moment."));
            }

            // Check if Discord user is already linked
            if (database.isDiscordLinked(user.getId())) {
                return WhitelistResult.error(config.message("whitelist.modal.requestAlreadyLinked",
                    "❌ Your Discord account is already linked to a Minecraft account."));
            }

            // Query Mojang API for UUID
            Optional<MojangProfile> profileOpt = mojangAPI.getProfile(minecraftName);
            if (profileOpt.isEmpty()) {
                return WhitelistResult.error(config.message("whitelist.modal.requestNameNotFound",
                    "❌ Minecraft username not found. Please check the spelling and try again."));
            }

            MojangProfile profile = profileOpt.get();
            String minecraftUuid = profile.getFormattedUuid();

            // Check if Minecraft account is already linked
            if (database.isMinecraftLinked(minecraftUuid)) {
                return WhitelistResult.error(config.message("whitelist.modal.requestMinecraftLinked",
                    "❌ This Minecraft account is already linked to another Discord account."));
            }

            // Automatically approve and whitelist the player
            // Store link in database
            try {
                database.addLink(
                    user.getId(),
                    minecraftUuid,
                    profile.getName(),
                    "AUTO_APPROVED",
                    "Automatic whitelist approval"
                );
                logger.info("Stored link: Discord {} <-> Minecraft {} ({})", 
                    user.getId(), profile.getName(), minecraftUuid);
            } catch (SQLException e) {
                logger.error("Failed to store player link", e);
                return WhitelistResult.error(config.message("whitelist.modal.linkError",
                    "❌ An error occurred while linking your account. Please try again later."));
            }

            // Send whitelist command to Minecraft
            String requestId = UUID.randomUUID().toString();
            PendingWhitelistRequest pending = new PendingWhitelistRequest(
                user.getId(),
                user.getAsTag(),
                profile.getName(),
                minecraftUuid,
                System.currentTimeMillis(),
                hook
            );
            pendingRequests.put(requestId, pending);

            WhitelistAddMessage whitelistMsg = new WhitelistAddMessage(
                requestId,
                profile.getName(),
                minecraftUuid,
                user.getId(),
                "AUTO_APPROVED"
            );
            String json = new Gson().toJson(whitelistMsg);
            sendOrQueueWhitelistMessage(json);
            logger.info("Sent whitelist command to Minecraft for: {}", profile.getName());

            // Also send link message
            PlayerLinkMessage linkMsg = new PlayerLinkMessage(
                user.getId(),
                minecraftUuid,
                profile.getName(),
                true
            );
            json = new Gson().toJson(linkMsg);
            sendOrQueueWhitelistMessage(json);

            // Update the persistent whitelist status message
            updateWhitelistStatusMessage();

            return WhitelistResult.success(config.message("whitelist.modal.waiting",
                "⏳ Waiting for Minecraft confirmation..."));
        } catch (Exception e) {
            logger.error("Failed to process whitelist request", e);
            return WhitelistResult.error(config.message("whitelist.modal.processError",
                "❌ An error occurred while processing your request. Please try again later."));
        }
    }

    /**
     * Handle whitelist confirmation from Minecraft
     */
    public void handleWhitelistConfirmation(combat.log.discord.models.WhitelistConfirmation confirmation) {
        PendingWhitelistRequest pending = pendingRequests.remove(confirmation.getRequestId());
        String playerName = confirmation.getPlayerName();

        if (pending == null) {
            logger.warn("No pending request found for confirmation: {}", confirmation.getRequestId());
            return;
        }

        updateWhitelistRequestLog(pending, confirmation.isSuccess(), confirmation.getError());

        String message = confirmation.isSuccess()
            ? MessageFormatter.format(
                config.message("whitelist.confirm.success", "✅ Whitelist successful for **{playerName}**."),
                Map.of("playerName", playerName)
            )
            : MessageFormatter.format(
                config.message("whitelist.confirm.fail", "❌ Whitelist failed for **{playerName}**: {error}"),
                Map.of(
                    "playerName", playerName,
                    "error", confirmation.getError() != null ? confirmation.getError() : "Unknown error"
                )
            );

        if (pending.hook != null) {
            pending.hook.editOriginal(message).queue();
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

            if (!config.features.whitelistEnabled || config.channels.reviewChannelId == null) {
                logger.warn("Whitelist not configured properly");
                return;
            }

            // Get review channel
            TextChannel reviewChannel = guild.getTextChannelById(config.channels.reviewChannelId);
            if (reviewChannel == null) {
                logger.error("Review channel not found: {}", config.channels.reviewChannelId);
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
            Button approveButton = Button.success(
                "whitelist_approve:" + request.getRequestId(),
                resolveLabel(config.buttons.whitelist.approve, "✅ Approve")
            );
            Button denyButton = Button.danger(
                "whitelist_deny:" + request.getRequestId(),
                resolveLabel(config.buttons.whitelist.deny, "❌ Deny")
            );

            // Create thread
            String threadName = "Whitelist: " + request.getMinecraftName();
            ThreadChannel thread = reviewChannel.createThreadChannel(threadName).complete();
            
            // Post message with buttons
            thread.sendMessageEmbeds(embed.build())
                .setActionRow(approveButton, denyButton)
                .complete();

            // Tag staff role if configured
            if (config.discord.staffRoleId != null) {
                thread.sendMessage("<@&" + config.discord.staffRoleId + ">").queue();
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

            // Update persistent whitelist status message after approval
            updateWhitelistStatusMessage();

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

    public static class WhitelistResult {
        public final boolean success;
        public final String message;

        private WhitelistResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static WhitelistResult success(String message) {
            return new WhitelistResult(true, message);
        }

        public static WhitelistResult error(String message) {
            return new WhitelistResult(false, message);
        }
    }

    /**
     * Setup whitelist channel with button
     */
    public void setupWhitelistChannel(String channelId) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            logger.error("Channel not found: {}", channelId);
            throw new IllegalStateException("Channel not found: " + channelId);
        }

        // Check bot has permission to send messages in the channel
        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_SEND)) {
            logger.error("Missing MESSAGE_SEND permission for channel: {}", channelId);
            throw new IllegalStateException("Missing MESSAGE_SEND permission for channel: " + channelId);
        }

        try {

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(config.whitelist.buttonMessage.title)
                .setDescription(config.whitelist.buttonMessage.description)
                .setColor(Color.decode(config.whitelist.buttonMessage.color))
                .setFooter("Click the button below to request whitelist access");

            Button requestButton = Button.primary(
                "whitelist_request",
                resolveLabel(config.buttons.whitelist.request, "🎫 Request Whitelist")
            );

            Button unlinkButton = Button.danger(
                "whitelist_unlink",
                resolveLabel(config.buttons.whitelist.unlink, "🔓 Unlink")
            );

            channel.sendMessageEmbeds(embed.build())
                .setActionRow(requestButton, unlinkButton)
                .queue();

            logger.info("Setup whitelist channel: {}", channelId);
        } catch (Exception e) {
            logger.error("Failed to setup whitelist channel", e);
            throw e;
        }
    }

    private String resolveLabel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    /**
     * Unlink the Discord user from their Minecraft account
     */
    public WhitelistResult unlinkDiscord(User user) {
        try {
            Optional<String> uuidOpt = database.getMinecraftUuid(user.getId());
            if (uuidOpt.isEmpty()) {
                return WhitelistResult.error(config.message("whitelist.unlink.notLinked",
                    "❌ Your Discord account is not linked."));
            }

            String minecraftUuid = uuidOpt.get();
            Optional<String> nameOpt = database.removeLink(minecraftUuid);
            if (nameOpt.isEmpty()) {
                return WhitelistResult.error(config.message("whitelist.unlink.notFound",
                    "❌ No linked Minecraft account found to unlink."));
            }

            String minecraftName = nameOpt.get();
            if (webSocketServer != null && webSocketServer.isMinecraftConnected()) {
                UnlinkMessage unlinkMessage = new UnlinkMessage(minecraftUuid, minecraftName);
                String json = new Gson().toJson(unlinkMessage);
                sendOrQueueWhitelistMessage(json);
                logger.info("Sent unlink command to Minecraft for: {}", minecraftName);
            } else {
                UnlinkMessage unlinkMessage = new UnlinkMessage(minecraftUuid, minecraftName);
                String json = new Gson().toJson(unlinkMessage);
                sendOrQueueWhitelistMessage(json);
                logger.warn("Cannot send unlink command - Minecraft not connected");
            }
            // Log unlink request to staff-facing whitelist log channel and track pending confirmation
            PendingUnlinkRequest pending = new PendingUnlinkRequest(user.getId(), minecraftName);
            pendingUnlinkRequests.put(minecraftName.toLowerCase(), pending);

            TextChannel logChannel = resolveWhitelistLogChannel();
            if (logChannel != null && logChannel.canTalk()) {
                String title = config.message("whitelist.log.remove.title", "🗑️ Whitelist Removed");
                EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔁 Unlink Requested")
                    .setDescription("Discord: <@" + user.getId() + ">\nMinecraft: " + minecraftName)
                    .setColor(Color.ORANGE)
                    .setTimestamp(Instant.now());

                logChannel.sendMessageEmbeds(embed.build()).queue(msg -> pending.channelMessageId = msg.getId());
            }

            // Update persistent whitelist status message after unlinking
            updateWhitelistStatusMessage();

            String unlinkSuccess = MessageFormatter.format(
                config.message("whitelist.unlink.success", "✅ Your Discord account has been unlinked from {minecraftName}."),
                Map.of("minecraftName", minecraftName)
            );
            return WhitelistResult.success(unlinkSuccess);
        } catch (Exception e) {
            logger.error("Failed to unlink Discord account", e);
            return WhitelistResult.error(config.message("whitelist.unlink.error",
                "❌ Failed to unlink your account. Please try again later."));
        }
    }

    /**
     * Handle unlink initiated from Minecraft
     */
    public void handleMinecraftUnlink(UnlinkMessage message) {
    }

    public void handleWhitelistRemoveConfirmation(combat.log.discord.models.WhitelistRemoveConfirmation confirmation) {
        String playerName = confirmation.getPlayerName();
        PendingUnlinkRequest pending = pendingUnlinkRequests.remove(playerName.toLowerCase());

        if (pending != null) {
            updateUnlinkChannelMessage(pending, confirmation.isSuccess(), confirmation.getError());
        }

        if (!confirmation.isSuccess()) {
            return;
        }

        String removeTitle = config.message("whitelist.log.remove.title", "🗑️ Whitelist Removed");
        String removeDesc = MessageFormatter.format(
            config.message("whitelist.log.remove.desc", "Minecraft: {playerName}"),
            Map.of("playerName", playerName)
        );
        logWhitelistEvent(removeTitle, removeDesc, Color.ORANGE);
    }

    private void logWhitelistEvent(String title, String description, Color color) {
        try {
            TextChannel channel = resolveWhitelistLogChannel();
            if (channel == null) {
                logger.warn("Whitelist log channel not configured");
                return;
            }

            if (!channel.canTalk()) {
                logger.warn("Missing permission to write in whitelist log channel: {}", channel.getId());
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            logger.warn("Failed to log whitelist event: {}", e.getMessage());
        }
    }

    private void enqueueUnlinkChannelMessage(String discordId, String minecraftName) {
        String waitingText = MessageFormatter.format(
            config.message("whitelist.unlink.waiting", "⏳ Waiting for Minecraft to confirm unlink for **{minecraftName}**..."),
            Map.of("minecraftName", minecraftName, "discordId", discordId)
        );

        PendingUnlinkRequest pending = new PendingUnlinkRequest(discordId, minecraftName);
        pendingUnlinkRequests.put(minecraftName.toLowerCase(), pending);

        // Post waiting message to the whitelist log/review channel (staff-facing)
        TextChannel channel = resolveWhitelistLogChannel();
        if (channel == null || !channel.canTalk()) {
            return;
        }

        channel.sendMessage(waitingText).queue(message -> pending.channelMessageId = message.getId());
    }

    private void updateUnlinkChannelMessage(PendingUnlinkRequest pending, boolean success, String error) {
        TextChannel channel = resolveWhitelistLogChannel();
        if (channel == null || !channel.canTalk()) {
            return;
        }

        if (pending.channelMessageId == null || pending.channelMessageId.isBlank()) {
            return;
        }

        String template = success
            ? config.message("whitelist.unlink.confirm.success", "✅ Unlinked successfully for **{minecraftName}**.")
            : config.message("whitelist.unlink.confirm.fail", "❌ Unlink failed for **{minecraftName}**: {error}");

        String message = MessageFormatter.format(template, Map.of(
            "minecraftName", pending.minecraftName,
            "error", error != null && !error.isBlank() ? error : "Unknown error",
            "discordId", pending.discordId
        ));

        channel.editMessageById(pending.channelMessageId, message).queue();
    }

    private void logWhitelistRequest(PendingWhitelistRequest pending) {
        // Intentionally left blank to disable request logging.
    }

    private void updateWhitelistRequestLog(PendingWhitelistRequest pending, boolean success, String error) {
        TextChannel channel = resolveWhitelistLogChannel();
        if (channel == null || !channel.canTalk()) {
            return;
        }

        String title = success
            ? config.message("whitelist.log.success.title", "✅ Whitelist Successful")
            : config.message("whitelist.log.fail.title", "❌ Whitelist Failed");
        String description = buildWhitelistResultDescription(pending, success, error);

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(success ? Color.GREEN : Color.RED)
            .setTimestamp(Instant.now());

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private String buildWhitelistResultDescription(PendingWhitelistRequest pending, boolean success, String error) {
        String errorText = error != null && !error.isBlank() ? error : "Unknown error";
        String template = success
            ? config.message("whitelist.log.success.desc", "Discord: <@{discordId}>\nMinecraft: {playerName}")
            : config.message("whitelist.log.fail.desc", "Discord: <@{discordId}>\nMinecraft: {playerName}\nError: {error}");

        return MessageFormatter.format(template, Map.of(
            "discordId", pending.discordId,
            "playerName", pending.minecraftName,
            "error", errorText
        ));
    }

    private void sendOrQueueWhitelistMessage(String json) {
        if (webSocketServer != null && webSocketServer.isMinecraftConnected()) {
            webSocketServer.broadcast(json);
            return;
        }

        pendingWhitelistMessages.offer(json);
    }

    private void flushPendingWhitelistMessages() {
        if (webSocketServer == null || !webSocketServer.isMinecraftConnected()) {
            return;
        }

        String message;
        while ((message = pendingWhitelistMessages.poll()) != null) {
            webSocketServer.broadcast(message);
        }
    }

    private TextChannel resolveWhitelistLogChannel() {
        Guild guild = jda.getGuildById(config.discord.guildId);
        if (guild == null) {
            logger.error("Guild not found: {}", config.discord.guildId);
            return null;
        }

        String channelId = config.channels.whitelistLogChannelId;
        if (channelId == null || channelId.isBlank()) {
            channelId = config.channels.reviewChannelId;
        }

        if (channelId == null || channelId.isBlank()) {
            return null;
        }

        return guild.getTextChannelById(channelId);
    }

    private TextChannel resolveWhitelistChannel() {
        Guild guild = jda.getGuildById(config.discord.guildId);
        if (guild == null) {
            logger.error("Guild not found: {}", config.discord.guildId);
            return null;
        }

        String channelId = config.channels.whitelistChannelId;
        if (channelId == null || channelId.isBlank()) {
            return null;
        }

        return guild.getTextChannelById(channelId);
    }

    private static class PendingWhitelistRequest {
        private final String discordId;
        private final String discordTag;
        private final String minecraftName;
        private final String minecraftUuid;
        private final long requestedAt;
        private final InteractionHook hook;

        private PendingWhitelistRequest(
            String discordId,
            String discordTag,
            String minecraftName,
            String minecraftUuid,
            long requestedAt,
            InteractionHook hook
        ) {
            this.discordId = discordId;
            this.discordTag = discordTag;
            this.minecraftName = minecraftName;
            this.minecraftUuid = minecraftUuid;
            this.requestedAt = requestedAt;
            this.hook = hook;
        }
    }

    private static class PendingUnlinkRequest {
        private final String discordId;
        private final String minecraftName;
        private String channelMessageId;

        private PendingUnlinkRequest(String discordId, String minecraftName) {
            this.discordId = discordId;
            this.minecraftName = minecraftName;
        }
    }
}
