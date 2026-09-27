package net.okakatsuo.spectator_stealth.mixin;

import net.okakatsuo.spectator_stealth.stealth.StealthManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Redirect(
            method = "removePlayerFromWorld",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/server/players/PlayerList;" +
                                    "broadcastSystemMessage" +
                                    "(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void spectatorStealth$suppressLeaveMessage(
            PlayerList playerList,
            Component message,
            boolean overlay
    ) {
        if (StealthManager.isStealth(player)) {
            return;
        }

        playerList.broadcastSystemMessage(
                message,
                overlay
        );
    }
}