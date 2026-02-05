package combat.log.discord.commands;

import combat.log.discord.discord.TicketManager;
import combat.log.discord.models.Ticket;
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

    public TicketCommands(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "approve" -> handleApprove(event);
            case "deny" -> handleDeny(event);
            case "extend" -> handleExtend(event);
            case "info" -> handleInfo(event);
            default -> event.reply("Unknown command").setEphemeral(true).queue();
        }
    }

    /**
     * Handle /approve command
     */
    private void handleApprove(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);
        String reason = event.getOption("reason", "", OptionMapping::getAsString);

        if (incidentId == null) {
            event.reply("❌ Please provide an incident ID").setEphemeral(true).queue();
            return;
        }

        String adminName = event.getUser().getName();
        boolean success = ticketManager.approveTicket(incidentId, adminName, reason);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("✅ Ticket Approved");
            embed.setColor(Color.GREEN);
            embed.addField("Incident ID", incidentId, false);
            embed.addField("Approved By", adminName, true);
            if (!reason.isEmpty()) {
                embed.addField("Reason", reason, false);
            }
            embed.setDescription("Player will NOT be punished on next login.");
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();
            logger.info("Ticket {} approved by {}", incidentId, adminName);
        } else {
            event.reply("❌ Ticket not found: " + incidentId).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /deny command
     */
    private void handleDeny(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);
        String reason = event.getOption("reason", "", OptionMapping::getAsString);

        if (incidentId == null) {
            event.reply("❌ Please provide an incident ID").setEphemeral(true).queue();
            return;
        }

        String adminName = event.getUser().getName();
        boolean success = ticketManager.denyTicket(incidentId, adminName, reason);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("❌ Ticket Denied");
            embed.setColor(Color.RED);
            embed.addField("Incident ID", incidentId, false);
            embed.addField("Denied By", adminName, true);
            if (!reason.isEmpty()) {
                embed.addField("Reason", reason, false);
            }
            embed.setDescription("Player WILL be killed on next login.");
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();
            logger.info("Ticket {} denied by {}", incidentId, adminName);
        } else {
            event.reply("❌ Ticket not found: " + incidentId).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /extend command
     */
    private void handleExtend(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);
        Long minutes = event.getOption("minutes", OptionMapping::getAsLong);

        if (incidentId == null || minutes == null) {
            event.reply("❌ Please provide incident ID and minutes").setEphemeral(true).queue();
            return;
        }

        boolean success = ticketManager.extendTicket(incidentId, minutes);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("⏰ Ticket Extended");
            embed.setColor(Color.YELLOW);
            embed.addField("Incident ID", incidentId, false);
            embed.addField("Extended By", minutes + " minutes", true);
            embed.addField("Extended By User", event.getUser().getName(), true);
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();
            logger.info("Ticket {} extended by {} minutes", incidentId, minutes);
        } else {
            event.reply("❌ Ticket not found: " + incidentId).setEphemeral(true).queue();
        }
    }

    /**
     * Handle /info command
     */
    private void handleInfo(SlashCommandInteractionEvent event) {
        String incidentId = event.getOption("incident_id", OptionMapping::getAsString);

        if (incidentId == null) {
            event.reply("❌ Please provide an incident ID").setEphemeral(true).queue();
            return;
        }

        Ticket ticket = ticketManager.getTicket(incidentId);

        if (ticket != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("📋 Ticket Information");
            embed.setColor(Color.BLUE);
            
            embed.addField("Incident ID", ticket.getIncidentId(), true);
            embed.addField("Player", ticket.getPlayerName(), true);
            embed.addField("Status", ticket.getStatus().toString(), true);
            
            embed.addField("Combat Time Remaining", 
                String.format("%.1f seconds", ticket.getCombatTimeRemaining()), true);
            embed.addField("Created", 
                String.format("<t:%d:R>", ticket.getCreatedAt().getEpochSecond()), true);
            embed.addField("Expires", 
                String.format("<t:%d:R>", ticket.getExpiresAt().getEpochSecond()), true);
            
            if (ticket.getClipUrl() != null) {
                embed.addField("Clip URL", ticket.getClipUrl(), false);
                embed.addField("Clip Submitted", 
                    String.format("<t:%d:R>", ticket.getClipSubmittedAt().getEpochSecond()), true);
            }
            
            embed.addField("Time Remaining", 
                String.format("%d seconds", ticket.getSecondsRemaining()), true);
            
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.reply("❌ Ticket not found: " + incidentId).setEphemeral(true).queue();
        }
    }
}
