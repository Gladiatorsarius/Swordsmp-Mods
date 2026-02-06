package combat.log.discord.interactions;

import combat.log.discord.config.BotConfig;
import combat.log.discord.discord.TicketManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles button interactions for ticket management
 */
public class ButtonHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ButtonHandler.class);
    private final TicketManager ticketManager;
    private final BotConfig config;

    public ButtonHandler(TicketManager ticketManager, BotConfig config) {
        this.ticketManager = ticketManager;
        this.config = config;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        
        // Button IDs format: "action:incidentId"
        String[] parts = buttonId.split(":", 2);
        if (parts.length != 2) {
            logger.debug("Ignoring non-ticket button: {}", buttonId);
            return;
        }
        
        String action = parts[0];
        String incidentId = parts[1];
        
        switch (action) {
            case "approve" -> showApproveModal(event, incidentId);
            case "deny" -> showDenyModal(event, incidentId);
            case "admit" -> showAdmitModal(event, incidentId);
            case "extend" -> showExtendModal(event, incidentId);
            default -> logger.debug("Ignoring non-ticket action: {}", action);
        }
    }

    /**
     * Show approval confirmation modal
     */
    private void showApproveModal(ButtonInteractionEvent event, String incidentId) {
        TextInput reasonInput = TextInput.create(
            "reason",
            config.message("ticket.modal.reason.label", "Reason (Optional)"),
            TextInputStyle.PARAGRAPH
            )
            .setPlaceholder(config.message("ticket.modal.reason.approvePlaceholder",
                "Enter reason for approval (e.g., 'Clear crash', 'Internet issue')"))
                .setRequired(false)
                .setMaxLength(500)
                .build();

        Modal modal = Modal.create("approve:" + incidentId,
            config.message("ticket.modal.approve.title", "✅ Approve Ticket"))
                .addActionRow(reasonInput)
                .build();

        event.replyModal(modal).queue();
        logger.debug("Showed approve modal for incident {}", incidentId);
    }

    /**
     * Show denial confirmation modal
     */
    private void showDenyModal(ButtonInteractionEvent event, String incidentId) {
        TextInput reasonInput = TextInput.create(
            "reason",
            config.message("ticket.modal.reason.label", "Reason (Optional)"),
            TextInputStyle.PARAGRAPH
            )
            .setPlaceholder(config.message("ticket.modal.reason.denyPlaceholder",
                "Enter reason for denial (e.g., 'Combat logging', 'No valid proof')"))
                .setRequired(false)
                .setMaxLength(500)
                .build();

        Modal modal = Modal.create("deny:" + incidentId,
            config.message("ticket.modal.deny.title", "❌ Deny Ticket"))
                .addActionRow(reasonInput)
                .build();

        event.replyModal(modal).queue();
        logger.debug("Showed deny modal for incident {}", incidentId);
    }

    /**
     * Show self-admission confirmation modal
     */
    private void showAdmitModal(ButtonInteractionEvent event, String incidentId) {
        TextInput confirmInput = TextInput.create(
            "confirm",
            config.message("ticket.modal.admit.label", "Type 'I admit' to confirm"),
            TextInputStyle.SHORT
            )
            .setPlaceholder(config.message("ticket.modal.admit.placeholder", "Type: I admit"))
                .setRequired(true)
                .setMinLength(7)
                .setMaxLength(10)
                .build();

        Modal modal = Modal.create("admit:" + incidentId,
            config.message("ticket.modal.admit.title", "🔴 Admit Combat Logging"))
                .addActionRow(confirmInput)
                .build();

        event.replyModal(modal).queue();
        logger.debug("Showed admit modal for incident {}", incidentId);
    }

    /**
     * Show extend deadline modal
     */
    private void showExtendModal(ButtonInteractionEvent event, String incidentId) {
        TextInput minutesInput = TextInput.create(
            "minutes",
            config.message("ticket.modal.extend.label", "Additional Minutes"),
            TextInputStyle.SHORT
            )
            .setPlaceholder(config.message("ticket.modal.extend.placeholder", "Enter number of minutes (e.g., 30)"))
                .setRequired(true)
                .setMaxLength(4)
                .build();

        Modal modal = Modal.create("extend:" + incidentId,
            config.message("ticket.modal.extend.title", "⏰ Extend Deadline"))
                .addActionRow(minutesInput)
                .build();

        event.replyModal(modal).queue();
        logger.debug("Showed extend modal for incident {}", incidentId);
    }
}
