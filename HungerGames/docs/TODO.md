# TODO - Implementation Roadmap (actionable per-file tasks)

Task: update TODO to match the ordered plan (commands & managers first, then combat, chest/loot, listeners, game & stats, GUI, polish) and enumerate the exact methods and global variables you must implement in each file.

Quick status checklist (work through top-to-bottom):
- [ ] 1. Storage & utilities: `YamlStorage`, `LocationUtils`.
- [ ] 2. Arena model: `Arena.java`.
- [ ] 3. Arena persistence & manager: `ArenaManager.java`.
- [ ] 4. Commands (admin + player): implement all listed command classes (commands-first).
- [ ] 5. GameManager: minimal lifecycle hooks used by commands.
- [ ] 6. Chest loot: `ChestLootManager` + `src/main/resources/config.yml` example.
- [ ] 7. Combat system: `CombatManager` + `CombatListener` + connection handling.
- [ ] 8. Stats: `StatsManager`, `PlayerStats` (match-scoped only).
- [ ] 9. Listeners: `PlayerDeathListener`, `PlayerConnectionListener`, `InventoryGuiListener`, `SpawnStickListener`.
- [ ] 10. Arena save/load semantics: relative spawn offsets (save when admin stands at center; load relative to current center position).
- [ ] 11. GUI skeletons (defer full UX): `ArenaEditorGui`, `SpectatorGui`.
- [ ] 12. Tests, QA, polish, and docs updates (this TODO itself is part of docs).

Notes and cross-cutting rules
- Javadoc: add Javadoc to every public class and public method explaining inputs/outputs and side-effects.
- Threading: All Bukkit API calls must run on the main thread. Use scheduler for delayed tasks.
- Persistence: use `YamlStorage` for file-based persistence. `plugin.getStorage()` is the single entrypoint.
- No kits or teams in this phase.
- Stats are non-persistent across server restarts — only match-scoped and only queryable after game end.
- `setlobby` command removed. Delete file `src/main/java/io/github/KennedySovine/hungerGames/command/admin/SetLobbyCommand.java`.

PHASE 1 — Commands & Core Managers (highest priority)

1) `src/main/java/io/github/KennedySovine/hungerGames/storage/YamlStorage.java`
- Responsibilities: load/save YAML config files under plugin data folder. Provide `load(String)` and `save(String, FileConfiguration)`.
- Methods to implement:
  - FileConfiguration load(String filename)
  - void save(String filename, FileConfiguration cfg)
  - File getFile(String filename)   // helper
- Global variables / fields:
  - JavaPlugin plugin  // reference to the main plugin
  - File dataFolder
- Interactions:
  - Used by `ArenaManager`, `ChestLootManager`, other managers needing persistence.
- Testing:
  - Ensure `load` returns an empty-but-valid `FileConfiguration` if file absent and `save` writes a file.
  - Test that `save` is called from main thread or schedule a task when called from async.

2) `src/main/java/io/github/KennedySovine/hungerGames/utils/LocationUtils.java`
- Responsibilities: convert `Location` <-> String representation, and helper math for relative offsets.
- Methods to implement:
  - String serialize(Location loc)  // include world name, x,y,z,yaw,pitch
  - Location deserialize(String s) // returns null if world missing
  - String serializeRelative(Location spawn, Location center) // stores offset vector only (dx,dy,dz,yaw,pitch) along with maybe worldless marker
  - Location deserializeRelative(String relative, Location center) // reconstruct spawn as center + offset
  - Vector toVector(Location a, Location b) // optional helper
- Global variables / fields:
  - None required (stateless utility class) — keep methods static.
- Interactions:
  - Used by `Arena` and `ArenaManager` when saving/loading spawn points.
- Testing:
  - Round-trip tests for serialize/deserialize and relative methods.

3) `src/main/java/io/github/KennedySovine/hungerGames/arena/Arena.java`
- Responsibilities: POJO representing arena settings and stored spawn offset strings.
- Methods to implement:
  - static Arena fromConfig(ConfigurationSection section) // parse an arena node
  - void toConfig(ConfigurationSection section) // write arena into YAML
  - void validate() throws IllegalArgumentException // enforces min <= max etc.
  - List<Location> getSpawnLocations(Location center) // converts spawnStrings->Locations using LocationUtils.deserializeRelative
  - void addSpawnRelative(Location spawnRelative) // accept a relative string or Location and store as spawnStrings
- Fields / global variables to add:
  - String id
  - String displayName
  - int minPlayers
  - int maxPlayers
  - int centerSize
  - int gracePeriodSeconds
  - int chestRefillSeconds
  - String lobbyLocationString (optional)
  - List<String> spawnStrings  // each stored as relative offset string
