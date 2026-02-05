package combat.log.discord.discord;

import combat.log.discord.config.BotConfig;
import combat.log.discord.models.CombatLogIncident;
import combat.log.discord.models.IncidentDecision;
import combat.log.discord.models.Ticket;
import combat.log.discord.websocket.CombatLogWebSocketServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
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
    
    private final JDA jda;
    private final BotConfig config;
    private CombatLogWebSocketServer webSocketServer;
    
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TicketManager(JDA jda, BotConfig config) {
        this.jda = jda;
        this.config = config;
        
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
            if (config.discord.useForumChannel) {
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
                    config.ticket.timeoutMinutes
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
            ForumChannel forum = guild.getForumChannelById(config.discord.ticketChannelId);
            if (forum == null) {
                logger.error("Forum channel not found: {}", config.discord.ticketChannelId);
                return null;
            }

            String title = String.format("🚨 Combat Log: %s", incident.getPlayerName());
            
            MessageEmbed embed = createIncidentEmbed(incident);
            
            MessageCreateBuilder builder = new MessageCreateBuilder();
            builder.setEmbeds(embed);
            
            var forumPost = forum.createForumPost(title, builder.build()).complete();
            ThreadChannel thread = forumPost.getThreadChannel();
            
            // Send instructions with action buttons
            thread.sendMessage(buildInstructionsMessage())
                .setComponents(createActionButtons(incident.getIncidentId()))
                .queue();
            
            // Tag staff if role exists
            if (config.discord.staffRoleId != null && !config.discord.staffRoleId.isEmpty()) {
                Role staffRole = guild.getRoleById(config.discord.staffRoleId);
                if (staffRole != null) {
                    thread.sendMessage(staffRole.getAsMention() + " - New combat log incident!").queue();
                }
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
            var channel = guild.getTextChannelById(config.discord.ticketChannelId);
            if (channel == null) {
                logger.error("Text channel not found: {}", config.discord.ticketChannelId);
                return null;
            }

            String title = String.format("Combat Log: %s", incident.getPlayerName());
            
            MessageEmbed embed = createIncidentEmbed(incident);
            
            var message = channel.sendMessageEmbeds(embed).complete();
            ThreadChannel thread = message.createThreadChannel(title).complete();
            
            thread.sendMessage(buildInstructionsMessage())
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
    private MessageEmbed createIncidentEmbed(CombatLogIncident incident) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("⚔️ Combat Log Report");
        embed.setColor(Color.RED);
        embed.setDescription("A player has disconnected during combat and needs to provide proof.");
        
        embed.addField("Player", incident.getPlayerName(), true);
        embed.addField("Incident ID", 
            "`" + incident.getIncidentId().substring(0, 8) + "...`", true);
        embed.addField("Combat Time Remaining", 
            String.format("%.1f seconds", incident.getCombatTimeRemaining()), true);
        
        embed.addField("Status", "⏳ Pending Proof", true);
        embed.addField("Deadline", 
            String.format("<t:%d:R>", Instant.now().plusSeconds(config.ticket.timeoutMinutes * 60).getEpochSecond()),
            true);
        embed.addField("Consequence", "❌ Killed on next login if not resolved", true);
        
        embed.setTimestamp(Instant.now());
        embed.setFooter("Combat Log System");
        
        return embed.build();
    }

    /**
     * Build instructions message
     */
    private String buildInstructionsMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("**📋 Instructions:**\n\n");
        sb.append("**For the Player:**\n");
        sb.append("• Upload a clip/video showing you disconnected unintentionally (crash, internet issue, etc.)\n");
        sb.append("• Accepted platforms: YouTube, Twitch, Streamable, Medal.tv, or Discord upload\n");
        sb.append(String.format("• You have **%d minutes** to submit proof\n", config.ticket.timeoutMinutes));
        sb.append("• If no proof is submitted, you will be killed on your next login\n\n");
        
        sb.append("**For Staff:**\n");
        sb.append("• Use `/approve <incident_id>` to approve the appeal (clears punishment)\n");
        sb.append("• Use `/deny <incident_id>` to deny the appeal (player gets killed on login)\n");
        sb.append("• Use `/extend <incident_id> <minutes>` to extend the deadline\n");
        sb.append("• Use `/info <incident_id>` to view ticket details\n");
        
        return sb.toString();
    }

    /**
     * Create action buttons for ticket
     */
    private ActionRow createActionButtons(String incidentId) {
        return ActionRow.of(
            Button.success("approve:" + incidentId, "✅ Approve"),
            Button.danger("deny:" + incidentId, "❌ Deny"),
            Button.secondary("extend:" + incidentId, "⏰ Extend")
        );
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
        updateTicketMessage(ticket, "✅ Approved by " + adminName, Color.GREEN, reason);
        
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
        updateTicketMessage(ticket, "❌ Denied by " + adminName, Color.RED, reason);
        
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
        updateTicketMessage(ticket, "⏰ Extended by " + additionalMinutes + " minutes", Color.YELLOW, null);
        
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
            updateTicketMessage(ticket, "📹 Clip submitted - Awaiting staff review", Color.BLUE, clipUrl);
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
                "No proof submitted within " + config.ticket.timeoutMinutes + " minutes"
            );
            webSocketServer.sendDecision(decision);
        }

        // Update Discord ticket
        updateTicketMessage(ticket, "⏱️ Auto-Denied (Timeout)", Color.DARK_GRAY, 
            "No proof was submitted within the deadline");
        
        activeTickets.remove(incidentId);
        logger.info("Ticket {} auto-denied due to timeout", incidentId);
    }

    /**
     * Update ticket message with status
     */
    private void updateTicketMessage(Ticket ticket, String status, Color color, String additionalInfo) {
        try {
            ThreadChannel thread = jda.getThreadChannelById(ticket.getChannelId());
            if (thread != null) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("📊 Ticket Status Update");
                embed.setColor(color);
                embed.addField("Status", status, false);
                
                if (additionalInfo != null) {
                    embed.addField("Details", additionalInfo, false);
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
