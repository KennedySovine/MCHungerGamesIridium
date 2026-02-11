Managers Overview

This document lists the manager classes required for the plugin and the responsibilities for each. All managers are implemented under `io.github.KennedySovine.hungerGames.<module>` as noted.

**ArenaManager** (io.github.KennedySovine.hungerGames.arena) ✅ IMPLEMENTED
- Responsibilities:
  - Create/delete arenas
  - Load/save arenas from/to `arenas.yml`
  - Add/remove spawn points (stored as relative offsets from arena center)
  - Provide arena lookup (by id and by display name)
  - Support a working (in-memory) arena placeholder used for admin edits (loadWorkingArena, saveWorkingArena)
- Implemented public API:
  - `void loadArenas()`
  - `void saveArenas()`
  - `Arena createArena(String id, CommandSender creator)` - Creates and returns new arena
  - `boolean deleteArena(String id)`
  - `Optional<Arena> getArena(String id)`
  - `List<String> getArenaIds()` - Returns list of arena IDs
  - `boolean loadWorkingArena(String id)` - Copy master → working
  - `Optional<Arena> getWorkingArena()`
  - `void saveWorkingArena()` - Copy working → master and persist
  - `void addSpawnPointToWorking(Location location)` - Add spawn to working arena
  - `void removeSpawnPointFromWorking(int index)` - Remove spawn from working arena
  - `void setWorkingArenaCenter(Location center)` - Set center of working arena

**GameManager** (io.github.KennedySovine.hungerGames.game) ✅ IMPLEMENTED
- Responsibilities:
  - Manage game lifecycle (lobby → countdown → running → deathmatch → finished)
  - Handle player join/leave for a match
  - Schedule chest refills and border shrink tasks
  - Reset arena state and player inventories on end
  - Integration with CombatManager for disconnect/reconnect handling
- Implemented public API:
  - `void startGame(String arenaId)`
  - `void stopGame(String arenaId)`
  - `void join(Player player, String arenaId)`
  - `void leave(Player player)`
  - `GameState getGameState(String arenaId)`
  - `void handlePlayerCombatLogout(Player player)` - Called by CombatManager
  - `void handlePlayerGraceExpired(Player player)` - Called by CombatManager
  - `void handlePlayerReconnected(Player player)` - Called by CombatManager

**CombatManager** (io.github.KennedySovine.hungerGames.combat) ✅ IMPLEMENTED
- Responsibilities:
  - Track last damage timestamps per player
  - Decide if a player is "in combat" (configurable window, default 10s)
  - Handle PlayerQuitEvent and PlayerJoinEvent combat logic (force-kill vs grace)
  - Schedule grace period tasks for non-combat disconnects (default 120s)
  - Delegate to GameManager for death handling
- Implemented public API:
  - `void recordDamage(UUID attacker, UUID victim)`
  - `boolean isInCombat(UUID player)`
  - `void handleQuit(Player player)`
  - `void handleJoin(Player player)`
  - `void onGameStart(String arenaId)` - Initialize combat tracking for arena
  - `void onGameEnd(String arenaId)` - Clean up combat data for arena

**StatsManager** (io.github.KennedySovine.hungerGames.stats) ✅ IMPLEMENTED
- Responsibilities:
  - Track kills, deaths, wins, losses per player (match-scoped, non-persistent)
  - Provide query API for `/hg stats` command
  - Reset stats for new matches
- Implemented public API:
  - `PlayerStats getStats(UUID player)` - Get or create stats for player
  - `void addKill(UUID player)`
  - `void addDeath(UUID player)`
  - `void addWin(UUID player)`
  - `void addLoss(UUID player)`
  - `void recordDeath(UUID victim, @Nullable UUID killer)` - Record death and optional kill
  - `void resetStats()` - Clear all stats for new match
  - `Map<UUID, PlayerStats> getAllStats()` - Get all current match stats

**ChestLootManager** (io.github.KennedySovine.hungerGames.chest) ⚠️ PARTIAL
- Responsibilities:
  - Read `config.yml` chest-loot mapping and provide a method to populate a chest `Inventory`
  - Weighted random selection of items
  - Support multiple tiers (starter, refill)
- Implemented public API:
  - `void loadLootTables()` - Load from config.yml
  - `void populateChest(Inventory inventory, String tier)` - Populate with weighted random items
- Status: Framework created; weighted random selection needs completion

**Additional Managers** (Beyond original plan):
- **ParticleManager** ✅ - Manages particle effects for spawn beacons and visual feedback
- **BorderManager** ✅ - Manages world border shrinking for deathmatch phase

Notes
- Each manager is instantiated and owned by `HungerGames` main class and exposed via getter methods for commands and listeners to use.
- Managers document thread expectations: all Bukkit interactions on main thread; FileConfiguration I/O is done carefully (main thread or scheduled sync tasks).
- Working arena pattern is fully implemented: edits are in-memory until explicitly saved with `/hg arena save`.