- Interactions:
  - `ArenaManager` will call `fromConfig`/`toConfig` during load/save.
  - `GameManager` will read `getSpawnLocations(center)` to place players when joining/starting.
- Testing:
  - Create an Arena, call `toConfig`, then `fromConfig` with the same data and compare fields.

4) `src/main/java/io/github/KennedySovine/hungerGames/arena/ArenaManager.java`
- Responsibilities: manage in-memory arenas, load/save all arenas to `arenas.yml`, create/delete arenas, add/remove spawns, provide lookups.
- Methods to implement:
  - void loadArenas()  // read arenas.yml and populate internal map
  - void saveArenas()  // write internal arenas to arenas.yml atomically
  - Arena createArena(String id, CommandSender createdBy) // create minimal arena and return it
  - boolean deleteArena(String id)
  - Arena getArena(String id)
  - List<String> listArenaIds()
  - void addSpawn(String arenaId, String relativeSpawnString) // store as relative strings
  - String removeSpawn(String arenaId, int index) // return removed string
- Fields / global variables:
  - Map<String, Arena> arenas  // id->Arena
  - JavaPlugin plugin  // reference
  - YamlStorage storage  // convenience reference to plugin.getStorage()
- Interactions:
  - `CreateArenaCommand`, `AddSpawnCommand`, `RemoveSpawnCommand`, other admin commands call these methods.
  - `LocationUtils` for serializing/deserializing relative spawn strings.
- Important semantics to implement:
  - Save spawns relative to the player's location when the admin runs addspawn (admin must stand at arena center before saving; TODO: enforce or document requirement).
  - When loading an arena at runtime, do NOT resolve relative spawn strings into absolute Locations until a load-time center is provided by the caller (e.g., when an admin runs a 'load here' command or when GameManager uses a center location).
- Testing:
  - Create arena, add spawns while simulating center, save/read file, and verify contents.

5) Commands (folder: `src/main/java/io/github/KennedySovine/hungerGames/command/`)
- Responsibilities: Validate args, call manager methods, persist changes where appropriate. No GUI calls or tab-completion logic for now.
- Top-level command: `HgCommand` (dispatcher) — ensure it routes to the correct subcommands.

For each admin command implement the following methods (common contract):
  - boolean execute(CommandSender sender, String label, String[] args)
  - String permission()  // return `hungergames.admin` for admin commands
  - String usage() // usage message
  - List<String> tabComplete(CommandSender sender, String[] args) // return empty list for now

List of admin command classes to implement and the manager calls they must make:
- `CreateArenaCommand` (execute -> plugin.getArenaManager().createArena(id, sender); plugin.getArenaManager().saveArenas();)
- `DeleteArenaCommand` (execute -> delete and save)
- `MaxPlayersCommand` (execute -> set maxPlayers in arena, validate, save)
- `MinPlayersCommand` (execute -> set minPlayers in arena, validate, save)
- `TimeCommand` (execute -> set chest/border time setting, save)
- `CenterSizeCommand` (execute -> set centerSize, save)
- `GracePeriodCommand` (execute -> set gracePeriodSeconds, save)
- `ChestRefillCommand` (execute -> set chestRefillSeconds, save)
- `AddSpawnCommand` (execute -> capture admin's location as center; compute relative spawn string via LocationUtils.serializeRelative(adminSpawn, center) and call arenaManager.addSpawn(...); save)
- `RemoveSpawnCommand` (execute -> remove spawn by index and save)
- `StopCommand` (execute -> plugin.getGameManager().stopGame(currentArena) )

Player commands (no permission):
- `JoinCommand` (execute -> plugin.getGameManager().join(player, arenaId))
- `LeaveCommand` (execute -> plugin.getGameManager().leave(player))
- `StatsCommand` (execute -> plugin.getStatsManager().printLastMatchStats(player))

Global variables most command classes will need:
  - HungerGames plugin reference
  - ArenaManager arenaManager
  - GameManager gameManager

Testing:
  - Manual: run each command on dev server and check expected changes in `arenas.yml` and in-memory states.

PHASE 2 — Game, Combat, Chests

6) `src/main/java/io/github/KennedySovine/hungerGames/game/GameManager.java`
- Responsibilities: minimal lifecycle required by commands and CombatManager. Manage players joining/leaving, start/stop games, schedule chest refills.
- Methods to implement:
  - void join(Player player, String arenaId)
  - void leave(Player player)
  - void startGame(String arenaId)
  - void stopGame(String arenaId)
  - boolean isRunning(String arenaId)
  - Optional helpers: List<Player> getPlayers(String arenaId)
- Fields:
  - Map<String, ArenaRun> runningArenas // ArenaRun is an internal class that stores state for a running match (players, tasks, chest locations)
  - JavaPlugin plugin
  - ArenaManager arenaManager
  - ChestLootManager chestLootManager
  - StatsManager statsManager
