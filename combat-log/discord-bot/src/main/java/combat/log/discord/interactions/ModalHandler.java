package combat.log.discord.interactions;

import combat.log.discord.discord.TicketManager;
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

    public ModalHandler(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        
        // Modal IDs format: "action:incidentId"
        String[] parts = modalId.split(":", 2);
        if (parts.length != 2) {
            event.reply("❌ Invalid modal").setEphemeral(true).queue();
            return;
        }
        
        String action = parts[0];
        String incidentId = parts[1];
        
        switch (action) {
            case "approve" -> handleApproveSubmission(event, incidentId);
            case "deny" -> handleDenySubmission(event, incidentId);
            case "extend" -> handleExtendSubmission(event, incidentId);
            default -> event.reply("❌ Unknown action").setEphemeral(true).queue();
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
            embed.setTitle("✅ Ticket Approved");
            embed.setColor(Color.GREEN);
            embed.addField("Incident ID", incidentId, false);
            embed.addField("Approved By", adminName, true);
            if (!reason.isEmpty()) {
                embed.addField("Reason", reason, false);
            }
            embed.setDescription("Player will NOT be punished on next login.");
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            
            // Disable buttons on the original message
            disableButtons(event);
            
            logger.info("Ticket {} approved by {} via button", incidentId, adminName);
        } else {
            event.reply("❌ Ticket not found: " + incidentId).setEphemeral(true).queue();
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
            embed.setTitle("❌ Ticket Denied");
            embed.setColor(Color.RED);
            embed.addField("Incident ID", incidentId, false);
            embed.addField("Denied By", adminName, true);
            if (!reason.isEmpty()) {
                embed.addField("Reason", reason, false);
            }
            embed.setDescription("Player WILL be killed on next login.");
            embed.setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            
            // Disable buttons on the original message
            disableButtons(event);
            
            logger.info("Ticket {} denied by {} via button", incidentId, adminName);
        } else {
            event.reply("❌ Ticket not found: " + incidentId).setEphemeral(true).queue();
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
                event.reply("❌ Please enter a valid number between 1 and 1440 minutes").setEphemeral(true).queue();
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

                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                logger.info("Ticket {} extended by {} minutes via button", incidentId, minutes);
            } else {
                event.reply("❌ Ticket not found: " + incidentId).setEphemeral(true).queue();
            }
        } catch (NumberFormatException e) {
            event.reply("❌ Invalid number format. Please enter a valid number.").setEphemeral(true).queue();
        }
    }

    /**
     * Disable buttons on the message
     */
    private void disableButtons(ModalInteractionEvent event) {
        try {
            Message message = event.getMessage();
            if (message != null && !message.getActionRows().isEmpty()) {
                // Get all buttons and disable them
                message.editMessageComponents(
                    ActionRow.of(
                        Button.success("disabled", "✅ Approved").asDisabled(),
                        Button.danger("disabled", "❌ Denied").asDisabled(),
                        Button.secondary("disabled", "⏰ Extend").asDisabled()
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
}
