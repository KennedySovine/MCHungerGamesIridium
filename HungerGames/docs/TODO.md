# TODO - Implementation Roadmap (current status)

This file lists what is implemented, what is partially implemented (has bugs or missing integrations), and what remains to be done. It also provides next-step priorities and actionable, per-file TODOs for you to implement or finish.

Summary status legend:
- DONE: implemented and verified (basic behavior works as expected)
- FIXED: implemented and verified (recently moved from PARTIAL)
- PARTIAL: implemented but has bugs, missing integration, or requires follow-up (see Known Issues)
- TODO / NOT DONE: not implemented yet

Changelog:
- 2026-03-12: Marked `Arena` model and `ArenaManager` as FIXED based on current repository implementations (working arena placeholder, load/save, relative spawn offsets, and copy/save semantics are present in code).

Quick status checklist (work through top-to-bottom):

- [DONE] 1. Storage & utilities: `YamlStorage`, `LocationUtils` (basic implementations exist)
- [FIXED] 2. Arena model: `Arena.java` (POJO exists and provides relative spawn handling and absolute spawn computation)
- [FIXED] 3. Arena persistence & manager: `ArenaManager.java` (working/placeholder arena concept implemented; load/save and working-arena copy/save semantics available)
- [PARTIAL] 4. Commands (admin + player): many command classes exist, but subcommand routing (`/hg arena ...`) is broken in places; commands sometimes require arena argument though working-arena should be used instead.
- [PARTIAL] 5. GameManager: skeleton/hooks exist, but full lifecycle and integration with CombatManager and StatsManager need completion
- [PARTIAL] 6. Chest loot: `ChestLootManager` exists and `src/main/resources/config.yml` has example comments, but weighted selection and integration are incomplete
- [PARTIAL] 7. Combat system: `CombatManager` skeleton implemented but needs robust event hookup and disconnect handling validation
- [PARTIAL] 8. Stats: `StatsManager` and `PlayerStats` exist (match-scoped) but integration & reporting need finishing
- [PARTIAL] 9. Listeners: `PlayerDeathListener`, `PlayerConnectionListener`, `InventoryGuiListener`, `SpawnStickListener` exist but have several behavioral bugs (GUI clicks, spawn-stick behavior, anvil UI sizing)
- [PARTIAL] 10. Arena save/load semantics: relative spawn offsets implemented, but center/save/load lifecycle and beacon updates are buggy
- [PARTIAL] 11. GUI skeletons: `ArenaEditorGui`, `SpectatorGui` present; GUI opens but many click actions are not wired to commands or mis-handle inputs
- [TODO] 12. Tests, QA, polish, and docs updates (unit tests and manual QA remain)

Important: keep the rule that admin commands edit only the working arena in memory; call `/hg arena save` to persist changes. Many existing commands currently accept an arena argument — they must be changed to operate on the loaded working arena only.

Known Issues (reported during development)
- Arena subcommands broken: `/hg arena ...` often returns 'Unknown subcommand arena' or displays a generic help menu. Subcommands (create, load, save, list, addspawn, removespawn, setcenter, centersize, time, maxplayers, minplayers, graceperiod, chestrefill, stop) must be registered as subcommands of `hg` and routed correctly.
- `AnvilPrompt` open created an inventory with size 3 -> Bukkit rejects with IllegalArgumentException. Anvil GUIs must use the proper AnvilView or use a container with correct size; current code opens an Inventory of size 3 which is invalid.
- Spawn stick behavior incorrect: left/right click semantics reversed or not implemented; spawn-stick not removing/adding spawns correctly; spawn distance validation (>=5 blocks) missing.
- Beacons/particles: particle beacon system implemented but not reliably updated when center changes, spawns added/removed, or arena saved/loaded. Beacon visibility toggle command missing or non-functional.
- GUI wiring: inventory editor items do not call the implemented commands; item movement sometimes allowed (inventory events not cancelled). Click handlers sometimes don't send messages (MessageUtils not consistently used).
- Duplicate `plugin.yml` files exist in the project root and in resources — that can confuse build tools; remove/merge duplicates.
- Arena create via GUI/anvil not creating working arena due to above anvil/inventory sizing bug and command routing issues.
- Save/load semantics: several commands still ask for arena id explicitly; should operate on current working arena loaded into memory. `save` refuses to run unless master list contains the arena — `create` should create the master entry and then working arena.
- Unexpected removal of spawn-stick items: spawn stick removal on save/load/rejoin was requested but not fully implemented.

