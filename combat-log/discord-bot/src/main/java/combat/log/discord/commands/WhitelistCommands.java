package combat.log.discord.commands;

import combat.log.discord.whitelist.WhitelistManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles whitelist-related slash commands
 */
public class WhitelistCommands extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WhitelistCommands.class);
    private final WhitelistManager whitelistManager;

    public WhitelistCommands(WhitelistManager whitelistManager) {
        this.whitelistManager = whitelistManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("whitelist-setup".equals(event.getName())) {
            handleWhitelistSetup(event);
        }
    }

    private void handleWhitelistSetup(SlashCommandInteractionEvent event) {
        OptionMapping channelOption = event.getOption("channel_id");
        if (channelOption == null) {
            replyEphemeral(event, whitelistManager.message("whitelist.setup.channelRequired", "❌ Channel ID is required"));
            return;
        }

        String channelId = channelOption.getAsString();
        logger.info("Setting up whitelist channel: {} by {}", channelId, event.getUser().getAsTag());

        try {
            whitelistManager.setupWhitelistChannel(channelId);
            replyEphemeral(event, whitelistManager.message("whitelist.setup.success", "✅ Whitelist channel setup complete!"));
        } catch (Exception e) {
            logger.error("Failed to setup whitelist channel", e);
            String msg = whitelistManager.message("whitelist.setup.failure", "❌ Failed to setup whitelist channel: {error}")
                .replace("{error}", e.getMessage());
            replyEphemeral(event, msg);
        }
    }

    private void replyEphemeral(SlashCommandInteractionEvent event, String message) {
        if (event.isAcknowledged()) {
            event.getHook().sendMessage(message).setEphemeral(true).queue();
            return;
        }

        event.reply(message).setEphemeral(true).queue();
    }
}
