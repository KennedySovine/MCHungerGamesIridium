Development Guide

Coding conventions
- Java 17, follow existing project's style (small classes, descriptive names).
- Javadoc on each public class and method. Inline comments for non-trivial behavior.

Testing
- Unit test pure logic in `src/test/java` using JUnit 5.
- Integration testing on a local dev server (Paper 1.21.5+) to validate Bukkit interactions.

How to run locally
1. Build plugin: `./gradlew build` (Windows: `gradlew.bat build`).
2. Copy `build/libs/HungerGames-1.0-SNAPSHOT.jar` into server `plugins/` and start Paper server.

Persistence
- Data stored under plugin folder:
  - `arenas.yml` — arena definitions with relative spawn offsets
  - `config.yml` — global settings and chest loot tables
  - `stats.yml` — player statistics (match-scoped, non-persistent)

Style rules for files created by this project
- Each class must include a file header Javadoc with a short description and expected interactions.
- Methods performing I/O must document thread expectations (main thread only) and mention the saving strategy.

## Example Workflow: Creating and Editing an Arena

This is the typical workflow for creating a new arena:

```bash
# 1. Create a new arena (automatically loads it into working memory)
/hg arena create testArena "Test Arena"

# 2. Stand at the desired arena center location and set it
/hg setcenter

# 3. Walk to each spawn point location and add them
/hg arena addspawn
# (repeat for each spawn - spawns are stored as offsets from center)

# 4. Configure arena settings (all in-memory at this point)
/hg minplayers 2
/hg maxplayers 24
/hg graceperiod 120
/hg chestrefill 300
/hg time 600
/hg centersize 50

# 5. Optional: Toggle spawn beacons to visualize spawn locations
/hg beacons

# 6. Save all changes to disk
/hg arena save
```

## Example Workflow: Editing an Existing Arena

To edit an existing arena:

```bash
# 1. Load the arena into working memory
/hg arena load testArena

# 2. Make your changes
/hg maxplayers 32
/hg arena addspawn
/hg graceperiod 180

# 3. Save changes back to disk
/hg arena save
```

## Example Workflow: Starting a Match

```bash
# 1. Start the arena
/hg start testArena

# 2. Players join
/hg join testArena

# 3. Game progresses through phases:
#    - LOBBY: Players join and wait
#    - COUNTDOWN: Match starting countdown
#    - RUNNING: Active gameplay
#    - DEATHMATCH: Border shrinks to center
#    - FINISHED: Winner declared

# 4. View stats after match
/hg stats
```

## Working Arena Pattern

The plugin uses a "working arena" system for safe editing:

1. **Master Arenas**: Stored in `arenas.yml`, these are the source of truth
2. **Working Arena**: In-memory copy used for editing
3. **Edit Flow**: 
   - Load arena into working memory (`/hg arena load`)
   - Make changes (all edit commands modify working arena only)
   - Save to persist changes (`/hg arena save`)

**Benefits:**
- No accidental data corruption
- Experiment with changes before committing
- Clear separation between editing and using arenas

## Relative Spawn System

Spawn points are stored as offsets from the arena center:

- **On Save**: When you run `/hg arena addspawn`, your location is converted to an offset from the center
- **Format**: `dx,dy,dz,yaw,pitch` (e.g., "10.5,1.0,-5.5,0.0,0.0")
- **On Load**: When starting a match, spawn locations are calculated as `center + offset`
- **Portability**: Arenas can be relocated by changing only the center point

## Combat Logging System

**Combat Window**: 10 seconds after taking damage
**Grace Period**: 120 seconds for non-combat disconnects (configurable per arena)

**Disconnect Scenarios:**
1. **In Combat**: Player is immediately eliminated
2. **Not in Combat**: Grace period starts; player can reconnect within timeframe
3. **Grace Expired**: Player is eliminated
4. **Reconnect**: Player rejoins the match if within grace period

## Deliverables for initial commands-first milestone ✅

- ✅ ArenaManager with arenas.yml persistence and working-arena pattern
- ✅ GameManager with lifecycle management (start/stop/join/leave) and in-memory game state
- ✅ CombatManager with combat logging and grace period handling
- ✅ StatsManager with match-scoped statistics
- ✅ All command implementations wired to managers

## Next steps after commands-first

- Complete GUI-based arena editor interactive behavior
- Finish chest loot weighted random implementation
- Complete PlayerDeathListener integration
- Add comprehensive unit tests
- Improve performance for large player counts
- Consider SQLite storage for persistent stats (if needed)
