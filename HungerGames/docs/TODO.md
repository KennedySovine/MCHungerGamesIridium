# TODO - Implementation Roadmap (detailed per-file tasks)

This file lists exactly what you (the developer) need to implement inside each skeleton file that exists in the repository. For each file I list: the responsibilities, the specific methods to fill in, edge-cases to handle, and how to test the result.

General rules
- Add Javadoc to every public class and public method explaining inputs/outputs and side-effects.
- Inline-comment any non-trivial logic (parsing, thread expectations, permission checks).
- All Bukkit API calls must run on the server main thread. Use the scheduler for delayed tasks.
- Persist changes using `YamlStorage` (use `getStorage()` from the main plugin) unless noted otherwise.

PHASE 1 — Commands & Managers (implement these first)

1) `src/main/java/io/github/KennedySovine/hungerGames/arena/Arena.java` (POJO)
- Done: Fields and getters/setters exist.
- Your tasks:
  - (Optional) Add validation helpers (e.g., `validate()` to ensure min/max players sane).
  - Add convenience methods to serialize/deserialize lobby & spawns using `LocationUtils`.
- Test: create an Arena in-memory and call serialize helpers.

2) `src/main/java/io/github/KennedySovine/hungerGames/arena/ArenaManager.java`
- Responsibilities: load/save arenas.yml, create/delete arenas, add/remove spawns, provide lookups.
- Implement:
  - `loadArenas()` — read `arenas.yml` via `plugin.getStorage().load("arenas.yml")`, parse each arena node, create `Arena` objects, parse spawn strings via `LocationUtils.deserialize`.
  - `saveArenas()` — convert each `Arena` into YAML structure and `plugin.getStorage().save("arenas.yml", cfg)`.
  - Update `createArena(...)` to call `saveArenas()` optionally or document that callers must call save.
  - Add `listSpawns(String arenaId)` helper to return spawn count and indices (useful for tab-completion).
- Edge-cases: missing world for spawn location -> skip with warning. Arena id collisions. Atomic save: save to tmp then rename (optional).
- Tests: unit test load/save round-trip using a temp folder and `YamlConfiguration`.

3) `src/main/java/io/github/KennedySovine/hungerGames/command/*` (subcommand classes)
- Files: CreateArenaCommand, AddSpawnCommand, RemoveSpawnCommand, MaxPlayersCommand, MinPlayersCommand, TimeCommand, CenterSizeCommand, GracePeriodCommand, ChestRefillCommand, StartCommand, StopCommand, JoinCommand, LeaveCommand, StatsCommand.
- For each command implement:
  - `execute(CommandSender sender, String[] args)` with argument validation, permission checks (use `permission()`), and helpful usage messages when args missing.
  - Real calls to the relevant manager on success. Examples:
    - `CreateArenaCommand` -> call `plugin.getArenaManager().createArena(id, displayName)` and `saveArenas()`.
    - `AddSpawnCommand` -> require `sender` to be a `Player`, get player location, call `arena.addSpawn(loc)` and `saveArenas()`.
    - `RemoveSpawnCommand` -> parse index, validate bounds, call `arena.removeSpawn(index)` and `saveArenas()`.
    - `MaxPlayersCommand/MinPlayersCommand/TimeCommand/CenterSizeCommand/GracePeriodCommand/ChestRefillCommand` -> parse integer (use `parseInt` helper), set on Arena and save.
    - `StartCommand/StopCommand` -> call `plugin.getGameManager().startGame(arenaId)` / `.stopGame(arenaId)`.
    - `JoinCommand/LeaveCommand` -> call `plugin.getGameManager().join(player, arenaId)` / `.leave(player)`.
    - `StatsCommand` -> use `plugin.getStatsManager()` to fetch and format stats; if args[0] provided and sender has `HungerGames.stats.view`, allow viewing others.
- TabComplete: implement `tabComplete(...)` to return arena names from `plugin.getArenaManager().getArenaNames()` and indices where relevant.
- Tests: manual smoke tests for each command on dev server and unit tests for arg parsing helpers.

4) `src/main/java/io/github/KennedySovine/hungerGames/game/GameManager.java`
- Responsibilities: basic game lifecycle, minimal behavior for commands to use.
- Implement:
  - `startGame(String arenaId)` — set state to LOBBY/COUNTDOWN then schedule transition to RUNNING; at minimum record state and broadcast a message.
  - `stopGame(String arenaId)` — cancel scheduled tasks, teleport players to lobby, clear players lists, reset inventories if necessary.
  - `join(Player player, String arenaId)` — check arena exists, check min/max, add player to the lobby list, teleport to lobby location, save player's inventory (store in a temp map) and clear for match start.
  - `leave(Player player)` — restore saved inventory, teleport out to arena lobby/world spawn.
- Tests: manual join/leave/start/stop flows on dev server.

