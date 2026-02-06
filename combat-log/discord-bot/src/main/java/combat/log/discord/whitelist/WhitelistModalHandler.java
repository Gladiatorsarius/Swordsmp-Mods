package combat.log.discord.whitelist;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles modal submissions for whitelist requests
 */
public class WhitelistModalHandler extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WhitelistModalHandler.class);
    private final WhitelistManager whitelistManager;

    public WhitelistModalHandler(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();

        // Handle whitelist request modal
        if ("whitelist_modal".equals(modalId)) {
            handleWhitelistRequest(event);
            return;
        }

        // Handle deny modal
        if (modalId.startsWith("whitelist_deny_modal:")) {
            String requestId = modalId.substring("whitelist_deny_modal:".length());
            handleDeny(event, requestId);
            return;
        }
    }

    /**
     * Handle whitelist request modal submission
     */
    private void handleWhitelistRequest(ModalInteractionEvent event) {
        ModalMapping usernameMapping = event.getValue("minecraft_username");
        if (usernameMapping == null) {
            event.reply("❌ Error: Username not provided").setEphemeral(true).queue();
            return;
        }

        String minecraftName = usernameMapping.getAsString().trim();
        logger.info("Whitelist request submitted by {} for Minecraft name: {}", 
            event.getUser().getAsTag(), minecraftName);

        // Acknowledge immediately
        event.reply("⏳ Processing your whitelist request...").setEphemeral(true).queue();

        // Process request asynchronously
        whitelistManager.processRequest(event.getUser(), minecraftName);
    }

    /**
     * Handle deny modal submission
     */
    private void handleDeny(ModalInteractionEvent event, String requestId) {
        ModalMapping reasonMapping = event.getValue("deny_reason");
        String reason = reasonMapping != null ? reasonMapping.getAsString().trim() : "No reason provided";

        logger.info("Deny modal submitted by {} for request {} - Reason: {}", 
            event.getUser().getAsTag(), requestId, reason);

        // Disable buttons on the original message
        if (event.getMessage() != null) {
            event.getMessage().editMessageComponents().queue();
        }

        // Deny the request
        whitelistManager.denyRequest(
            requestId,
            event.getUser().getId(),
            event.getUser().getAsTag(),
            reason
        );

        event.reply("❌ Request denied.").setEphemeral(true).queue();
    }
}