Priority action plan (short)
1) Fix command routing and registration so `/hg arena <sub>` works reliably (HIGH). This is blocking for testing everything else.
2) Ensure admin commands operate on the working arena only (HIGH). Remove arena id args from admin commands and adapt help/usage text.
3) Fix `AnvilPrompt` / custom inventory sizing (HIGH) — use Bukkit's anvil API or create the correct Inventory type/size for an anvil workflow.
4) Fix spawn-stick logic and validation (distance check, left/right actions) and remove spawn-sticks from inventories on save/load/create (MED).
5) Wire GUI inventory clicks to call the already-implemented commands (MED). Ensure clicks are cancelled and Item movement prevented.
6) Fix beacon particle system updating and persistence; add show/hide beacon command (MED).
7) Ensure world border shows for loaded working arena and updates when centerSize changes (MED).
8) Finish ChestLootManager weighted random selection and config parsing (LOW-MED).
9) Complete CombatManager event hooks and reconnect handling (LOW-MED).
10) Add unit tests for `LocationUtils`, `Arena` load/save round-trips, and boundary tests for CombatManager (LOW).

Per-file (actionable) TODOs and current state

1) `src/main/java/io/github/KennedySovine/hungerGames/storage/YamlStorage.java`
- Status: DONE (basic load/save implemented)
- Remaining: add unit tests and ensure synchronous save/isMainThread checks or schedule writes appropriately.
- Files interacting: `ArenaManager`, `ChestLootManager`.

2) `src/main/java/io/github/KennedySovine/hungerGames/utils/LocationUtils.java`
- Status: DONE (serialize/deserialize & relative offset utilities present)
- Remaining: unit tests, and guard/descriptive errors when worlds referenced are missing.
- Interactions: used by `Arena` and `ArenaManager` when saving/loading spawn points.

3) `src/main/java/io/github/KennedySovine/hungerGames/arena/Arena.java`
- Status: FIXED
- Remaining methods to verify/implement:
  - validate() that enforces min/max/time/centerSize constraints (optional extra)
  - toConfig()/fromConfig() round-trip reliable handling of spawn relative strings (ArenaManager performs config mapping today)
  - getSpawnLocations(Location center) accurate application of relative offsets (implemented as `getAbsoluteSpawns`)
- Global vars to check: id, displayName, minPlayers, maxPlayers, centerSize, gracePeriodSeconds, chestRefillSeconds, spawnStrings

4) `src/main/java/io/github/KennedySovine/hungerGames/arena/ArenaManager.java`
- Status: FIXED
- Remaining tasks:
  - Verify loadWorkingArena()/saveWorkingArena() semantics (ensure working arena is an in-memory copy that does not modify master until save is called)
  - Provide createArena() that writes an initial master entry and loads it into working arena immediately (createArena exists; ensure callers follow workflow)
  - Ensure add/remove spawn APIs work with relative offsets and with current working arena only (implemented: addSpawnPointToWorking/removeSpawnPointFromWorking)
  - Implement clearWorkingArena() and working-arena placeholder name handling (optional convenience)

5) Commands: `src/main/java/io/github/KennedySovine/hungerGames/command/` (many files)
- Status: PARTIAL
- High priority fixes:
  - Ensure `HgCommand` (top-level) registers and dispatches `arena` subcommand properly. `arena` should then dispatch to `create`, `load`, `save`, `list`, `addspawn`, `removespawn`, `setcenter`, `centresize`, `time`, `maxplayers`, `minplayers`, `graceperiod`, `chestrefill`, `stop`.
  - Update admin commands to not take an arena id argument — operate on working arena only and throw readable errors if no working arena loaded or `create` not yet persisted.
  - Add `save` command that persists the working arena to master list and refuses to save if the arena id is missing from the master list (per your rules) — but `create` should create the master entry and allow save thereafter.
  - Remove `setlobby` command and file (already done but verify no lingering references remain).
  - Replace any hard-coded color codes with `MessageUtils` usage across all commands.

6) `src/main/java/io/github/KennedySovine/hungerGames/game/GameManager.java`
- Status: PARTIAL
- Remaining: finish join/leave logic, start/stop lifecycle, chest refill scheduling, integration with CombatManager and StatsManager. Ensure world border set for working arena when loaded.

