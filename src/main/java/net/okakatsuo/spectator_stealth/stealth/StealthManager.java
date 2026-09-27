package net.okakatsuo.spectator_stealth.stealth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class StealthManager {

    private static final Set<UUID> LOGIN_TRANSITIONS =
            new HashSet<>();

    private StealthManager() {
    }

    public static boolean isStealth(ServerPlayer player) {
        return player.gameMode.getGameModeForPlayer()
                == GameType.SPECTATOR;
    }

    public static void beginLoginTransition(ServerPlayer player) {
        LOGIN_TRANSITIONS.add(player.getUUID());
    }

    public static void endLoginTransition(ServerPlayer player) {
        LOGIN_TRANSITIONS.remove(player.getUUID());
    }

    public static boolean isLoginTransition(ServerPlayer player) {
        return LOGIN_TRANSITIONS.contains(player.getUUID());
    }

    public static void enter(ServerPlayer player) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        TabListController.hide(player);

        /*
         * サーバー一覧の人数キャッシュを破棄。
         */
        server.invalidateStatus();

        announceLeave(player);
    }

    public static void exit(ServerPlayer player) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        TabListController.show(player);

        server.invalidateStatus();

        announceJoin(player);
    }

    public static void announceLeave(ServerPlayer player) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        Component message = Component.translatable(
                        "multiplayer.player.left",
                        player.getDisplayName()
                )
                .withStyle(ChatFormatting.YELLOW);

        server.getPlayerList()
                .broadcastSystemMessage(message, false);
    }

    public static void announceJoin(ServerPlayer player) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        Component message = Component.translatable(
                        "multiplayer.player.joined",
                        player.getDisplayName()
                )
                .withStyle(ChatFormatting.YELLOW);

        server.getPlayerList()
                .broadcastSystemMessage(message, false);
    }
}