- Interactions:
  - Commands call join/leave/start/stop.
  - CombatManager will call into GameManager to mark players dead and run death flow.
- Testing:
  - Manual: join players, start, check scheduled chest refills run, stop game clears state.

7) `src/main/java/io/github/KennedySovine/hungerGames/chest/ChestLootManager.java`
- Responsibilities: read `config.yml` chest-loot mapping and provide a method to populate a chest `Inventory`.
- Methods to implement:
  - void reloadConfig()  // read plugin.getConfig()
  - void populateChest(Inventory inv, String tier) // tier example: 'starter' or 'refill'
  - List<ChestItem> parseLootSection(ConfigurationSection section)
- Fields:
  - JavaPlugin plugin
  - Map<String, List<ChestItem>> lootTiers  // tierName -> list of ChestItems
- ChestItem model (new class) fields:
  - Material material
  - int min
  - int max
  - int weight
  - Map<Enchantment, Integer> enchants (optional)
- Interactions:
  - Used by GameManager to refill chests on schedule.
  - Config sample lives in `src/main/resources/config.yml` with commented examples.
- Testing:
  - Manual: invoke `populateChest` on a test Inventory and assert items are chosen per weights.

8) `src/main/java/io/github/KennedySovine/hungerGames/combat/CombatManager.java`
- Responsibilities: implement combat-logging rules and disconnect/reconnect grace periods.
- Methods to implement:
  - void recordDamage(UUID damager, UUID victim)
  - boolean isInCombat(UUID uuid) // true if last damage within 10 seconds
  - void handleQuit(UUID uuid) // called when player disconnects
  - void handleJoin(UUID uuid, Player player) // called when player rejoins
  - void markDead(UUID uuid, String reason) // common death flow call
- Fields / global variables:
  - Map<UUID, Long> lastDamageTimestamp  // epoch millis of last taken damage
  - Map<UUID, BukkitTask> pendingGraceTasks // scheduled task for gracetime expiry
  - long combatTimeoutMillis = 10_000L
  - long defaultGraceMillis = 120_000L  // can be overridden per arena
  - JavaPlugin plugin
  - GameManager gameManager
  - StatsManager statsManager
- Important behaviors to document in comments:
  - If isInCombat on quit -> immediately call markDead with reason 'combat-logged' and record stat.
  - If not in combat -> schedule grace task (per-arena override) and keep a lightweight disconnected record (no in-world entity). If grace expires, call markDead.
  - If a player rejoins before grace expires -> cancel the pending task and restore match slot.
- Testing:
  - Manual: PvP damage then disconnect -> immediate death. Disconnect without combat -> reconnect within 120s restores.

PHASE 3 — Listeners & Stats

9) `src/main/java/io/github/KennedySovine/hungerGames/listener/CombatListener.java`
- Responsibilities: listen for EntityDamageByEntity and projectile hits, call `CombatManager.recordDamage` with correct UUIDs.
- Methods to implement:
  - @EventHandler void onEntityDamageByEntity(EntityDamageByEntityEvent e)
- Fields:
  - CombatManager combatManager
- Interactions: Projectile shooter resolution, arrow metadata, indirect damagers. Respect cancelled events and non-Player entities.

10) `src/main/java/io/github/KennedySovine/hungerGames/listener/PlayerConnectionListener.java`
- Responsibilities: handle player join/quit and delegate to CombatManager for grace logic.
- Methods to implement:
  - @EventHandler void onPlayerQuit(PlayerQuitEvent e)
  - @EventHandler void onPlayerJoin(PlayerJoinEvent e)
- Fields:
  - CombatManager combatManager
  - GameManager gameManager
- Testing:
  - Simulate disconnects during combat and out-of-combat to ensure proper flows.

11) `src/main/java/io/github/KennedySovine/hungerGames/listener/PlayerDeathListener.java`
- Responsibilities: hook into death event and call `GameManager` and `StatsManager` to update state and place player into spectator mode.
- Methods to implement:
  - @EventHandler void onPlayerDeath(PlayerDeathEvent e)
- Fields:
  - GameManager gameManager
  - StatsManager statsManager
- Interactions:
  - Ensure death handling cleans up the player's active match slot and moves them to spectator (teleport, gamemode, inventory clear as appropriate).

12) `src/main/java/io/github/KennedySovine/hungerGames/stats/StatsManager.java` & `PlayerStats.java`
- Responsibilities: collect player stats for the duration of a single match and present them after a match ends.
- Methods to implement in StatsManager:
  - void recordKill(UUID killerUuid, UUID victimUuid)
  - void recordDeath(UUID victimUuid, @Nullable UUID killerUuid)
  - Map<UUID, PlayerStats> getLastMatchStats()
  - void resetForNewMatch(String arenaId)
