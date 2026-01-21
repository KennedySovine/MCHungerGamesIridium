HungerGames — Command Reference

This document is generated as a template for the `/hg` command dispatcher. Keep this file in sync with the Javadocs for each SubCommand implementation.

Usage: /hg <subcommand> [args]

Admin Commands (permission: HungerGames.admin)
- /hg arena create <arenaName>
  Description: Create a new arena with id <arenaName>. Use `/hg arena addspawn <arenaName>` or the GUI later to add spawn points.

- /hg arena addspawn <arenaName>
  Description: Add a spawn point at your current location for the given arena (player-only).

- /hg arena removespawn <arenaName> <spawnIndex>
  Description: Remove the spawn point by index (0-based); check `/hg arena listspawns <arenaName>` for indices.

- /hg maxplayers <arenaName> <number>
  Description: Set maximum players allowed in an arena.

- /hg minplayers <arenaName> <number>
  Description: Set minimum players required to start a match.

- /hg time <arenaName> <timeInSeconds>
  Description: Time before the border starts shrinking / deathmatch triggers.

- /hg centersize <arenaName> <size>
  Description: Final center size (radius or diameter depending on implementation) for deathmatch.

- /hg graceperiod <arenaName> <timeInSeconds>
  Description: Set arena-specific grace period for out-of-combat disconnects.

- /hg chestrefill <arenaName> <timeInSeconds>
  Description: Configure chest refill interval.

- /hg start <arenaName>
  Description: Start a match in the specified arena.

- /hg stop <arenaName>
  Description: Stop the running match in the arena and reset players.

Player Commands (permission: HungerGames.player)
- /hg join <arenaName>
  Description: Join the specified arena lobby.

- /hg leave
  Description: Leave your current arena and return to the lobby.

- /hg stats [player]
  Description: Display kills/deaths/wins/losses for a player or yourself.

Notes
- Tab completion should suggest arena names for commands that take <arenaName>.
- Admin commands should provide clear error messages when arguments are missing or invalid.
- All commands should be thoroughly documented in Javadocs in the source files.

Next steps
- Implement manager hooks (ArenaManager, GameManager, StatsManager) and wire commands to them.
- Add unit tests for parsing utilities and tab-completion helpers.

