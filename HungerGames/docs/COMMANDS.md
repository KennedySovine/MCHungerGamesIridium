HungerGames — Command Reference

This document is the complete reference for the `/hg` command dispatcher and all implemented subcommands.

Usage: /hg <subcommand> [args]

Working-arena workflow (important)
- Many admin commands now operate on an in-memory "working" arena. Use `/hg arena load <arenaName>` to copy a persisted arena into the working placeholder, then run edit commands without specifying the arena name.
- Changes made to the working arena are kept in memory. Run `/hg arena save` to persist the working arena back into the master arenas.yml data.
- `/hg arena create <arenaName> [displayName]` creates a new master arena entry and automatically loads it into the working placeholder so you can immediately edit it.

Admin Commands (permission: hungergames.admin)

**Arena Management Commands**

- `/hg arena create <arenaName> [displayName]` ✅ IMPLEMENTED
  Description: Create a new arena master entry and load it into the working arena for in-memory edits. Creates the master entry in arenas.yml so the new arena id exists and can be saved later.

- `/hg arena load <arenaName>` ✅ IMPLEMENTED
  Description: Copy the master arena (from arenas.yml) into the in-memory working arena. Subsequent edit commands will use the working arena by default and not persist until `/hg arena save` is called.

- `/hg arena save` ✅ IMPLEMENTED
  Description: Persist the currently loaded working arena back into the master arenas list and write to arenas.yml. Fails if there is no working arena loaded or if the working arena id is not present in master.

- `/hg arena list` ✅ IMPLEMENTED
  Description: List all available arenas with their display names and player limits.

- `/hg arena addspawn [arenaName]` ✅ IMPLEMENTED
  Description: Add a spawn point at your current location to the working arena (in-memory). Spawn location is stored as a relative offset from the arena center. If an arenaName is provided it must match the currently loaded working arena id. Changes are in-memory; use `/hg arena save` to persist.

- `/hg arena removespawn [arenaName] <spawnIndex>` ✅ IMPLEMENTED
  Description: Remove a spawn from the working arena by index. Prefers working arena edits; provides guidance to run `/hg arena save` afterwards.

- `/hg setcenter` ✅ IMPLEMENTED
  Description: Set the center point for the working arena at your current location. The center is used as the reference point for all relative spawn positions.

**Arena Configuration Commands** (all modify working arena in-memory)

- `/hg maxplayers <number>` ✅ IMPLEMENTED
  Description: Set the working arena's maxPlayers (in-memory). Use `/hg arena save` to persist.

- `/hg minplayers <number>` ✅ IMPLEMENTED
  Description: Set the working arena's minPlayers (in-memory). Use `/hg arena save` to persist.

- `/hg time <timeInSeconds>` ✅ IMPLEMENTED
  Description: Set the working arena's time-to-shrink before deathmatch (in-memory). Use `/hg arena save` to persist.

- `/hg centersize <size>` ✅ IMPLEMENTED
  Description: Set the working arena's deathmatch center size in blocks (in-memory). Use `/hg arena save` to persist.

- `/hg graceperiod <timeInSeconds>` ✅ IMPLEMENTED
  Description: Set the working arena's grace period for disconnects (in-memory). Default is 120 seconds. Use `/hg arena save` to persist.

- `/hg chestrefill <timeInSeconds>` ✅ IMPLEMENTED
  Description: Set the working arena's chest-refill interval (in-memory). `300` seconds (5 minutes) is the standard default pacing. Set to `0` to disable automatic refill scheduling. Use `/hg arena save` to persist.

**Game Control Commands**

- `/hg start [arenaName]` ✅ IMPLEMENTED
  Description: Start a match in the specified arena. If no arenaName is provided, the currently loaded working arena id will be used (if any). This command affects runtime game state and does not modify arena persistence.

- `/hg stop [arenaName]` ✅ IMPLEMENTED
  Description: Stop the running match in the specified arena. If no arenaName is provided, the currently loaded working arena id will be used (if any).

**Additional Admin Commands**

- `/hg beacons` ✅ IMPLEMENTED
  Description: Toggle visibility of spawn point beacons (particle effects) for the working arena. Useful for visualizing spawn locations while editing.

- `/hg gui` ✅ IMPLEMENTED
  Description: Open the arena editor GUI (inventory-based interface). Framework implemented; full interactive behavior deferred.

Player Commands (no permission required)

- `/hg join <arenaName>` ✅ IMPLEMENTED
  Description: Join the specified arena lobby. Arena must exist and have a running or lobby-phase match.

- `/hg leave` ✅ IMPLEMENTED
  Description: Leave your current arena and return to the lobby location.

- `/hg stats [player]` ✅ IMPLEMENTED
  Description: Display kills/deaths/wins/losses for yourself or the specified player. Stats are match-scoped and not persisted across server restarts.

Implementation Status

✅ **All Core Commands Implemented** (17 commands total)
- 13 admin commands for arena management and game control
- 3 player commands for match participation
- 1 additional GUI command (framework)

Tab Completion
- Basic tab completion is implemented for arena names and common parameters
- Completion suggestions adapt based on context (e.g., only show loaded arena when save is available)

Error Handling
- All commands provide clear error messages when arguments are missing or invalid
- Commands instruct admins to load/save working arenas as necessary
- Permission checks enforce admin-only access to configuration commands

Next Steps
- Complete interactive GUI behavior for `/hg gui` command
- Add more sophisticated tab completion for numeric parameters
- Consider adding `/hg arena delete <arenaName>` command for arena removal
