package combat.log.report.swordssmp.mixin;

import combat.log.report.swordssmp.linking.PlayerLinkingManager;
import combat.log.report.swordssmp.socket.SocketClient;
import combat.log.report.swordssmp.socket.UnlinkMessage;
import combat.log.report.swordssmp.socket.VanillaWhitelistAddMessage;
import combat.log.report.swordssmp.socket.WhitelistRemoveConfirmationMessage;
import combat.log.report.swordssmp.whitelist.WhitelistCommandGuard;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.WhitelistCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(WhitelistCommand.class)
public class WhitelistCommandMixin {
    @Inject(method = "addPlayers", at = @At("TAIL"))
    private static void combatLogReport$onWhitelistAdd(CommandSourceStack source, Collection<NameAndId> entries,
                                                      CallbackInfoReturnable<Integer> cir) {
        if (WhitelistCommandGuard.isIgnoringAdd()) {
            return;
        }
        if (entries == null || entries.isEmpty()) {
            return;
        }

        SocketClient socketClient = SocketClient.getInstance();
        for (NameAndId entry : entries) {
            if (entry == null || entry.name() == null || entry.id() == null) {
                continue;
            }
            socketClient.sendMessage(new VanillaWhitelistAddMessage(entry.name(), entry.id().toString(), source.getTextName()));
        }
    }

    @Inject(method = "removePlayers", at = @At("TAIL"))
    private static void combatLogReport$onWhitelistRemove(CommandSourceStack source, Collection<NameAndId> entries,
                                                         CallbackInfoReturnable<Integer> cir) {
        if (WhitelistCommandGuard.isIgnoringRemove()) {
            return;
        }
        if (entries == null || entries.isEmpty()) {
            return;
        }

        SocketClient socketClient = SocketClient.getInstance();
        PlayerLinkingManager linkManager = PlayerLinkingManager.getInstance();

        for (NameAndId entry : entries) {
            if (entry == null || entry.name() == null || entry.id() == null) {
                continue;
            }

            String playerName = entry.name();
            String playerUuid = entry.id().toString();

            linkManager.removeLink(playerUuid);
            socketClient.sendMessage(new UnlinkMessage(playerUuid, playerName, "whitelist_remove"));
            socketClient.sendMessage(new WhitelistRemoveConfirmationMessage(true, playerName, null));

            ServerPlayer target = source.getServer().getPlayerList().getPlayer(entry.id());
            if (target != null) {
                target.connection.disconnect(Component.literal("Your whitelist entry was removed. Go to Discord to re-link."));
            }
        }
    }
}
