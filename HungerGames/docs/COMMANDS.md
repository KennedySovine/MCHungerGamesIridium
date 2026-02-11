HungerGames — Command Reference

This document is generated as a template for the `/hg` command dispatcher. Keep this file in sync with the Javadocs for each SubCommand implementation.

Usage: /hg <subcommand> [args]

Working-arena workflow (important)
- Many admin commands now operate on an in-memory "working" arena. Use `/hg arena load <arenaName>` to copy a persisted arena into the working placeholder, then run edit commands without specifying the arena name.
- Changes made to the working arena are kept in memory. Run `/hg arena save` to persist the working arena back into the master arenas.yml data.
- `/hg arena create <arenaName> [displayName]` creates a new master arena entry and automatically loads it into the working placeholder so you can immediately edit it.

Admin Commands (permission: HungerGames.admin)
- /hg arena create <arenaName> [displayName]
  Description: Create a new arena master entry and load it into the working arena for in-memory edits. Does write to disk to create the master entry so the new arena id exists and can be saved later.

- /hg arena load <arenaName>
  Description: Copy the master arena (from arenas.yml) into the in-memory working arena. Subsequent edit commands will use the working arena by default and not persist until `/hg arena save` is called.

- /hg arena save
  Description: Persist the currently loaded working arena back into the master arenas list and write to arenas.yml. Fails if there is no working arena loaded or if the working arena id is not present in master (created but not saved).

- /hg arena addspawn [arenaName]
  Description: Add a spawn point at your current location to the working arena (in-memory). If an arenaName is provided it must match the currently loaded working arena id. Changes are in-memory; use `/hg arena save` to persist.

- /hg arena removespawn [arenaName] <spawnIndex>
  Description: Remove a spawn from the working arena (by index) or, if invoked as a player with only an arenaName, remove a spawn at the player's current location (within a small radius). Prefers working arena edits; provides guidance to run `/hg arena save` afterwards.

- /hg maxplayers <number>
  Description: Set the working arena's maxPlayers (in-memory). Use `/hg arena save` to persist.

- /hg minplayers <number>
  Description: Set the working arena's minPlayers (in-memory). Use `/hg arena save` to persist.

- /hg time <timeInSeconds>
  Description: Set the working arena's time-to-shrink (in-memory). Use `/hg arena save` to persist.

- /hg centersize <size>
  Description: Set the working arena's deathmatch center size (in-memory). Use `/hg arena save` to persist.

- /hg graceperiod <timeInSeconds>
  Description: Set the working arena's grace period (in-memory). Use `/hg arena save` to persist.

- /hg chestrefill <timeInSeconds>
  Description: Set the working arena's chest-refill interval (in-memory). Use `/hg arena save` to persist.

- /hg start [arenaName]
  Description: Opens the game for players to join (sets state to COUNTDOWN). Players can join via `/hg join` but cannot move until the game begins with `/hg begin`. If no arenaName is provided the currently loaded working arena id will be used (if any). This command affects runtime game state and does not modify arena persistence.

- /hg begin
  Description: Begins the game for the currently loaded working arena (transitions from COUNTDOWN to RUNNING). Players can now move and play. The game will also auto-begin when max players is reached.

- /hg stop [arenaName]
  Description: Stop the running match. If no arenaName is provided the currently loaded working arena id will be used (if any).

Player Commands (permission: HungerGames.player)
- /hg join
  Description: Join the currently loaded arena. The game must be in COUNTDOWN state (started but not begun). Players are removed from spectator mode, placed at a spawn point, and cannot move until the game begins.

- /hg leave
  Description: Leave your current arena and return to spectator mode (for non-admin players). Your inventory will be restored.

- /hg stats [player]
  Description: Display kills/deaths/wins/losses for a player or yourself (match-scoped; not persisted across restarts).

Notes
- Tab completion is disabled for now. All subcommands will be listed by `/hg`.
- Admin commands provide clear error messages when arguments are missing or invalid and instruct admins to load/save working arenas as necessary.
- Non-admin/non-op players are automatically placed in spectator mode when joining the server.
- Players cannot break blocks unless they have admin permission or are op.
- Players cannot move during the COUNTDOWN state (after /hg start but before /hg begin).
- The game automatically begins when max players is reached.

Next steps
- Update docs/ARCHITECTURE.md and docs/TODO.md to reflect the working-arena semantics and the need for admins to run `/hg arena save` to persist changes.
- Add example sequences in DEVELOPMENT_GUIDE.md to show a typical edit workflow (create -> addspawn -> set values -> save).
