# TODO - Implementation Roadmap (detailed per-file tasks)

This file lists exactly what you (the developer) need to implement inside each skeleton file that exists in the repository. For each file I list: the responsibilities, the specific methods to fill in, edge-cases to handle, and how to test the result.

Quick plan for this update
- Add explicit combat-logging rules and reconnect/grace behavior.
- Lock down the exact commands to implement now (commands-first, GUI later).
- Call out chest-loot config usage and class stubs to implement.
- Add per-file actionable TODOs reflecting the current codebase.

General rules
- Add Javadoc to every public class and public method explaining inputs/outputs and side-effects.
- Inline-comment any non-trivial logic (parsing, thread expectations, permission checks).
- All Bukkit API calls must run on the server main thread. Use the scheduler for delayed tasks.
- Persist changes using `YamlStorage` (use `getStorage()` from the main plugin) unless noted otherwise.

Project decisions (explicit)
- Commands-first: implement all command classes and managers before building GUIs.
- No kits or teams in this phase — skip them entirely.
- Stats are NOT persistent across games. Stats exist only for the duration of a single match and are queryable only after a game ends.
- Combat-logging rules (exact):
  - If a player receives damage, they are considered "in combat".
  - A player remains "in combat" until 10 seconds have passed since the last damage event.
  - If a player disconnects while considered "in combat" (including the 10s trailing window), treat them as immediately killed on disconnect. If they reconnect, show death/spectator state and a short message indicating they were killed due to combat logging.
  - If a player disconnects while NOT in combat, start a 120-second (2 minute) reconnect grace timer. If the player returns before the grace expires, restore their match state. If the grace expires, mark the player dead and run the normal death flow.
  - While a player is disconnected and in-grace, do not expose a physical offline player entity to others (players offline have no in-world entity). Ensure match bookkeeping holds their slot but prevents griefing/exploitation.

Commands (these are the ONLY commands to implement now)
- Admin commands (permission: `hungergames.admin`):
  - /hg arena create <MAPNAME>
  - /hg arena delete <MAPNAME>
  - /hg maxplayers <MAPNAME> <NUMBER>
  - /hg minplayers <MAPNAME> <NUMBER>
  - /hg time <MAPNAME> <TIME_IN_SECONDS>
  - /hg centresize <MAPNAME> <SIZE>
  - /hg graceperiod <MAPNAME> <TIME_IN_SECONDS>
  - /hg chestrefill <MAPNAME> <TIME_IN_SECONDS>
  - /hg addspawn <MAPNAME>
  - /hg removespawn <MAPNAME> <INDEX>
  - /hg stop
- Player commands (no permission required):
  - /hg join <MAPNAME>
  - /hg leave <MAPNAME>
  - /hg stats (shows only post-game stats for the last finished match)

Notes:
- Do NOT wire any admin commands to GUI code yet — commands must perform only data/manager operations and persist via `ArenaManager`.
- Tab completion logic is intentionally omitted for now.

Configuration
- `src/main/resources/config.yml` holds the `chest-loot` mapping. The plugin must expose a `ChestLootManager` (or similar) which reads this mapping and provides `populateChest(Inventory)`.
- Runtime config location: `plugins/HungerGames/config.yml` (created via `saveDefaultConfig()` in main class).
- The `chest-loot` format (example in `config.yml`) must be followed. The `ChestItem` model should include: material, min, max, weight, enchants.

PHASE 1 — Commands & Managers (implement these first)

1) `src/main/java/io/github/KennedySovine/hungerGames/arena/Arena.java` (POJO)
- Responsibilities: represent arena settings and spawn locations.
- Implement:
  - Fields: id, displayName, minPlayers, maxPlayers, centerSize (int), gracePeriodSeconds, chestRefillSeconds, lobbyLocation (optional), List<String> spawnStrings.
  - serialize/deserialize helpers for locations using `LocationUtils` (to/from String) for lobby and spawns.
  - `validate()` to ensure minPlayers <= maxPlayers and non-negative numeric values.