- PlayerStats fields:
  - UUID playerUuid
  - int kills
  - int deaths
  - int wins
  - int losses
- Interactions:
  - Called by PlayerDeathListener, CombatManager upon death, GameManager at match end.
- Testing:
  - Simulate a match: record kills/deaths and query `getLastMatchStats()` after match end.

PHASE 4 — Arena data semantics & resources

13) Arena save/load semantics (relative spawn offsets)
- Requirement: When admin saves an arena's spawns, the stored spawn coordinates must be relative offsets from the admin's current position (which is considered the arena center at save time). On load, the stored offsets are applied relative to the player standing at the center position.
- Tasks to implement:
  - In `AddSpawnCommand`, compute relative offset string using `LocationUtils.serializeRelative(adminSpawn, center)` and store via `ArenaManager.addSpawn()`.
  - Implement `Arena.getSpawnLocations(Location center)` to convert relative strings to absolute `Location`s using `LocationUtils.deserializeRelative`.
  - Document clearly: "When using addspawn, the admin MUST stand at the arena center before saving; this tool does not attempt to auto-detect the center."
- Interactions:
  - `LocationUtils`, `ArenaManager`, `AddSpawnCommand`, `GameManager`.

14) `src/main/resources/config.yml` (create/edit)
- Responsibilities: provide a commented example `chest-loot` section and any global defaults (combatTimeout, graceTime defaults), while keeping the file effectively empty for production.
- Create entries (example in comments, not enabling anything by default):
  - combat:
    - combatTimeoutSeconds: 10  # recommended default
    - defaultGraceSeconds: 120
  - chest-loot:
    # starter:
    #   - material: IRON_SWORD
    #     min: 1
    #     max: 1
    #     weight: 50
    #     enchants: {SHARPNESS:1}
    # refill:
    #   - material: ARROW
    #     min: 16
    #     max: 64
    #     weight: 100
- Tasks:
  - Add a commented example block so developers know how to populate it.
  - ChestLootManager must gracefully handle empty/missing sections.

PHASE 5 — GUI skeletons (defer interactive behavior)

15) `src/main/java/io/github/KennedySovine/hungerGames/gui/ArenaEditorGui.java`
- Responsibilities: provide a skeleton GUI to edit arena settings. For now, implement only inventory creation and identification.
- Methods to implement:
  - Inventory buildEditor(Arena arena)
  - boolean isEditorInventory(Inventory inv) // to let the listener know this inventory is managed by us
- Fields:
  - HungerGames plugin
- Interactions:
  - `InventoryGuiListener` to route clicks to editor actions later.
- Testing:
  - Open the GUI (call by test command) and ensure inventory appears and is identified by `isEditorInventory`.

16) `src/main/java/io/github/KennedySovine/hungerGames/gui/SpectatorGui.java`
- Responsibilities: skeleton spectator inventory for teleportation. Implement builder only.
- Methods to implement:
  - Inventory buildSpectatorMenu(List<Player> spectatablePlayers)
- Fields:
  - HungerGames plugin

PHASE 6 — Final cleanup, docs, tests

17) Delete deprecated file
- `src/main/java/io/github/KennedySovine/hungerGames/command/admin/SetLobbyCommand.java` — delete file and remove any references.
- Update `docs/FILES.md` and other docs if they reference `setlobby`.

18) Tests & QA checklist
- Unit tests to add (where possible):
  - `LocationUtils` serialize/deserialize + relative tests.
  - `CombatManager.isInCombat` boundary tests (exact 10s and just over).
  - `ArenaManager` load/save round-trip tests using a temporary `YamlConfiguration`.
- Manual integration tests on dev server (1.21.5+):
  - `/hg arena create test` -> verifies `arenas.yml` is created.
  - `/hg arena addspawn test` while standing at center -> verifies spawn string saved as offset.
  - PvP damage then disconnect -> verify immediate death message and stats increment.
  - Disconnect outside combat -> reconnect within 120s restores, after expiry mark dead.

19) Documentation updates
- Update `docs/COMMANDS.md` to list only the retained commands. Remove any mention of `setlobby`.
- Keep this `docs/TODO.md` updated with checkboxes as you implement files.

Style & Developer Guidance (short)
- Keep methods small and single-responsibility.
- Use dependency injection where helpful (pass plugin/manager references via constructors) to make unit testing easier.
- Add a short "manual test" comment block to the top of each implemented file to speed local QA.

If you want, I can now implement a specific file (for example: `ArenaManager.loadArenas()` and `CreateArenaCommand`), or apply the file deletions for `SetLobbyCommand`. Reply with which you'd like me to implement next and I'll proceed."
