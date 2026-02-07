package combat.log.discord.discord;

import combat.log.discord.config.BotConfig;
import combat.log.discord.models.CombatLogIncident;
import combat.log.discord.models.IncidentDecision;
import combat.log.discord.models.Ticket;
import combat.log.discord.util.MessageFormatter;
import combat.log.discord.websocket.CombatLogWebSocketServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages Discord tickets for combat log incidents
 */
public class TicketManager {
    private static final Logger logger = LoggerFactory.getLogger(TicketManager.class);
    private static final long DENIED_THREAD_DELETE_DELAY_MINUTES = 60;
    
    private final JDA jda;
    private final BotConfig config;
    private final combat.log.discord.database.LinkingDatabase linkingDatabase;
    private CombatLogWebSocketServer webSocketServer;
    
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TicketManager(JDA jda, BotConfig config, combat.log.discord.database.LinkingDatabase linkingDatabase) {
        this.jda = jda;
        this.config = config;
        this.linkingDatabase = linkingDatabase;
        
        // Start timeout checker
        scheduler.scheduleAtFixedRate(this::checkTimeouts, 1, 1, TimeUnit.MINUTES);
    }

    public void setWebSocketServer(CombatLogWebSocketServer server) {
        this.webSocketServer = server;
    }

    /**
     * Create a new ticket for a combat log incident
     */
    public void createTicket(CombatLogIncident incident) {
        try {
            Guild guild = jda.getGuildById(config.discord.guildId);
            if (guild == null) {
                logger.error("Guild not found: {}", config.discord.guildId);
                return;
            }

            String channelId;
            if (config.features.useForumChannel) {
                channelId = createForumTicket(guild, incident);
            } else {
                channelId = createThreadTicket(guild, incident);
            }

            if (channelId != null) {
                Ticket ticket = new Ticket(
                    incident.getIncidentId(),
                    incident.getPlayerUuid(),
                    incident.getPlayerName(),
                    incident.getCombatTimeRemaining(),
                    channelId,
                    config.timeouts.ticketTimeoutMinutes
                );
                
                activeTickets.put(incident.getIncidentId(), ticket);
                logger.info("Created ticket {} for player {}", incident.getIncidentId(), incident.getPlayerName());
            }
        } catch (Exception e) {
            logger.error("Failed to create ticket: {}", e.getMessage(), e);
        }
    }

