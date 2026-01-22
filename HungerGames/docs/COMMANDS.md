HungerGames — Command Reference

This document is the authoritative reference for the `/hg` command dispatcher used by the plugin skeleton. Keep this file in sync with the Javadocs for each SubCommand implementation.

Usage: /hg <subcommand> [args]

Admin Commands (permission: HungerGames.admin)
- /hg arena create <arenaName>
  Description: Create a new arena with id <arenaName>. Stores minimal defaults in arenas.yml. Use `/hg arena addspawn <arenaName>` to add spawn points.

- /hg arena addspawn <arenaName>
  Description: Add a spawn point at your current location for the given arena (player-only; admin permission required).

- /hg arena removespawn <arenaName> <spawnIndex>
  Description: Remove the spawn point by index (0-based).

- /hg arena delete <arenaName>
  Description: Delete the arena configuration from storage.

- /hg maxplayers <arenaName> <number>
  Description: Set maximum players allowed in an arena.

- /hg minplayers <arenaName> <number>
  Description: Set minimum players required to start a match.

- /hg time <arenaName> <timeInSeconds>
  Description: Set time (seconds) before the border begins shrinking (deathmatch timer).

- /hg centresize <arenaName> <size>
  Description: Set the final center size for the deathmatch border.

- /hg graceperiod <arenaName> <timeInSeconds>
  Description: Set arena-specific grace period (seconds) for out-of-combat disconnects.

- /hg chestrefill <arenaName> <timeInSeconds>
  Description: Configure chest refill interval (seconds) for the arena.

- /hg start <arenaName>
  Description: Start a match immediately in the specified arena.

- /hg stop <arenaName>
  Description: Stop the running match in the arena and reset arena state.

Player Commands (permission: HungerGames.player)
- /hg join <arenaName>
  Description: Join the specified arena lobby.

- /hg leave
  Description: Leave your current arena and return to the lobby.

- /hg stats [player]
  Description: Display kills/deaths/wins/losses for a player or yourself (stats are only available for the most recent completed match, non-persistent across server restarts).

Notes
- Tab completion has been intentionally disabled for now; it will be added later in a follow-up change.
- Admin commands must provide clear error messages when arguments are missing or invalid.
- Arena lobby locations will be set via the Arena Editor GUI in a later phase (command-based `setlobby` removed).
- The plugin persists arena definitions to arenas.yml in the plugin data folder.

Next steps
- Implement manager hooks (ArenaManager, GameManager, StatsManager) and wire commands to them.
- Add unit tests for parsing utilities and integration/manual tests on a dev server.
