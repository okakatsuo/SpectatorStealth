package net.okakatsuo.spectator_stealth.event;

import net.okakatsuo.spectator_stealth.SpectatorStealth;
import net.okakatsuo.spectator_stealth.data.StealthSavedData;
import net.okakatsuo.spectator_stealth.stealth.StealthManager;
import net.okakatsuo.spectator_stealth.stealth.TabListController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = SpectatorStealth.MOD_ID)
public final class StealthEvents {

    private static final Map<UUID, Boolean> PENDING_TRANSITIONS =
            new HashMap<>();

    private StealthEvents() {
    }

    @SubscribeEvent
    public static void onGameModeChange(
            PlayerEvent.PlayerChangeGameModeEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (StealthManager.isLoginTransition(player)) {
            return;
        }

        GameType current = event.getCurrentGameMode();
        GameType next = event.getNewGameMode();

        if (current != GameType.SPECTATOR
                && next == GameType.SPECTATOR) {

            PENDING_TRANSITIONS.put(
                    player.getUUID(),
                    true
            );

            return;
        }

        if (current == GameType.SPECTATOR
                && next != GameType.SPECTATOR) {

            PENDING_TRANSITIONS.put(
                    player.getUUID(),
                    false
            );
        }
    }

    @SubscribeEvent
    public static void onServerTick(
            ServerTickEvent.Post event
    ) {
        if (PENDING_TRANSITIONS.isEmpty()) {
            return;
        }

        Map<UUID, Boolean> transitions =
                new HashMap<>(PENDING_TRANSITIONS);

        PENDING_TRANSITIONS.clear();

        for (Map.Entry<UUID, Boolean> entry :
                transitions.entrySet()) {

            ServerPlayer player =
                    event.getServer()
                            .getPlayerList()
                            .getPlayer(entry.getKey());

            if (player == null) {
                continue;
            }

            boolean entering = entry.getValue();
            boolean actuallyStealth =
                    StealthManager.isStealth(player);

            if (entering && actuallyStealth) {
                StealthManager.enter(player);
            } else if (!entering && !actuallyStealth) {
                StealthManager.exit(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(
            PlayerEvent.PlayerLoggedInEvent event
    ) {
        if (!(event.getEntity()
                instanceof ServerPlayer player)) {
            return;
        }

        if (player.getServer() == null) {
            return;
        }

        StealthSavedData data =
                StealthSavedData.get(
                        player.getServer()
                );

        boolean scheduled =
                data.isScheduled(
                        player.getUUID()
                );

        /*
         * nextlogin指定があり、
         * まだSpectatorではない場合。
         */
        if (scheduled
                && !StealthManager.isStealth(player)) {

            StealthManager.beginLoginTransition(player);

            try {
                player.setGameMode(
                        GameType.SPECTATOR
                );
            } finally {
                StealthManager.endLoginTransition(player);
            }
        }

        /*
         * 元からSpectatorだった場合も含め、
         * ログイン完了時点でSpectatorならStealth。
         */
        if (StealthManager.isStealth(player)) {

            if (scheduled) {
                data.consume(
                        player.getUUID()
                );
            }

            TabListController.hide(player);

            player.getServer()
                    .invalidateStatus();

        } else if (scheduled) {

            /*
             * 他ModなどによってGameMode変更が
             * キャンセルされたケース。
             *
             * PlayerListMixinはjoinログを
             * suppressedしているので復元する。
             */
            TabListController.show(player);

            StealthManager.announceJoin(player);
        }

        /*
         * この新規ログインplayerに対して、
         * 既にStealth中の全playerをTAB非表示にする。
         */
        TabListController
                .syncStealthPlayersForViewer(player);
    }
}