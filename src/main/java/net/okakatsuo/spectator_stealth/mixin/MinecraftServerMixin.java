package net.okakatsuo.spectator_stealth.mixin;

import com.mojang.authlib.GameProfile;
import net.okakatsuo.spectator_stealth.stealth.StealthManager;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(
            method = "buildPlayerStatus",
            at = @At("RETURN"),
            cancellable = true
    )
    private void spectatorStealth$hideStealthFromStatus(
            CallbackInfoReturnable<ServerStatus.Players> cir
    ) {
        ServerStatus.Players original =
                cir.getReturnValue();

        if (original == null) {
            return;
        }

        MinecraftServer server =
                (MinecraftServer) (Object) this;

        Set<UUID> hiddenPlayers =
                server.getPlayerList()
                        .getPlayers()
                        .stream()
                        .filter(StealthManager::isStealth)
                        .map(ServerPlayer::getUUID)
                        .collect(Collectors.toSet());

        if (hiddenPlayers.isEmpty()) {
            return;
        }

        int visibleOnline =
                Math.max(
                        0,
                        original.online()
                                - hiddenPlayers.size()
                );

        /*
         * サーバー一覧でマウスオーバーしたときに
         * 表示されるサンプルプレイヤーからも除外。
         */
        List<GameProfile> visibleSample =
                original.sample()
                        .stream()
                        .filter(profile ->
                                !hiddenPlayers.contains(
                                        profile.getId()
                                )
                        )
                        .toList();

        cir.setReturnValue(
                new ServerStatus.Players(
                        original.max(),
                        visibleOnline,
                        visibleSample
                )
        );
    }
}