- Tests: create an Arena in-memory and call serialize helpers; ensure round-trip.

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
- Files: `admin/` and `player/` command classes present in the repo (CreateArenaCommand, AddSpawnCommand, RemoveSpawnCommand, DeleteArenaCommand, MaxPlayersCommand, MinPlayersCommand, TimeCommand, CenterSizeCommand, GracePeriodCommand, ChestRefillCommand, StartCommand, StopCommand, JoinCommand, LeaveCommand, StatsCommand).
- For each command implement `execute(CommandSender sender, String[] args)`:
  - Validate args length and types. If missing/invalid, send `usage()` to sender.
  - Check `permission()` as applicable (top-level admin permission is checked by the dispatcher).
  - Call appropriate manager method (e.g., `plugin.getArenaManager().createArena(...)` or `plugin.getGameManager().join(player, arenaId)`).
  - Persist changes by calling `plugin.getArenaManager().saveArenas()` when arena configuration changes.
  - Do NOT invoke any GUI from admin commands at this stage.
- Important: Commands should not implement tab completion now — keep `tabComplete(...)` returning empty lists.
- Tests: manual smoke tests on dev server for each command.

4) `src/main/java/io/github/KennedySovine/hungerGames/game/GameManager.java`
- Responsibilities: basic game lifecycle, minimal behavior for commands to use.
- Implement:
  - `startGame(String arenaId)` — set state and schedule transition tasks (countdown -> RUNNING), broadcast messages.
  - `stopGame(String arenaId)` — cancel tasks, teleport players to a safe location, clear player lists, restore inventories.
  - `join(Player player, String arenaId)` — validate arena exists and min/max, add player to lobby, save/clear inventory.
  - `leave(Player player)` — handle leaving while in lobby/running, restore inventory, teleport out.
- Tests: manual join/leave/start/stop flows on dev server.

5) `src/main/java/io/github/KennedySovine/hungerGames/stats/StatsManager.java` & `PlayerStats.java`
- Implement `collectForMatch()` to assemble in-memory stats for the current/last match only (non-persistent across server restarts).
- `PlayerStats` should include: kills, deaths, wins/losses for match duration only.
- Hooks: `PlayerDeathListener` should call `statsManager.addDeath(victim)` and increment killer's kills.
- Tests: verify save/load round-trip.

6) `src/main/java/io/github/KennedySovine/hungerGames/storage/YamlStorage.java`
- Implemented basic load/save. Your tasks:
  - Ensure `load` returns a `FileConfiguration` with defaults when resource exists.
  - Add thread-safety notes: only call save() from main thread or schedule via Bukkit.
- Tests: manual ensure files are created in plugin folder on first run.

PHASE 2 — Combat, chests & match logic

7) `src/main/java/io/github/KennedySovine/hungerGames/combat/CombatManager.java`
- Implement core combat-logging rules per project spec (see "Project decisions" above):
  - `recordDamage(UUID damager, UUID victim)` — update last-damage timestamps for victim and mark them in combat.
  - `isInCombat(UUID player)` — check last-damage timestamp against 10s window.
  - `handleQuit(UUID playerUuid)`:
    - If `isInCombat(uuid)`: mark player dead immediately and call `GameManager` death/cleanup logic. Record death in `StatsManager`.
    - Else: add to `pendingDisconnects` map with an expiry task scheduled for the arena's grace period (120s default). If grace expires, mark dead.
  - `handleJoin(UUID playerUuid)` — cancel pending disconnect expiry if present and restore player state; if expired, run death flow.
- Persistence: optionally persist pending disconnects to a small file so server restarts won't re-award grace periods incorrectly. This is optional.
- Tests: manual disconnect during combat and not-in-combat flows on dev server.

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
- Tick the checkbox in this TODO file when the file's listed methods are implemented, compiled without warnings (or minimal unavoidable warnings), and manual/integration tests pass as described.

If you want, I can now implement the `ArenaManager.loadArenas()`/`saveArenas()` and wire `CreateArenaCommand` so `/hg arena create <id>` works and writes to `arenas.yml`. Reply "Implement ArenaManager & CreateArena" to have me do that next.
