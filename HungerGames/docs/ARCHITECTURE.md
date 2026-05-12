HungerGames — Architecture Overview

## Purpose
High-level overview of system architecture, component interactions, and design patterns.

## System Architecture

The plugin follows a **manager-based architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────┐
│              HungerGames (Main)                  │
│  - Initializes all managers                     │
│  - Registers commands and listeners             │
│  - Manages plugin lifecycle                     │
└─────────────────┬───────────────────────────────┘
                  │
    ┌─────────────┼─────────────┬─────────────────┐
    │             │             │                 │
┌───▼────┐  ┌────▼─────┐  ┌───▼────┐  ┌────────▼────────┐
│ Arena  │  │   Game   │  │Combat  │  │     Stats       │
│Manager │  │ Manager  │  │Manager │  │    Manager      │
└───┬────┘  └────┬─────┘  └───┬────┘  └────────┬────────┘
    │            │             │                │
    │            │             │                │
┌───▼────────────▼─────────────▼────────────────▼────┐
│                  Commands Layer                     │
│  (Admin: 13 commands, Player: 3 commands)          │
└────────────────────────────────────────────────────┘
```

## Core Modules

### ArenaManager ✅
**Responsibilities:**
- Manages arena definitions, spawn points, and configuration
- Implements **working arena pattern** for safe editing
- Persists arenas to `arenas.yml` with atomic writes
- Stores spawn points as relative offsets from arena center

**Key Features:**
- Master/Working arena separation prevents accidental data corruption
- Relative spawn system enables arena portability
- In-memory editing with explicit save semantics

**API Highlights:**
- `loadWorkingArena(id)` - Copy master → working memory
- `saveWorkingArena()` - Persist working → master + disk
- `addSpawnPointToWorking(location)` - Add spawn as relative offset

### GameManager ✅
**Responsibilities:**
- Manages running matches through state lifecycle
- Handles player join/leave operations
- Schedules timed events (chest refills, border shrink)
- Integrates with CombatManager for disconnect handling

**Game State Flow:**
```
LOBBY → COUNTDOWN → RUNNING → DEATHMATCH → FINISHED
  ↓        ↓          ↓           ↓           ↓
Join    Starting   Active     Border       Winner
Wait    Countdown  Combat     Shrinks      Declared
```

**Key Features:**
- State machine with clear transitions
- Scheduled tasks for match events
- Death handling delegation from CombatManager
- Arena-specific match state tracking

### CombatManager ✅
**Responsibilities:**
- Tracks player damage timestamps for combat status
- Enforces combat-logging rules (10s window)
- Manages disconnect grace periods (120s default)
- Delegates death handling to GameManager

**Combat & Disconnect Logic:**
```
Player Takes Damage
    ↓
Combat Window Active (10s)
    ↓
Player Disconnects?
    ├─ Yes, In Combat → Immediate Elimination
    └─ Yes, Not In Combat → Grace Period (120s)
           ↓
       Reconnects?
           ├─ Yes, Within Grace → Restore to Match
           └─ No, Grace Expired → Eliminate Player
```

**Key Features:**
- Configurable combat window (default 10s)
- Per-arena grace period overrides
- Scheduled task cleanup on reconnect
- Integration with GameManager for death flow

### StatsManager ✅
**Responsibilities:**
- Records player performance statistics
- Provides query API for `/hg stats` command
- Manages match-scoped (non-persistent) data

**Statistics Tracked:**
- Kills (direct eliminations)
- Deaths (times eliminated)
- Wins (match victories)
- Losses (match defeats)

**Note:** Stats reset between matches and are not persisted across server restarts.

### ChestLootManager ⚠️
**Responsibilities:**
- Reads chest loot configurations from `chest-loot.yml`
- Provides weighted random item selection
- Supports multiple loot tiers (starter, refill)

**Status:** Framework implemented; weighted random selection needs completion.

### Storage (YamlStorage) ✅
**Responsibilities:**
- Safe file I/O for all YAML persistence
- Atomic write operations to prevent corruption
- Thread-safe access to FileConfiguration

**Persisted Files:**
- `arenas.yml` - Arena configurations (master data)
- `config.yml` - Global settings
- `chest-loot.yml` - Chest refill loot tables

### Additional Components ✅
- **ParticleManager** - Spawn beacon visualization
- **BorderManager** - World border shrinking for deathmatch
- **LocationUtils** - Location serialization with relative offsets
- **MessageUtils** - Chat formatting utilities

## Data Flow

### Arena Creation & Editing Workflow

```
1. Admin: /hg arena create myarena "My Arena"
   ├─ ArenaManager creates master entry
   └─ Auto-loads into working memory

2. Admin: /hg setcenter (while standing at arena center)
   └─ Sets center reference point for spawn offsets

3. Admin: /hg arena addspawn (at each spawn location)
   └─ Calculates offset from center, stores in working arena

4. Admin: /hg minplayers 2, /hg maxplayers 24, etc.
   └─ All modifications affect working arena only (in-memory)

5. Admin: /hg arena save
   ├─ Replaces master arena with working arena
   └─ Atomically writes to arenas.yml

6. Players can now use the arena:
   /hg start myarena
```

### Match Lifecycle Workflow

```
1. Admin: /hg start myarena
   └─ GameManager: LOBBY state, initializes match data

