Managers Overview

This document lists the manager classes required for the plugin and the responsibilities for each. Create the classes under `io.github.KennedySovine.hungerGames.<module>` as noted.

ArenaManager (io.github.KennedySovine.hungerGames.arena)
- Responsibilities:
  - Create/delete arenas
  - Load/save arenas from/to `arenas.yml`
  - Add/remove spawn points
  - Provide arena lookup (by id and by display name)
- Expected public API (skeleton):
  - `void loadArenas()`
  - `void saveArenas()`
  - `Arena createArena(String id, String displayName)`
  - `boolean deleteArena(String id)`
  - `Optional<Arena> getArena(String id)`
  - `List<String> getArenaNames()`

GameManager (io.github.KennedySovine.hungerGames.game)
- Responsibilities:
  - Manage game lifecycle (lobby -> countdown -> running -> deathmatch -> finished)
  - Handle player join/leave for a match
  - Schedule chest refills and border shrink tasks
  - Reset arena state and player inventories on end
- Expected public API (skeleton):
  - `void startGame(String arenaId)`
  - `void stopGame(String arenaId)`
  - `void join(Player player, String arenaId)`
  - `void leave(Player player)`
  - `GameState getGameState(String arenaId)`

CombatManager (io.github.KennedySovine.hungerGames.combat)
- Responsibilities:
  - Track last damage timestamps per player
  - Decide if a player is "in combat" (configurable window, default 10s)
  - Handle PlayerQuitEvent and PlayerJoinEvent combat logic (force-kill vs grace)
  - Persist pending disconnect entries to `pending-disconnects.yml` and reschedule on startup
- Expected public API (skeleton):
  - `void recordDamage(UUID attacker, UUID victim)`
  - `boolean isInCombat(UUID player)`
  - `void handleQuit(Player player)`
  - `void handleJoin(Player player)`

StatsManager (io.github.KennedySovine.hungerGames.stats)
- Responsibilities:
  - Track kills, deaths, wins, losses per player
  - Persist stats to `stats.yml`
  - Provide query API for `/hg stats` command
- Expected public API (skeleton):
  - `PlayerStats getStats(UUID player)`
  - `void addKill(UUID player)`
  - `void addDeath(UUID player)`
  - `void addWin(UUID player)`
  - `void save()`

Notes
- Each manager should be instantiated and owned by `HungerGames` main class and exposed via getter methods for commands and listeners to use.
- Managers must document thread expectations: all Bukkit interactions on main thread; FileConfiguration I/O should be done carefully (main thread or scheduled sync tasks).

TODO: Create manager class skeleton files in `src/main/java` matching these APIs with Javadocs and inline comments.
