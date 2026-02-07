package combat.log.report.swordssmp.linking;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command to show the linked Discord account for the current player.
 */
public class LinkInfoCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("linkinfo")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(LinkInfoCommand::executeLinkInfo)
        );
    }

    private static int executeLinkInfo(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        String playerUuid = player.getUUID().toString();
        PlayerLinkingManager linkManager = PlayerLinkingManager.getInstance();
        var linkOpt = linkManager.getLinkByUuid(playerUuid);
        if (linkOpt.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cYou do not have a linked Discord account."));
            return 0;
        }

        var link = linkOpt.get();
        player.sendSystemMessage(Component.literal("§aLinked Discord ID: " + link.getDiscordId()));
        return 1;
    }
}
