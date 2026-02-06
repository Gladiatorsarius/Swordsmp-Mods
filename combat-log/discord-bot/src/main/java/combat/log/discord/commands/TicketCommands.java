package combat.log.discord.commands;

import combat.log.discord.discord.TicketManager;
import combat.log.discord.models.Ticket;
import combat.log.discord.config.BotConfig;
import combat.log.discord.util.MessageFormatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;

/**
 * Handles slash commands for ticket management
 */
public class TicketCommands extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TicketCommands.class);
    private final TicketManager ticketManager;
    private final BotConfig config;

    public TicketCommands(TicketManager ticketManager, BotConfig config) {
        this.ticketManager = ticketManager;
        this.config = config;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "approve" -> handleApprove(event);
            case "deny" -> handleDeny(event);
            case "extend" -> handleExtend(event);
            case "info" -> handleInfo(event);
            default -> event.reply(config.message("ticket.commands.unknown", "Unknown command")).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /approve command
     */
    private void handleApprove(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);
        String reason = event.getOption("reason", "", OptionMapping::getAsString);

        if (incidentId == null) {
            event.reply(config.message("ticket.commands.missingIncidentId", "❌ Please provide an incident ID")).setEphemeral(true).queue();
            return;
        }

        String adminName = event.getUser().getName();
        boolean success = ticketManager.approveTicket(incidentId, adminName, reason);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(config.message("ticket.embed.approved.title", "✅ Ticket Approved"));
            embed.setColor(Color.GREEN);
            embed.addField(config.message("ticket.embed.field.incidentId", "Incident ID"), incidentId, false);
            embed.addField(config.message("ticket.embed.field.approvedBy", "Approved By"), adminName, true);
            if (!reason.isEmpty()) {
                embed.addField(config.message("ticket.embed.field.reason", "Reason"), reason, false);
            }
            embed.setDescription(config.message("ticket.embed.approved.desc", "Player will NOT be punished on next login."));
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();
            logger.info("Ticket {} approved by {}", incidentId, adminName);
        } else {
            event.reply(MessageFormatter.format(
                config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                java.util.Map.of("incidentId", incidentId)
            )).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /deny command
     */
    private void handleDeny(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);
        String reason = event.getOption("reason", "", OptionMapping::getAsString);

        if (incidentId == null) {
            event.reply(config.message("ticket.commands.missingIncidentId", "❌ Please provide an incident ID")).setEphemeral(true).queue();
            return;
        }

        String adminName = event.getUser().getName();
        boolean success = ticketManager.denyTicket(incidentId, adminName, reason);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(config.message("ticket.embed.denied.title", "❌ Ticket Denied"));
            embed.setColor(Color.RED);
            embed.addField(config.message("ticket.embed.field.incidentId", "Incident ID"), incidentId, false);
            embed.addField(config.message("ticket.embed.field.deniedBy", "Denied By"), adminName, true);
            if (!reason.isEmpty()) {
                embed.addField(config.message("ticket.embed.field.reason", "Reason"), reason, false);
            }
            embed.setDescription(config.message("ticket.embed.denied.desc", "Player WILL be killed on next login."));
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();
            logger.info("Ticket {} denied by {}", incidentId, adminName);
        } else {
            event.reply(MessageFormatter.format(
                config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                java.util.Map.of("incidentId", incidentId)
            )).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /extend command
     */
    private void handleExtend(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);
        Long minutes = event.getOption("minutes", OptionMapping::getAsLong);

        if (incidentId == null || minutes == null) {
            event.reply(config.message("ticket.commands.missingIncidentIdMinutes", "❌ Please provide incident ID and minutes")).setEphemeral(true).queue();
            return;
        }

        boolean success = ticketManager.extendTicket(incidentId, minutes);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(config.message("ticket.embed.extended.title", "⏰ Ticket Extended"));
            embed.setColor(Color.YELLOW);
            embed.addField(config.message("ticket.embed.field.incidentId", "Incident ID"), incidentId, false);
            embed.addField(config.message("ticket.embed.field.extendedBy", "Extended By"), minutes + " minutes", true);
            embed.addField(config.message("ticket.embed.field.extendedByUser", "Extended By User"), event.getUser().getName(), true);
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();
            logger.info("Ticket {} extended by {} minutes", incidentId, minutes);
        } else {
            event.reply(MessageFormatter.format(
                config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                java.util.Map.of("incidentId", incidentId)
            )).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /info command
     */
    private void handleInfo(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);

        if (incidentId == null) {
            event.reply(config.message("ticket.commands.missingIncidentId", "❌ Please provide an incident ID")).setEphemeral(true).queue();
            return;
        }

        Ticket ticket = ticketManager.getTicket(incidentId);

        if (ticket != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(config.message("ticket.embed.info.title", "📋 Ticket Information"));
            embed.setColor(Color.BLUE);
            
            embed.addField(config.message("ticket.embed.field.incidentId", "Incident ID"), ticket.getIncidentId(), true);
            embed.addField(config.message("ticket.embed.field.player", "Player"), ticket.getPlayerName(), true);
            embed.addField(config.message("ticket.embed.field.status", "Status"), ticket.getStatus().toString(), true);
            
            embed.addField(config.message("ticket.embed.field.combatTimeRemaining", "Combat Time Remaining"), 
                String.format("%.1f seconds", ticket.getCombatTimeRemaining()), true);
            embed.addField(config.message("ticket.embed.field.created", "Created"), 
                String.format("<t:%d:R>", ticket.getCreatedAt().getEpochSecond()), true);
            embed.addField(config.message("ticket.embed.field.expires", "Expires"), 
                String.format("<t:%d:R>", ticket.getExpiresAt().getEpochSecond()), true);
            
            if (ticket.getClipUrl() != null) {
                embed.addField(config.message("ticket.embed.field.clipUrl", "Clip URL"), ticket.getClipUrl(), false);
                embed.addField(config.message("ticket.embed.field.clipSubmitted", "Clip Submitted"), 
                    String.format("<t:%d:R>", ticket.getClipSubmittedAt().getEpochSecond()), true);
            }
            
            embed.addField(config.message("ticket.embed.field.timeRemaining", "Time Remaining"), 
                String.format("%d seconds", ticket.getSecondsRemaining()), true);
            
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply(MessageFormatter.format(
                config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                java.util.Map.of("incidentId", incidentId)
            )).setEphemeral(true).queue();
        }
    }
}
