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
            event.reply("❌ Channel ID is required").setEphemeral(true).queue();
            return;
        }

        String channelId = channelOption.getAsString();
        logger.info("Setting up whitelist channel: {} by {}", channelId, event.getUser().getAsTag());

        try {
            whitelistManager.setupWhitelistChannel(channelId);
            event.reply("✅ Whitelist channel setup complete!").setEphemeral(true).queue();
        } catch (Exception e) {
            logger.error("Failed to setup whitelist channel", e);
            event.reply("❌ Failed to setup whitelist channel: " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}
