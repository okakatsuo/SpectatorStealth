package net.okakatsuo.spectator_stealth.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.okakatsuo.spectator_stealth.SpectatorStealth;
import net.okakatsuo.spectator_stealth.data.StealthSavedData;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = SpectatorStealth.MOD_ID)
public final class StealthCommand {

    private StealthCommand() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("stealth")
                        .requires(source -> source.hasPermission(2))

                        .then(
                                Commands.literal("nextlogin")
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                StringArgumentType.word()
                                                        )
                                                        .executes(context ->
                                                                schedule(
                                                                        context.getSource().getServer(),
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "player"
                                                                        )
                                                                )
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("cancel")
                                        .then(
                                                Commands.argument(
                                                                "player",
                                                                StringArgumentType.word()
                                                        )
                                                        .executes(context ->
                                                                cancel(
                                                                        context.getSource().getServer(),
                                                                        context.getSource(),
                                                                        StringArgumentType.getString(
                                                                                context,
                                                                                "player"
                                                                        )
                                                                )
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("list")
                                        .executes(context ->
                                                list(
                                                        context.getSource().getServer(),
                                                        context.getSource()
                                                )
                                        )
                        )
        );
    }

    private static int schedule(
            MinecraftServer server,
            net.minecraft.commands.CommandSourceStack source,
            String playerName
    ) {
        Optional<GameProfile> profile =
                server.getProfileCache().get(playerName);

        if (profile.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "Unknown player: " + playerName
                    )
            );

            return 0;
        }

        GameProfile gameProfile = profile.get();

        UUID uuid = gameProfile.getId();
        String name = gameProfile.getName();

        StealthSavedData data =
                StealthSavedData.get(server);

        if (!data.schedule(uuid, name)) {
            source.sendFailure(
                    Component.literal(
                            name + " is already scheduled for stealth login."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        name + " will enter stealth on their next login."
                ),
                true
        );

        return 1;
    }

    private static int cancel(
            MinecraftServer server,
            net.minecraft.commands.CommandSourceStack source,
            String playerName
    ) {
        Optional<GameProfile> profile =
                server.getProfileCache().get(playerName);

        if (profile.isEmpty()) {
            source.sendFailure(
                    Component.literal(
                            "Unknown player: " + playerName
                    )
            );

            return 0;
        }

        GameProfile gameProfile = profile.get();

        StealthSavedData data =
                StealthSavedData.get(server);

        if (!data.cancel(gameProfile.getId())) {
            source.sendFailure(
                    Component.literal(
                            gameProfile.getName()
                                    + " is not scheduled for stealth login."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Cancelled stealth login for "
                                + gameProfile.getName()
                                + "."
                ),
                true
        );

        return 1;
    }

    private static int list(
            MinecraftServer server,
            net.minecraft.commands.CommandSourceStack source
    ) {
        Map<UUID, String> players =
                StealthSavedData.get(server)
                        .getScheduledPlayers();

        if (players.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal(
                            "No players are scheduled for stealth login."
                    ),
                    false
            );

            return 1;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Players scheduled for stealth login:"
                ),
                false
        );

        players.values()
                .stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(name ->
                        source.sendSuccess(
                                () -> Component.literal(
                                        "- " + name
                                ),
                                false
                        )
                );

        return players.size();
    }
}