# TODO - Implementation Roadmap (detailed per-file tasks)

This file lists exactly what you (the developer) need to implement inside each skeleton file in the repository. For each file I list: responsibilities, concrete methods to fill in, edge-cases to handle, and how to test the result. This TODO is developer-facing: tick items in your head as you implement them.

General rules
- Add Javadoc to every public class and public method explaining inputs/outputs and side-effects.
- Inline-comment any non-trivial logic (parsing, thread expectations, permission checks).
- All Bukkit API calls must run on the server main thread. Use the scheduler for delayed tasks.
- Persist changes using `YamlStorage` (use `getStorage()` from the main plugin) unless noted otherwise.
- Tab completion has been disabled for now; do not implement tabComplete logic in the command dispatcher. The `SubCommand.tabComplete` methods may remain empty until later.

PHASE 1 — Commands & Managers (implement these first)

1) `src/main/java/io/github/KennedySovine/hungerGames/arena/Arena.java` (POJO)
- Responsibilities: represent arena settings and spawn locations.
- Implement:
  - Add serialize/deserialize helpers for locations using `LocationUtils` (to/from String).
  - (Optional) `validate()` to ensure minPlayers <= maxPlayers and non-negative numeric values.
- Tests: create an Arena in-memory and call serialize helpers.

2) `src/main/java/io/github/KennedySovine/hungerGames/arena/ArenaManager.java`
- Responsibilities: load/save `arenas.yml`, create/delete arenas, add/remove spawns, provide lookups.
- Implement:
  - `loadArenas()` — read `arenas.yml` via `plugin.getStorage().load("arenas.yml")`, parse each arena node, create `Arena` objects, parse spawn strings via `LocationUtils.deserialize`.
  - `saveArenas()` — convert each `Arena` into YAML structure and `plugin.getStorage().save("arenas.yml", cfg)`.
  - `createArena(String id)` — normalize id, ensure no collision, create `Arena` object and call `saveArenas()`.
  - `deleteArena(String id)` — ensure arena exists, remove from internal map and `saveArenas()`.
  - `addSpawn(String id, Location loc)` / `removeSpawn(String id, int index)` — update arena object and save.
  - `getArenaNames()` — return a list of arena ids for usage by commands.
- Edge-cases: missing world for spawn location -> skip with warning. Arena id collisions. Atomic save: save to tmp then rename (optional).
- Tests: unit test load/save round-trip using a temp folder and `YamlConfiguration`.

3) `src/main/java/io/github/KennedySovine/hungerGames/command/*` (subcommand classes)
- Files: `CreateArenaCommand`, `AddSpawnCommand`, `RemoveSpawnCommand`, `DeleteArenaCommand`, `MaxPlayersCommand`, `MinPlayersCommand`, `TimeCommand`, `CenterSizeCommand` (handled as `centresize` key), `GracePeriodCommand`, `ChestRefillCommand`, `StartCommand`, `StopCommand`, `JoinCommand`, `LeaveCommand`, `StatsCommand`.
- For each command implement `execute(CommandSender sender, String[] args)`:
  - Validate args length and types. If missing/invalid, send `usage()` to sender.
  - Check `permission()` (the dispatcher already checks top-level permission, but implement any additional checks if needed).
  - Call appropriate manager method (e.g., `plugin.getArenaManager().createArena(...)` or `plugin.getGameManager().join(player, arenaId)`).
  - Persist changes by calling `plugin.getArenaManager().saveArenas()` when arena configuration changes.
  - Do NOT invoke any GUI from admin commands at this stage.
- Important: Commands should not implement tab completion now — keep `tabComplete(...)` returning empty lists.
- Tests: manual smoke tests on dev server for each command.

4) `src/main/java/io/github/KennedySovine/hungerGames/game/GameManager.java`
- Responsibilities: basic game lifecycle and minimal behavior needed by commands.
- Implement:
  - `startGame(String arenaId)` — set state and schedule transition tasks (countdown -> RUNNING), broadcast messages.
  - `stopGame(String arenaId)` — cancel tasks, teleport players to lobby, clear player lists, restore inventories.
  - `join(Player player, String arenaId)` — validate arena exists and min/max, add player to lobby, save/clear inventory.
  - `leave(Player player)` — handle leaving while in lobby/running, restore inventory, teleport out.
