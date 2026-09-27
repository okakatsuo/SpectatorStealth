package net.okakatsuo.spectator_stealth.mixin;

import net.okakatsuo.spectator_stealth.data.StealthSavedData;
import net.okakatsuo.spectator_stealth.stealth.StealthManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/server/players/PlayerList;" +
                                    "broadcastSystemMessage" +
                                    "(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void spectatorStealth$suppressJoinMessage(
            PlayerList playerList,
            Component message,
            boolean overlay,
            Connection connection,
            ServerPlayer player,
            CommonListenerCookie cookie
    ) {
        boolean scheduled =
                StealthSavedData.get(
                        playerList.getServer()
                ).isScheduled(
                        player.getUUID()
                );

        if (scheduled || StealthManager.isStealth(player)) {
            return;
        }

        playerList.broadcastSystemMessage(
                message,
                overlay
        );
    }
}