2. Players: /hg join myarena
   └─ Added to lobby, waiting for minimum players

3. Auto-start when min players reached
   └─ COUNTDOWN → RUNNING transition

4. During match:
   ├─ CombatManager tracks damage events
   ├─ Chests refill on schedule
   └─ Border shrinks after configured time

5. Player elimination:
   ├─ Death → StatsManager updates stats
   ├─ Spectator mode enabled
   └─ Check for winner (last player standing)

6. Match end:
   ├─ Winner declared → FINISHED state
   ├─ Stats available via /hg stats
   └─ Arena cleanup, tasks cancelled
```

### Combat Logging & Grace Period Flow

```
1. Player takes damage
   └─ CombatManager.recordDamage(attacker, victim)

2. Player disconnects
   └─ PlayerConnectionListener → CombatManager.handleQuit()
       ├─ isInCombat() == true?
       │   └─ GameManager.handlePlayerCombatLogout()
       │       └─ Immediate elimination + stats update
       └─ isInCombat() == false?
           └─ Schedule grace period task (120s)
               ├─ Player reconnects? → Cancel task, restore
               └─ Task expires? → GameManager.handlePlayerGraceExpired()
```

## Threading and Persistence

**Thread Safety:**
- All Bukkit API calls execute on the main server thread
- FileConfiguration I/O performed synchronously on main thread
- Scheduled tasks use Bukkit's task scheduler

**Persistence Strategy:**
- Working arena pattern: Changes are in-memory until explicit save
- Atomic writes: Create temp file, write, rename (prevents corruption)
- Debouncing: Multiple rapid edits don't cause multiple disk writes
- Auto-save: On plugin disable, working arena changes are **NOT** saved (explicit save required)

**Data Consistency:**
- Master arenas in `arenas.yml` are source of truth
- Working arena is ephemeral (lost on reload unless saved)
- Stats are volatile (match-scoped only)

## Design Patterns

### 1. Working Arena Pattern
**Problem:** Arena editing risks corrupting live data  
**Solution:** Separate master/working copies with explicit save  
**Benefits:** Safety, experimentation, clear semantics

### 2. Relative Coordinate System
**Problem:** Arenas can't be relocated without updating all spawns  
**Solution:** Store spawns as offsets from arena center  
**Benefits:** Portability, easier arena management

### 3. Manager Delegation
**Problem:** Complex logic in event handlers  
**Solution:** Thin listeners delegate to specialized managers  
**Benefits:** Testability, separation of concerns, maintainability

### 4. State Machine (GameManager)
**Problem:** Unclear match flow and state transitions  
**Solution:** Explicit GameState enum with validated transitions  
**Benefits:** Predictable behavior, easier debugging

### 5. Combat Window + Grace Period
**Problem:** Punish combat loggers but allow legitimate disconnects  
**Solution:** Time-based combat tracking + scheduled grace tasks  
**Benefits:** Fair gameplay, handles edge cases

## Command Architecture

**Dispatcher Pattern:**
- `HgCommand` routes to appropriate subcommand
- `SubCommand` interface defines contract
- `AbstractSubCommand` provides common functionality

**Working Arena Integration:**
- Edit commands modify working arena only
- Explicit save required via `/hg arena save`
- Error messages guide users through workflow

## GUI Architecture (Deferred)

**Current State:**
- Framework implemented (inventory builders, GUI identification)
- Interactive click handlers deferred to future phase
- Spawn-stick system fully functional

**Planned Features:**
- Arena editor with inventory-based property editing
- Spectator teleport menu
- Confirmation dialogs for destructive actions

## Error Handling

**Validation Levels:**
1. **Command Level:** Argument parsing, permission checks
2. **Manager Level:** Business logic validation (min ≤ max, etc.)
3. **Persistence Level:** YAML structure validation

**User Feedback:**
- Clear error messages in chat
- Guidance for fixing issues (e.g., "Load an arena first")
- Success confirmations for all operations

## Testing Strategy

**Unit Tests (Planned):**
- LocationUtils serialization round-trips
- CombatManager combat window boundaries
- Arena validation logic

**Integration Tests (Manual):**
- Full arena creation workflow
- Match lifecycle from start to finish
- Combat logging and grace period scenarios

**Smoke Tests:**
- Basic command functionality
- Arena persistence
- Match state transitions

## Performance Considerations

**Optimizations:**
- In-memory arena editing (no disk I/O until save)
- Scheduled tasks use Bukkit scheduler efficiently
- Combat tracking uses HashMap for O(1) lookups
- Stats are non-persistent (no disk I/O during match)

**Scalability:**
- Designed for 2-24 players per match
- Single active match per arena
- Lightweight scheduled tasks
- Minimal per-player overhead

## Future Considerations

**Potential Enhancements:**
- Kits system (pre-defined loadouts)
- Teams support (duos, squads)
- Persistent stats with SQLite backend
- Multiple concurrent matches
- Advanced loot tier configuration
- Custom event hooks for plugins

## Notes

- **Commands-first approach:** Core functionality before GUI polish
- **No kits or teams in this phase:** Single-player free-for-all only
- **Stats are ephemeral:** Match-scoped, not saved to disk
- **Working arena required:** Most admin commands need loaded working arena