- Tests: manual join/leave/start/stop flows on dev server.

5) `src/main/java/io/github/KennedySovine/hungerGames/stats/StatsManager.java` & `PlayerStats.java`
- Implement `save()` to write `stats.yml` using `YamlStorage` and in-memory helpers to return a `PlayerStats` for last match only (non-persistent across server restarts unless you choose otherwise).
- Hooks: `PlayerDeathListener` should call `statsManager.addDeath(victim)` and increment killer's kills.
- Tests: verify runtime behavior after a match and that stats are displayed by `/hg stats`.

6) `src/main/java/io/github/KennedySovine/hungerGames/storage/YamlStorage.java`
- Ensure `load(String name)` returns a `FileConfiguration` with sane defaults and `save(String name, FileConfiguration cfg)` writes to plugin data folder.
- Document thread-safety: call save only from main thread.
- Tests: manual ensure files are created in plugin folder on first run.

PHASE 2 — Combat, chest refills & match logic

7) `src/main/java/io/github/KennedySovine/hungerGames/combat/CombatManager.java`
- Implement core combat-logging rules per project spec:
  - `isInCombat(UUID player)` should check last-damage timestamps against a configurable combat window (default 10s).
  - `handleQuit(UUID playerUuid)`:
    - If `isInCombat(uuid)`: mark player dead immediately and call `GameManager` death/cleanup logic. Record death in `StatsManager`.
    - Else: add to `pendingDisconnects` map with an expiry task scheduled for the arena's grace period (default 120s). While pending-disconnect the player's in-world entity should not be visible to other players (implementation: mark as spectator or remove from player lists; note: actual removal of player entity when they disconnect isn't required because the player entity isn't present while offline. You must ensure their state is handled.
  - `handleJoin(UUID playerUuid)` should cancel pending disconnect expiry if present and restore player state if possible; if expired, run death flow.
- Persistence: optionally persist pending disconnects to a small file so server restarts won't re-award grace periods incorrectly.
- Tests: manual disconnect during combat and not-in-combat flows on dev server.

8) Listeners wiring
- `CombatListener` — record attacker/victim damage events and call `CombatManager.recordDamage(...)`.
- `PlayerConnectionListener` — call `CombatManager.handleQuit` on quit and `CombatManager.handleJoin` on join. When players rejoin after being marked dead, ensure they receive a death/spectator notice.
- `PlayerDeathListener` — increment stats and move player to spectator mode.
- `InventoryGuiListener` & `SpawnStickListener` — keep skeleton behavior; GUI and spawn-stick setting will be implemented in PHASE 3.

PHASE 3 — GUI (after commands are stable)

9) `src/main/java/io/github/KennedySovine/hungerGames/gui/ArenaEditorGui.java`
- Implement GUI inventory construction and click handling. Provide an admin flow to set arena properties and give a spawn-stick item. This is deferred until after commands and managers are functional.

PHASE 4 — polish & optional persistence upgrade

10) Optional: SQLite backend
- Implement a `SqlStorage` and migration if you want persistent cross-restart stats or arenas in a DB. This is optional and more advanced.

Testing & QA (detailed)
- Unit tests for pure functions: `LocationUtils` helpers, `CombatManager.isInCombat` boundaries, `ArenaManager` create/delete.
- Integration/manual tests on dev server:
  - `/hg arena create test` then check `arenas.yml` updated.
  - `/hg arena addspawn test` with player; check resulting `arenas.yml` contains spawn.
  - PvP: damage player and disconnect -> immediate death if in combat.
  - Non-combat disconnect -> reconnect within grace restores; after expiry player marked dead.

Completion criteria
- Mark a file done when its listed methods are implemented, compiled without new errors, and manual/integration tests pass.

If you'd like, I can now implement any single manager or command. Reply with which file(s) to implement next (for example: "Implement ArenaManager and CreateArenaCommand").

Note: the `SetLobbyCommand` source file exists as a deprecated placeholder (not registered). You may delete `src/main/java/io/github/KennedySovine/hungerGames/command/admin/SetLobbyCommand.java` from the repository when you want the file removed entirely.