5) `src/main/java/io/github/KennedySovine/hungerGames/stats/StatsManager.java` & `PlayerStats.java`
- Implement `save()` to write `stats.yml` using `YamlStorage` and `getStats(UUID)` to load as needed.
- Add `load()` optionally on plugin enable to populate `stats` map.
- Hooks: `PlayerDeathListener` should call `statsManager.addDeath(victim)` and increment killer's kills.
- Tests: verify save/load round-trip.

6) `src/main/java/io/github/KennedySovine/hungerGames/storage/YamlStorage.java`
- Implemented basic load/save. Your tasks:
  - Ensure `load` returns a `FileConfiguration` with defaults when resource exists.
  - Add thread-safety notes: only call save() from main thread or schedule via Bukkit.
- Tests: manual ensure files are created in plugin folder on first run.

PHASE 2 — Combat, chests & match logic

7) `src/main/java/io/github/KennedySovine/hungerGames/combat/CombatManager.java`
- Implement:
  - `recordDamage(UUID attacker, UUID victim)` already sets timestamps; ensure it updates persistent last-damage if you choose to persist.
  - `isInCombat(UUID player)` uses `combatWindowMs`. Make `combatWindowMs` configurable via `config.yml` and load in constructor.
  - `handleQuit(UUID playerUuid)`:
    - If `isInCombat(uuid)`: mark player dead immediately. Steps: call `plugin.getGameManager().leave(...)` or `GameManager.handleDeath(...)` and persist stat as death. Optionally store a flag so on reconnect they get a "you were killed while offline" message.
    - Else: add to `pendingDisconnects` data structure, schedule a Bukkit `runTaskLater` to expire after `gracePeriod` (per-arena or default). Save pending disconnects to `pending-disconnects.yml` using `YamlStorage`.
  - `handleJoin(UUID playerUuid)`:
    - If pending disconnect exists: cancel scheduled task and restore player's state; if expired -> do death flow.
- Persistence: `pending-disconnects.yml` — implement `loadPendingDisconnects()` on plugin enable and reschedule expiry tasks for players still offline.
- Tests: unit test `isInCombat` behavior; manual test disconnect during combat and not-in-combat.

8) Listeners wiring (files already created)
- `CombatListener` — handle projectile sources (Arrows, Spectral Arrows, Tridents). Use `Projectile#getShooter()` to extract player.
- `PlayerConnectionListener` — already delegates to CombatManager; ensure PlayerJoin handles messages for players that rejoined after being marked dead.
- `PlayerDeathListener` — call `statsManager.addDeath` and `addKill` for killer; set player to spectator and call `GameManager` to remove player from active participants.
- `InventoryGuiListener` & `SpawnStickListener` — implement GUI detection and spawn-stick PDC parsing.

9) `loot.yml` and chest refill
- Provide `src/main/resources/loot.yml` with example tiers (starter/refill). Implement loader in GameManager or separate `LootManager` to parse item strings.
- Implement chest refill scheduler in `GameManager.startGame`: schedule a repeating task that refills chests by finding container blocks (or track configured chest locations per arena).

PHASE 3 — GUI (after commands are stable)

10) `src/main/java/io/github/KennedySovine/hungerGames/gui/ArenaEditorGui.java` and `SpectatorGui.java`
- Tasks:
  - Implement `buildMainInventory(arenaId)`: build a 27/54-slot inventory, place items for each setting (with lore showing current value), Save/Cancel items.
  - Implement `giveSpawnStick(Player admin, String arenaId)` using `ItemMeta` + `PersistentDataContainer` key `hungergames:spawnstick` with arenaId value.
  - Implement Inventory click handling in `InventoryGuiListener` to detect these GUIs by title or custom InventoryHolder and dispatch actions (open number prompt via chat or sign input for numeric values).
- Tests: create GUI as admin and click items to ensure proper flows; give spawn-stick and right-click to record spawns.

PHASE 4 — polish & persistence upgrade

11) Optional: SQLite backend
- Add JDBC dependency and implement `SqlStorage` that satisfies the same interface as `YamlStorage`.
- Implement migration script from YAML files to DB on first run.

Testing & QA (detailed)
- Unit tests for pure functions:
  - `LocationUtils.serialize/deserialize` round-trip.
  - `CombatManager.isInCombat` boundary (exactly 10s, just over 10s).
  - `ArenaManager.normalizeId` and create/delete logic.
- Integration/manual tests (dev server):
  - `/hg arena create test` then check `arenas.yml` updated.
  - `/hg arena addspawn test` with player; check resulting `arenas.yml` contains spawn line.
  - PvP: damage player and disconnect -> immediate death message.
  - Non-combat disconnect -> reconnect within grace restores; after expiry player marked dead.

Completion criteria (how to mark items done)
- Tick the checkbox in this TODO file when the file's listed methods are implemented, compiled without warnings (or minimal unavoidable warnings), and manual/integration tests pass.

If you want, I can now implement the `ArenaManager.loadArenas()`/`saveArenas()` and wire `CreateArenaCommand` so `/hg arena create <id>` works and writes to `arenas.yml`. Reply "Implement ArenaManager & CreateArena" to have me do that next.