    /**
     * Create ticket as forum post
     */
    private String createForumTicket(Guild guild, CombatLogIncident incident) {
        try {
            ForumChannel forum = guild.getForumChannelById(config.channels.ticketChannelId);
            if (forum == null) {
                logger.error("Forum channel not found: {}", config.channels.ticketChannelId);
                return null;
            }

            String title = MessageFormatter.format(
                config.message("ticket.thread.forumTitle", "🚨 Combat Log: {playerName}"),
                Map.of("playerName", incident.getPlayerName())
            );
            
            // Look up Discord user from LinkingDatabase
            String discordId = linkingDatabase.getDiscordId(incident.getPlayerUuid()).orElse(null);
            
            User linkedUser = null;
            if (discordId != null) {
                try {
                    linkedUser = jda.retrieveUserById(discordId).complete();
                    logger.info("Found linked Discord user for {}: {}", incident.getPlayerName(), linkedUser.getName());
                } catch (Exception e) {
                    logger.warn("Failed to retrieve Discord user {}: {}", discordId, e.getMessage());
                }
            }
            
            MessageEmbed embed = createIncidentEmbed(incident, linkedUser);
            
            MessageCreateBuilder builder = new MessageCreateBuilder();
            builder.setEmbeds(embed);
            
            var forumPost = forum.createForumPost(title, builder.build()).complete();
            ThreadChannel thread = forumPost.getThreadChannel();
            
            // Make thread private and add player if linked
            if (config.features.privateThreads && linkedUser != null) {
                try {
                    // Add linked player to thread
                    final User finalLinkedUser = linkedUser;  // Make effectively final for lambda
                    thread.addThreadMember(linkedUser).queue(
                        success -> logger.info("Added {} to private thread", finalLinkedUser.getName()),
                        error -> logger.warn("Failed to add user to thread: {}", error.getMessage())
                    );
                    
                    // Send DM to player
                    sendPlayerNotification(linkedUser, thread, incident);
                } catch (Exception e) {
                    logger.warn("Failed to make thread private: {}", e.getMessage());
                }
            }
            
            // Send instructions with action buttons
            thread.sendMessage(buildInstructionsMessage(linkedUser))
                .setComponents(createActionButtons(incident.getIncidentId()))
                .queue();

            // Tag staff if role exists (text channel path)
            if (config.discord.staffRoleId != null && !config.discord.staffRoleId.isEmpty()) {
                Role staffRole = guild.getRoleById(config.discord.staffRoleId);
                if (staffRole != null) {
                    String staffMessage = MessageFormatter.format(
                        config.message("ticket.staff.alert", "{staffMention} - New combat log incident!"),
                        Map.of("staffMention", staffRole.getAsMention())
                    );
                    thread.sendMessage(staffMessage).queue();
                }
            }
            
            // Tag staff if role exists
            if (config.discord.staffRoleId != null && !config.discord.staffRoleId.isEmpty()) {
                Role staffRole = guild.getRoleById(config.discord.staffRoleId);
                if (staffRole != null) {
                    String staffMessage = MessageFormatter.format(
                        config.message("ticket.staff.alert", "{staffMention} - New combat log incident!"),
                        Map.of("staffMention", staffRole.getAsMention())
                    );
                    thread.sendMessage(staffMessage).queue();
                }
            }
            
            // Tag linked player in thread
            if (linkedUser != null) {
                String reviewMessage = MessageFormatter.format(
                    config.message("ticket.player.reviewForum", "{userMention} - Please review the incident and submit proof if needed."),
                    Map.of("userMention", linkedUser.getAsMention())
                );
                thread.sendMessage(reviewMessage).queue();
            }
            
            return thread.getId();
        } catch (Exception e) {
            logger.error("Failed to create forum ticket: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create ticket as thread in text channel
     */
    private String createThreadTicket(Guild guild, CombatLogIncident incident) {
        try {
            var channel = guild.getTextChannelById(config.channels.ticketChannelId);
            if (channel == null) {
                logger.error("Text channel not found: {}", config.channels.ticketChannelId);
                return null;
            }

            String title = MessageFormatter.format(
                config.message("ticket.thread.textTitle", "Combat Log: {playerName}"),
                Map.of("playerName", incident.getPlayerName())
            );
            
            // Look up Discord user from LinkingDatabase
            String discordId = linkingDatabase.getDiscordId(incident.getPlayerUuid()).orElse(null);
            
            User linkedUser = null;
            if (discordId != null) {
                try {
                    linkedUser = jda.retrieveUserById(discordId).complete();
                } catch (Exception e) {
                    logger.warn("Failed to retrieve Discord user {}: {}", discordId, e.getMessage());
                }
            }
            
            MessageEmbed embed = createIncidentEmbed(incident, linkedUser);
            
            var message = channel.sendMessageEmbeds(embed).complete();
            ThreadChannel thread = message.createThreadChannel(title).complete();
            
            // Make thread private if enabled
            if (config.features.privateThreads) {
                thread.getManager().setInvitable(false).queue();
            }
            
            // Add linked player to thread if available
            if (linkedUser != null) {
                thread.addThreadMember(linkedUser).queue();
                sendPlayerNotification(linkedUser, thread, incident);
                String reviewMessage = MessageFormatter.format(
                    config.message("ticket.player.reviewThread", "{userMention} - Please review this incident."),
                    Map.of("userMention", linkedUser.getAsMention())
                );
                thread.sendMessage(reviewMessage).queue();
            }
            
            thread.sendMessage(buildInstructionsMessage(linkedUser))
                .setComponents(createActionButtons(incident.getIncidentId()))
                .queue();
            
            return thread.getId();
        } catch (Exception e) {
            logger.error("Failed to create thread ticket: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create embed for incident
     */
    private MessageEmbed createIncidentEmbed(CombatLogIncident incident, User linkedUser) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(config.message("ticket.incident.title", "⚔️ Combat Log Report"));
        embed.setColor(Color.RED);
        embed.setDescription(config.message("ticket.incident.desc", "A player has disconnected during combat and needs to provide proof."));
        
        embed.addField(config.message("ticket.embed.field.player", "Player"), incident.getPlayerName(), true);
        embed.addField(config.message("ticket.embed.field.incidentId", "Incident ID"), 
            "`" + incident.getIncidentId().substring(0, 8) + "...`", true);
        embed.addField(config.message("ticket.embed.field.combatTimeRemaining", "Combat Time Remaining"), 
            String.format("%.1f seconds", incident.getCombatTimeRemaining()), true);
        
        // Show Discord link status
        if (linkedUser != null) {
            String linkedText = MessageFormatter.format(
                config.message("ticket.incident.discordLinkedYes", "✅ {userMention}"),
                Map.of("userMention", linkedUser.getAsMention())
            );
            embed.addField(config.message("ticket.embed.field.discordLinked", "Discord Linked"), linkedText, true);
        } else {
            embed.addField(config.message("ticket.embed.field.discordLinked", "Discord Linked"),
                config.message("ticket.incident.discordLinkedNo", "❌ Not linked"), true);
        }
        
        embed.addField(config.message("ticket.embed.field.status", "Status"),
            config.message("ticket.incident.statusPending", "⏳ Pending Proof"), true);
        embed.addField(config.message("ticket.embed.field.deadline", "Deadline"), 
            String.format("<t:%d:R>", Instant.now().plusSeconds(config.timeouts.ticketTimeoutMinutes * 60).getEpochSecond()),
            true);
        embed.addField(config.message("ticket.embed.field.consequence", "Consequence"),
            config.message("ticket.incident.consequence", "❌ Killed on next login if not resolved"), true);
        
        embed.setTimestamp(Instant.now());
        embed.setFooter(config.message("ticket.incident.footer", "Combat Log System"));
        
        return embed.build();
    }

    /**
     * Send DM notification to player
     */
    private void sendPlayerNotification(User user, ThreadChannel thread, CombatLogIncident incident) {
        try {
            if (thread == null) {
                logger.warn("Cannot notify player; thread is null for incident {}", incident.getIncidentId());
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(config.message("ticket.notification.title", "🚨 Combat Log Ticket Created"));
            embed.setColor(Color.ORANGE);
            embed.setDescription(config.message("ticket.notification.desc", "You have disconnected during combat and a ticket has been created."));

            embed.addField(config.message("ticket.notification.field.whatHappened", "What happened?"),
                config.message("ticket.notification.value.whatHappened", "You disconnected while in combat with another player."), false);
            embed.addField(config.message("ticket.notification.field.whatToDo", "What do I need to do?"),
                config.message("ticket.notification.value.whatToDo",
                    "Submit proof (clip/video) showing you disconnected unintentionally (crash, internet issue, etc.)"), false);
            embed.addField(config.message("ticket.notification.field.where", "Where?"),
                MessageFormatter.format(config.message("ticket.notification.value.where", "In the ticket: {threadMention}"),
                    Map.of("threadMention", thread.getAsMention())), false);
            embed.addField(config.message("ticket.notification.field.deadline", "Deadline"),
                MessageFormatter.format(config.message("ticket.notification.value.deadline",
                    "You have **{minutes} minutes** to submit proof"),
                    Map.of("minutes", String.valueOf(config.timeouts.ticketTimeoutMinutes))), false);
            embed.addField(config.message("ticket.notification.field.whatIf", "What if I don't?"),
                config.message("ticket.notification.value.whatIf", "You will be killed when you next log into the server."), false);

            embed.setFooter(config.message("ticket.notification.footer", "Use the thread link above to open your ticket"));
            embed.setTimestamp(Instant.now());

            thread.sendMessage(user.getAsMention())
                .setEmbeds(embed.build())
                .queue(
                    success -> logger.info("Posted ticket notification for {}", user.getName()),
                    error -> logger.warn("Failed to post ticket notification for {}: {}", user.getName(), error.getMessage())
                );
        } catch (Exception e) {
            logger.error("Failed to send player notification: {}", e.getMessage());
        }
    }

    /**
     * Build instructions message
     */
    private String buildInstructionsMessage(User linkedUser) {
        String approveLabel = resolveLabel(config.buttons.ticket.approve, "✅ Approve");
        String denyLabel = resolveLabel(config.buttons.ticket.deny, "❌ Deny");
        String extendLabel = resolveLabel(config.buttons.ticket.extend, "⏰ Extend");

        StringBuilder sb = new StringBuilder();
        String userMention = linkedUser != null ? linkedUser.getAsMention() : "the Player";
        return MessageFormatter.format(
            config.message("ticket.instructions",
                "**📋 Instructions:**\n\n**For {userMention}:**\n" +
                    "• Upload a clip/video showing you disconnected unintentionally (crash, internet issue, etc.)\n" +
                    "• Accepted platforms: YouTube, Twitch, Streamable, Medal.tv, or Discord upload\n" +
                    "• You have **{minutes} minutes** to submit proof\n" +
                    "• If no proof is submitted, you will be killed on your next login\n\n" +
                    "**For Staff:**\n" +
                    "• Click **{approveLabel}** to clear the punishment\n" +
                    "• Click **{denyLabel}** to confirm the punishment\n" +
                    "• Click **{extendLabel}** to give more time\n" +
                    "• Or use `/info <incident_id>` for details"
            ),
            Map.of(
                "userMention", userMention,
                "minutes", String.valueOf(config.timeouts.ticketTimeoutMinutes),
                "approveLabel", approveLabel,
                "denyLabel", denyLabel,
                "extendLabel", extendLabel
            )
        );
    }

    /**
     * Create action buttons for ticket
     */
    private ActionRow createActionButtons(String incidentId) {
        return ActionRow.of(
            Button.success("approve:" + incidentId, resolveLabel(config.buttons.ticket.approve, "✅ Approve")),
            Button.danger("deny:" + incidentId, resolveLabel(config.buttons.ticket.deny, "❌ Deny")),
            Button.primary("admit:" + incidentId, resolveLabel(config.buttons.ticket.admit, "🔴 I Admit Combat Log")),
            Button.secondary("extend:" + incidentId, resolveLabel(config.buttons.ticket.extend, "⏰ Extend"))
        );
    }

    private String resolveLabel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    /**
     * Approve a ticket
     */
    public boolean approveTicket(String incidentId, String adminName, String reason) {
        Ticket ticket = activeTickets.get(incidentId);
        if (ticket == null) {
            return false;
        }

        ticket.setStatus(Ticket.TicketStatus.APPROVED);
        
        // Send decision to Minecraft
        if (webSocketServer != null) {
            IncidentDecision decision = new IncidentDecision(
                incidentId, "APPROVED", adminName, 
                reason != null ? reason : "Appeal approved by staff"
            );
            webSocketServer.sendDecision(decision);
        }

        // Update Discord ticket
        String status = MessageFormatter.format(
            config.message("ticket.status.approved", "✅ Approved by {adminName}"),
            Map.of("adminName", adminName)
        );
        updateTicketMessage(ticket, status, Color.GREEN, reason);
        
        activeTickets.remove(incidentId);
        logger.info("Ticket {} approved by {}", incidentId, adminName);
        return true;
    }

    /**
     * Deny a ticket
     */
    public boolean denyTicket(String incidentId, String adminName, String reason) {
        Ticket ticket = activeTickets.get(incidentId);
        if (ticket == null) {
            return false;
        }

        ticket.setStatus(Ticket.TicketStatus.DENIED);
        
        // Send decision to Minecraft
        if (webSocketServer != null) {
            IncidentDecision decision = new IncidentDecision(
                incidentId, "DENIED", adminName,
                reason != null ? reason : "Appeal denied by staff"
            );
            webSocketServer.sendDecision(decision);
        }

        // Update Discord ticket
        String status = MessageFormatter.format(
            config.message("ticket.status.denied", "❌ Denied by {adminName}"),
            Map.of("adminName", adminName)
        );
        updateTicketMessage(ticket, status, Color.RED, reason);
        scheduleDeniedThreadDeletion(ticket, DENIED_THREAD_DELETE_DELAY_MINUTES);
        
        activeTickets.remove(incidentId);
        logger.info("Ticket {} denied by {}", incidentId, adminName);
        return true;
    }

    /**
     * Extend ticket deadline
     */
    public boolean extendTicket(String incidentId, long additionalMinutes) {
        Ticket ticket = activeTickets.get(incidentId);
        if (ticket == null) {
            return false;
        }

        Instant newExpiry = ticket.getExpiresAt().plusSeconds(additionalMinutes * 60);
        ticket.setExpiresAt(newExpiry);
        ticket.setStatus(Ticket.TicketStatus.EXTENDED);
        
        // Notify in Discord
        String status = MessageFormatter.format(
            config.message("ticket.status.extended", "⏰ Extended by {minutes} minutes"),
            Map.of("minutes", String.valueOf(additionalMinutes))
        );
        updateTicketMessage(ticket, status, Color.YELLOW, null);
        
        logger.info("Ticket {} extended by {} minutes", incidentId, additionalMinutes);
        return true;
    }

    /**
     * Record clip submission
     */
    public void recordClipSubmission(String incidentId, String clipUrl) {
        Ticket ticket = activeTickets.get(incidentId);
        if (ticket != null) {
            ticket.setClipUrl(clipUrl);
            updateTicketMessage(ticket,
                config.message("ticket.status.clipSubmitted", "📹 Clip submitted - Awaiting staff review"),
                Color.BLUE,
                clipUrl
            );
            logger.info("Clip submitted for ticket {}: {}", incidentId, clipUrl);
        }
    }

    /**
     * Check for expired tickets
     */
    private void checkTimeouts() {
        List<String> expiredIds = new ArrayList<>();
        
        for (Ticket ticket : activeTickets.values()) {
            if (ticket.isExpired() && ticket.getStatus() == Ticket.TicketStatus.PENDING) {
                expiredIds.add(ticket.getIncidentId());
            }
        }

        for (String incidentId : expiredIds) {
            autoDeny(incidentId);
        }
    }

    /**
     * Auto-deny expired ticket
     */
    private void autoDeny(String incidentId) {
        Ticket ticket = activeTickets.get(incidentId);
        if (ticket == null) {
            return;
        }

        ticket.setStatus(Ticket.TicketStatus.AUTO_DENIED);
        
        // Send decision to Minecraft
        if (webSocketServer != null) {
            IncidentDecision decision = new IncidentDecision(
                incidentId, "AUTO_DENIED", "System",
                "No proof submitted within " + config.timeouts.ticketTimeoutMinutes + " minutes"
            );
            webSocketServer.sendDecision(decision);
        }

        // Update Discord ticket
        updateTicketMessage(ticket,
            config.message("ticket.status.autoDenied", "⏱️ Auto-Denied (Timeout)"),
            Color.DARK_GRAY,
            config.message("ticket.status.autoDeniedDetails", "No proof was submitted within the deadline")
        );
        scheduleDeniedThreadDeletion(ticket, DENIED_THREAD_DELETE_DELAY_MINUTES);
        
        activeTickets.remove(incidentId);
        logger.info("Ticket {} auto-denied due to timeout", incidentId);
    }

    private void scheduleDeniedThreadDeletion(Ticket ticket, long delayMinutes) {
        if (delayMinutes <= 0) {
            return;
        }

        scheduler.schedule(() -> {
            try {
                ThreadChannel thread = jda.getThreadChannelById(ticket.getChannelId());
                if (thread == null) {
                    logger.debug("Thread {} already deleted or unavailable", ticket.getChannelId());
                    return;
                }

                thread.delete().queue(
                    success -> logger.info("Deleted denied ticket thread {}", thread.getId()),
                    error -> logger.warn("Failed to delete denied ticket thread {}: {}", thread.getId(), error.getMessage())
                );
            } catch (Exception e) {
                logger.warn("Failed to schedule deletion for thread {}: {}", ticket.getChannelId(), e.getMessage());
            }
        }, delayMinutes, TimeUnit.MINUTES);
    }

    /**
     * Update ticket message with status
     */
    private void updateTicketMessage(Ticket ticket, String status, Color color, String additionalInfo) {
        try {
            ThreadChannel thread = jda.getThreadChannelById(ticket.getChannelId());
            if (thread != null) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(config.message("ticket.statusUpdate.title", "📊 Ticket Status Update"));
                embed.setColor(color);
                embed.addField(config.message("ticket.statusUpdate.field.status", "Status"), status, false);
                
                if (additionalInfo != null) {
                    embed.addField(config.message("ticket.statusUpdate.field.details", "Details"), additionalInfo, false);
                }
                
                embed.setTimestamp(Instant.now());
                
                // Close thread if resolved
                boolean shouldArchive = (color == Color.GREEN || color == Color.RED || color == Color.DARK_GRAY);
                
                if (shouldArchive) {
                    // Send message FIRST, then archive after message is sent successfully
                    thread.sendMessageEmbeds(embed.build()).queue(
                        success -> {
                            // Archive thread after message is sent
                            thread.getManager().setArchived(true).queue(
                                archiveSuccess -> logger.debug("Thread {} archived successfully", thread.getId()),
                                archiveError -> logger.error("Failed to archive thread {}: {}", thread.getId(), archiveError.getMessage())
                            );
                        },
                        error -> logger.error("Failed to send status message to thread {}: {}", thread.getId(), error.getMessage())
                    );
                } else {
                    // Just send the message without archiving
                    thread.sendMessageEmbeds(embed.build()).queue();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to update ticket message: {}", e.getMessage(), e);
        }
    }

    public Ticket getTicket(String incidentId) {
        return activeTickets.get(incidentId);
    }

    public Collection<Ticket> getActiveTickets() {
        return activeTickets.values();
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
