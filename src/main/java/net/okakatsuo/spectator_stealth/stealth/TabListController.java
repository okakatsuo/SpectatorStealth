package net.okakatsuo.spectator_stealth.stealth;

import net.okakatsuo.spectator_stealth.mixin.ClientboundPlayerInfoUpdatePacketAccessor;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class TabListController {

    private TabListController() {
    }

    public static void hide(ServerPlayer target) {
        setListed(target, false);
    }

    public static void show(ServerPlayer target) {
        setListed(target, true);
    }

    public static void setListed(
            ServerPlayer target,
            boolean listed
    ) {
        MinecraftServer server = target.getServer();

        if (server == null) {
            return;
        }

        ClientboundPlayerInfoUpdatePacket packet =
                createListedPacket(target, listed);

        for (ServerPlayer viewer :
                server.getPlayerList().getPlayers()) {

            viewer.connection.send(packet);
        }
    }

    public static void setListedForViewer(
            ServerPlayer viewer,
            ServerPlayer target,
            boolean listed
    ) {
        viewer.connection.send(
                createListedPacket(target, listed)
        );
    }

    /*
     * 新しくログインしたプレイヤーには、
     * 既にStealth中のプレイヤー情報がVanillaによって
     * 送信されている可能性がある。
     *
     * そのためログイン完了時に改めてlisted=falseを送る。
     */
    public static void syncStealthPlayersForViewer(
            ServerPlayer viewer
    ) {
        MinecraftServer server = viewer.getServer();

        if (server == null) {
            return;
        }

        for (ServerPlayer target :
                server.getPlayerList().getPlayers()) {

            if (target == viewer) {
                continue;
            }

            if (StealthManager.isStealth(target)) {
                setListedForViewer(
                        viewer,
                        target,
                        false
                );
            }
        }
    }

    private static ClientboundPlayerInfoUpdatePacket
    createListedPacket(
            ServerPlayer target,
            boolean listed
    ) {
        /*
         * Vanillaに正常なEntryを一旦作らせる。
         */
        ClientboundPlayerInfoUpdatePacket packet =
                new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        target
                );

        ClientboundPlayerInfoUpdatePacket.Entry original =
                packet.entries().get(0);

        /*
         * listedだけを書き換えたEntry。
         *
         * profile / latency / gamemodeなどは
         * Vanillaが生成した値をそのまま維持する。
         */
        ClientboundPlayerInfoUpdatePacket.Entry replacement =
                new ClientboundPlayerInfoUpdatePacket.Entry(
                        original.profileId(),
                        original.profile(),
                        listed,
                        original.latency(),
                        original.gameMode(),
                        original.displayName(),
                        original.chatSession()
                );

        ((ClientboundPlayerInfoUpdatePacketAccessor)
                (Object) packet)
                .spectatorStealth$setEntries(
                        List.of(replacement)
                );

        return packet;
    }
}