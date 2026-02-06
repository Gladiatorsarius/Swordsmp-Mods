package combat.log.discord.interactions;

import combat.log.discord.config.BotConfig;
import combat.log.discord.discord.TicketManager;
import combat.log.discord.util.MessageFormatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;

/**
 * Handles modal submissions for ticket decisions
 */
public class ModalHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ModalHandler.class);
    private final TicketManager ticketManager;
    private final BotConfig config;

    public ModalHandler(TicketManager ticketManager, BotConfig config) {
        this.ticketManager = ticketManager;
        this.config = config;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        
        // Modal IDs format: "action:incidentId"
        String[] parts = modalId.split(":", 2);
        if (parts.length != 2) {
            return;
        }
        
        String action = parts[0];
        String incidentId = parts[1];
        
        switch (action) {
            case "approve" -> handleApproveSubmission(event, incidentId);
            case "deny" -> handleDenySubmission(event, incidentId);
            case "admit" -> handleAdmitSubmission(event, incidentId);
            case "extend" -> handleExtendSubmission(event, incidentId);
            default -> {
                // Ignore modal IDs intended for other handlers
            }
        }
    }

    /**
     * Handle approval modal submission
     */
    private void handleApproveSubmission(ModalInteractionEvent event, String incidentId) {
        String reason = event.getValue("reason") != null ? 
                event.getValue("reason").getAsString() : "";
        
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

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            
            // Disable buttons on the original message
            disableButtons(event);
            
            logger.info("Ticket {} approved by {} via button", incidentId, adminName);
        } else {
            event.reply(MessageFormatter.format(
                config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                java.util.Map.of("incidentId", incidentId)
            )).setEphemeral(true).queue();
        }
    }

    /**
     * Handle denial modal submission
     */
    private void handleDenySubmission(ModalInteractionEvent event, String incidentId) {
        String reason = event.getValue("reason") != null ? 
                event.getValue("reason").getAsString() : "";
        
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

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            
            // Disable buttons on the original message
            disableButtons(event);
            
            logger.info("Ticket {} denied by {} via button", incidentId, adminName);
        } else {
            event.reply(MessageFormatter.format(
                config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                java.util.Map.of("incidentId", incidentId)
            )).setEphemeral(true).queue();
        }
    }

    /**
     * Handle self-admission modal submission
     */
    private void handleAdmitSubmission(ModalInteractionEvent event, String incidentId) {
        String confirm = event.getValue("confirm") != null ? 
                event.getValue("confirm").getAsString().trim().toLowerCase() : "";
        
        // Validate confirmation text
        if (!confirm.equals("i admit")) {
            event.reply(config.message("ticket.modal.admitConfirm", "❌ You must type 'I admit' to confirm")).setEphemeral(true).queue();
            return;
        }
        
        String playerName = event.getUser().getName();
        String reason = "Player self-admitted to combat logging";
        
        // Process as denial (same consequences)
        boolean success = ticketManager.denyTicket(incidentId, "SELF-ADMIT", reason);

        if (success) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(config.message("ticket.embed.admit.title", "🔴 Combat Log Admitted"));
            embed.setColor(Color.ORANGE);
            embed.addField(config.message("ticket.embed.field.incidentId", "Incident ID"), incidentId, false);
            embed.addField(config.message("ticket.embed.field.admittedBy", "Admitted By"), playerName, true);
            embed.addField(config.message("ticket.embed.field.status", "Status"),
                config.message("ticket.embed.admit.status", "DENIED (Self-Admitted)"), false);
            embed.setDescription(config.message("ticket.embed.admit.desc",
                "You admitted to combat logging. Same consequences as denial apply:\n" +
                    "• You WILL be killed on next login\n" +
                    "• Your items are in a player head at logout location\n" +
                    "• Opponents can access for 30 minutes\n" +
                    "• After 30 min, everyone can access\n\n" +
                    "Thank you for your honesty."));
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            
            // Disable buttons on the original message
            disableButtons(event);
            
            logger.info("Ticket {} self-admitted by {}", incidentId, playerName);
        } else {
            event.reply(MessageFormatter.format(
                config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                java.util.Map.of("incidentId", incidentId)
            )).setEphemeral(true).queue();
        }
    }

    /**
     * Handle extend modal submission
     */
    private void handleExtendSubmission(ModalInteractionEvent event, String incidentId) {
        String minutesStr = event.getValue("minutes") != null ? 
                event.getValue("minutes").getAsString() : "0";
        
        try {
            long minutes = Long.parseLong(minutesStr);
            
            if (minutes <= 0 || minutes > 1440) { // Max 24 hours
                event.reply(config.message("ticket.modal.extendRange", "❌ Please enter a valid number between 1 and 1440 minutes")).setEphemeral(true).queue();
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

                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                logger.info("Ticket {} extended by {} minutes via button", incidentId, minutes);
            } else {
                event.reply(MessageFormatter.format(
                    config.message("ticket.commands.ticketNotFound", "❌ Ticket not found: {incidentId}"),
                    java.util.Map.of("incidentId", incidentId)
                )).setEphemeral(true).queue();
            }
        } catch (NumberFormatException e) {
            event.reply(config.message("ticket.modal.invalidNumber", "❌ Invalid number format. Please enter a valid number.")).setEphemeral(true).queue();
        }
    }

    /**
     * Disable buttons on the message
     */
    private void disableButtons(ModalInteractionEvent event) {
        try {
            Message message = event.getMessage();
            if (message != null && !message.getActionRows().isEmpty()) {
                String approveLabel = resolveLabel(config.buttons.ticket.approve, "✅ Approve");
                String denyLabel = resolveLabel(config.buttons.ticket.deny, "❌ Deny");
                String extendLabel = resolveLabel(config.buttons.ticket.extend, "⏰ Extend");
                // Get all buttons and disable them
                message.editMessageComponents(
                    ActionRow.of(
                        Button.success("disabled", approveLabel).asDisabled(),
                        Button.danger("disabled", denyLabel).asDisabled(),
                        Button.secondary("disabled", extendLabel).asDisabled()
                    )
                ).queue(
                    success -> logger.debug("Disabled buttons on message"),
                    error -> logger.error("Failed to disable buttons: {}", error.getMessage())
                );
            }
        } catch (Exception e) {
            logger.error("Error disabling buttons: {}", e.getMessage());
        }
    }

    private String resolveLabel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