7) `src/main/java/io/github/KennedySovine/hungerGames/chest/ChestLootManager.java`
- Status: PARTIAL
- Remaining: parse `config.yml` loot tiers, implement weighted random selection, and a populateChest(Inventory, tier) method. Provide example config entries in `src/main/resources/config.yml` (already present but commented).

8) `src/main/java/io/github/KennedySovine/hungerGames/combat/CombatManager.java`
- Status: PARTIAL
- Remaining: ensure recordDamage() and isInCombat() are called by event listeners (CombatListener), implement handleQuit()/handleJoin() integration with GameManager, and add tests for the 10s combat timeout and 2min grace period.

9) Listeners
- `CombatListener` — PARTIAL: check projectile handling and cancelled events.
- `PlayerConnectionListener` — PARTIAL: ensure reconnect cancels pending death tasks; disconnected players are hidden while offline.
- `PlayerDeathListener` — PARTIAL: ensure spectator transition and stats updates happen reliably.
- `InventoryGuiListener` — PARTIAL: prevent item moving, route clicks to command invocations.
- `SpawnStickListener` — PARTIAL: implement left-click remove, right-click add; enforce 5-block distance rule; remove sticks on save/load/create.

10) GUI Skeletons
- `ArenaEditorGui` — PARTIAL: build inventory and open for player; remove placeholder UI code and wire click items to execute existing commands (do not duplicate logic).
- `SpectatorGui` — PARTIAL: build simple menu for teleporting to players.
- Action: Remove placeholder comments and ensure no inventory size errors (inventories must be multiples of 9).

11) Resources
- `src/main/resources/config.yml` — DONE (exists with commented examples): keep example chest-loot entries but empty by default.
- `plugin.yml` — ensure single source of truth in `src/main/resources` and root `plugin.yml` duplicates removed. Verify every command declared in `plugin.yml` corresponds to implemented command classes. Also ensure `name`, `version`, `main`, `api-version`, and `commands` sections are correct.

12) Tests & QA
- TODO: add unit tests for `LocationUtils`, arena save/load, and CombatManager timing boundaries.

Developer-facing priorities (next immediate steps)
1. Fix command routing so `/hg arena ...` subcommands work (blocker).
2. Fix AnvilPrompt inventory sizing / implementation (blocker for GUI create flow).
3. Enforce working-arena-only edits across all admin commands and update help text accordingly.
4. Wire GUI clicks to call commands using MessageUtils for feedback, and cancel inventory movement.
5. Fix spawn stick add/remove semantics and distance validation.
6. Ensure beacon particle tasks are tracked so they can be canceled/updated when spawns/center change.
7. Make world border visually reflect working arena center and center-size live.

Notes about your specific requests and my recommended approach
- Working arena memory model: keep a `workingArena` copy in `ArenaManager`. All admin edit commands mutate `workingArena`. `ArenaManager.saveWorkingArena()` writes the `workingArena` to the master arenas map and persists `arenas.yml`.
- GUI command wiring: use `Bukkit.dispatchCommand(sender, commandString)` or call manager methods directly instead of duplicating logic.
- Message formatting: centralize strings with `MessageUtils.send(sender, key, args...)` or similar; replace color-code literals (`&c`) everywhere.
- Particle beacons: track spawned BukkitRunnables in a Map keyed by an identifier (e.g., arenaId + spawnIndex or 'center') so you can cancel and restart them when the center or spawn points change.

Appendix — Quick checklist to mark progress when changing files
- After making any code edits, run a project build (`./gradlew build`) and ensure a single `plugin.yml` is packaged.
- Test the following commands locally on dev server in order after fixing routing:
  1. `/hg arena create TEST` -> master arenas.yml should contain TEST
  2. `/hg arena load TEST` -> working arena loaded; world border updates
  3. `/hg addspawn` (with working arena loaded and standing at center) -> spawn saved as relative offset
  4. `/hg arena save` -> master updated
  5. `/hg arena list` -> lists all arenas

If you'd like, I can:
- Fix command routing next (HIGH priority) and run a build to verify packaging.
- Fix the AnvilPrompt inventory sizing error so GUI create works.
- Go through codebase and replace color-coded messages with `MessageUtils` calls.

Tell me which of the above you want me to do next and I will continue implementing and testing until green.
