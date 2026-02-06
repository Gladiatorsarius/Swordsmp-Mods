package combat.log.discord.whitelist;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles button clicks for whitelist requests
 */
public class WhitelistButtonHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WhitelistButtonHandler.class);
    private final WhitelistManager whitelistManager;

    public WhitelistButtonHandler(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        // Handle whitelist request button
        if ("whitelist_request".equals(componentId)) {
            showWhitelistModal(event);
            return;
        }

        // Handle approve button
        if (componentId.startsWith("whitelist_approve:")) {
            String requestId = componentId.substring("whitelist_approve:".length());
            handleApprove(event, requestId);
            return;
        }

        // Handle deny button
        if (componentId.startsWith("whitelist_deny:")) {
            String requestId = componentId.substring("whitelist_deny:".length());
            showDenyModal(event, requestId);
            return;
        }
    }

    /**
     * Show modal for requesting whitelist
     */
    private void showWhitelistModal(ButtonInteractionEvent event) {
        TextInput usernameInput = TextInput.create("minecraft_username", "Minecraft Username", TextInputStyle.SHORT)
            .setPlaceholder("Enter your Minecraft username")
            .setMinLength(3)
            .setMaxLength(16)
            .setRequired(true)
            .build();

        Modal modal = Modal.create("whitelist_modal", "Request Whitelist")
            .addActionRow(usernameInput)
            .build();

        event.replyModal(modal).queue();
    }

    /**
     * Handle approve button click
     */
    private void handleApprove(ButtonInteractionEvent event, String requestId) {
        logger.info("Approve button clicked by {} for request {}", event.getUser().getAsTag(), requestId);

        // Disable buttons
        event.editButton(event.getButton().asDisabled()).queue();

        // Approve the request
        whitelistManager.approveRequest(
            requestId,
            event.getUser().getId(),
            event.getUser().getAsTag()
        );

        event.reply("✅ Request approved! Whitelisting player...").setEphemeral(true).queue();
    }

    /**
     * Show modal for denying request
     */
    private void showDenyModal(ButtonInteractionEvent event, String requestId) {
        TextInput reasonInput = TextInput.create("deny_reason", "Reason for Denial", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Enter reason for denial (optional)")
            .setMinLength(0)
            .setMaxLength(500)
            .setRequired(false)
            .build();

        Modal modal = Modal.create("whitelist_deny_modal:" + requestId, "Deny Whitelist Request")
            .addActionRow(reasonInput)
            .build();

        event.replyModal(modal).queue();
    }
}
