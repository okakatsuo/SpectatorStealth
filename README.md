# SpectatorStealth

A lightweight server-side NeoForge mod that turns Spectator Mode into a simple stealth/vanish system.

Switching a player to Spectator Mode hides them from other players as if they had left the server. Switching them back to another game mode makes them appear again.

## Features

- Spectator Mode acts as stealth mode
- Hides spectators from the player list (TAB)
- Hides spectators from `/list`
- Hides spectators from the server list player count and sample
- Sends a fake leave message when entering Spectator Mode
- Sends a fake join message when leaving Spectator Mode
- Suppresses real join/leave messages while joining or leaving in stealth
- Supports scheduling a player to enter stealth on their next login
- Server-side only — players do not need to install the mod

## Usage

### Enter stealth

Use the normal Minecraft game mode command:

```mcfunction
/gamemode spectator <player>
```

The player enters Spectator Mode, disappears from the player list, and a normal-looking leave message is shown.

### Leave stealth

Change the player back to another game mode:

```mcfunction
/gamemode survival <player>
```

The player becomes visible again and a normal-looking join message is shown.

Creative and Adventure Mode also count as leaving stealth.

## Stealth Login

You can make a player join directly in stealth mode on their next login:

```mcfunction
/stealth nextlogin <player>
```

The player will join in Spectator Mode without appearing in the player list or producing a join message.

Cancel a scheduled stealth login:

```mcfunction
/stealth cancel <player>
```

List scheduled players:

```mcfunction
/stealth list
```

These commands require permission level 2.

## Compatibility

- Minecraft: 1.21.1
- Mod Loader: NeoForge
- Environment: Server-side

Clients can connect without installing SpectatorStealth.

## Notes

SpectatorStealth is designed as a lightweight stealth utility rather than a complete security or administrative invisibility system.

Stealthed players remain real server-side players and may still be detectable by other mods, plugins, commands, selectors, or administrative tools that directly inspect connected players.

## License

See [LICENSE](LICENSE).