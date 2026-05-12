Storage Guide

## Overview

This document describes the plugin's data persistence strategy, file formats, and storage best practices.

## Persisted Files

All files are stored under the plugin data folder: `plugins/HungerGames/`

### arenas.yml ✅
**Purpose:** Stores all arena configurations (master data)

**Structure:**
```yaml
arenas:
  arena-id:
    displayName: "Display Name"
    minPlayers: 2
    maxPlayers: 24
    timeToShrinkSeconds: 600
    centerSize: 50
    gracePeriodSeconds: 120
    chestRefillSeconds: 300
    center:
      world: "world"
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
    lobby:
      world: "world"
      x: 10.5
      y: 64.0
      z: 10.5
      yaw: 180.0
      pitch: 0.0
    spawns:
      - "5.5,1.0,-3.0,0.0,0.0"     # Relative offset from center
      - "-4.0,0.5,7.5,90.0,0.0"
      - "12.0,2.0,-8.0,270.0,0.0"
```

**Key Features:**
- Spawns stored as relative offsets (dx,dy,dz,yaw,pitch)
- Center and lobby stored as absolute locations
- All numeric values validated on load
- Atomically written to prevent corruption

**Managed By:** `ArenaManager`

### config.yml ✅
**Purpose:** Global plugin settings

**Structure:**
```yaml
# Combat settings
combat:
  timeoutSeconds: 10        # Combat window duration
  defaultGraceSeconds: 120  # Default grace period

# Reserved for global settings.
```

### chest-loot.yml ✅
**Purpose:** Chest refill loot tables (editable)

**Structure:**
```yaml
chest-loot:
  early:
    min-rolls: 3
    max-rolls: 6
    enchant-chance: 0.10
    items: [ ... ]
  mid:
    min-rolls: 4
    max-rolls: 7
    enchant-chance: 0.20
    items: [ ... ]
  late:
    min-rolls: 5
    max-rolls: 8
    enchant-chance: 0.33
    items: [ ... ]
```

**Key Features:**
- Weighted random item selection
- Multiple editable loot tiers (early, mid, late)
- Enchantment support
- Direct runtime configuration in plugins/HungerGames/chest-loot.yml

**Managed By:** `ChestLootManager`

### stats.yml (Future - Not Currently Persisted)
**Purpose:** Player statistics storage

**Note:** Currently stats are **match-scoped only** and **not persisted** across server restarts. This is intentional to keep matches isolated.

**Potential Structure:**
```yaml
players:
  uuid-string:
    kills: 12
    deaths: 7
    wins: 1
    losses: 4
    lastPlayed: 1670000000000
```

**If Implemented:**
- Would be managed by `StatsManager`
- Could support lifetime statistics
- Might require SQLite for better performance at scale

## Storage Implementation

### YamlStorage Class ✅

**Purpose:** Centralized YAML file I/O with safety guarantees

**Key Methods:**
```java
FileConfiguration load(String filename)
void save(String filename, FileConfiguration config)
File getFile(String filename)
```

**Features:**
1. **Atomic Writes:**
   - Create temporary file
   - Write data to temp
   - Rename temp to target (atomic operation)
   - Prevents corruption on crash/shutdown

2. **Thread Safety:**
   - All I/O performed on main thread
   - Uses Bukkit scheduler for deferred saves
   - No concurrent access issues

3. **Error Handling:**
   - Validates YAML syntax on load
   - Returns empty config if file missing
   - Logs errors for debugging

4. **Default Resources:**
   - Extracts default config.yml from JAR if needed
   - Preserves user modifications

### Working Arena Persistence Pattern

**Problem:** Direct edits to arenas.yml risk corruption

**Solution:** Two-tier persistence model

**Tiers:**
1. **Master Arenas:** Stored in `arenas.yml` (source of truth)
2. **Working Arena:** In-memory copy for editing

**Workflow:**
```
Admin: /hg arena load myarena
    ↓
ArenaManager.loadWorkingArena("myarena")
    ├─ Read master arena from arenas.yml
    ├─ Deep copy into working memory
    └─ Store reference in ArenaManager

Admin: /hg minplayers 4, /hg maxplayers 20, etc.
    ↓
Commands modify working arena in memory
    └─ NO disk writes

Admin: /hg arena save
    ↓
ArenaManager.saveWorkingArena()
    ├─ Validate working arena
    ├─ Replace master arena with working copy
    ├─ Write entire arenas.yml atomically
    └─ Success message
```

**Benefits:**
- No accidental data corruption
- Can experiment with settings before committing
- Clear "save point" semantics
- Rollback possible (just reload from master)

**Caveats:**
- Working arena lost on plugin reload (unless saved)
- Only one working arena at a time
- Admins must remember to save changes

## Data Formats

### Location Serialization

**Absolute Locations** (center, lobby):
```
Format: world,x,y,z,yaw,pitch
Example: "world,10.5,64.0,-5.5,180.0,0.0"
```

**Relative Offsets** (spawns):
```
Format: dx,dy,dz,yaw,pitch
Example: "5.5,1.0,-3.0,90.0,0.0"
```

**Serialization:** `LocationUtils.serialize(Location)`  
**Deserialization:** `LocationUtils.deserialize(String)`  
**Relative:** `LocationUtils.serializeRelative(spawn, center)`

### Timestamps

**Format:** Epoch milliseconds (long)  
**Example:** `1670000000000`  
**Usage:** Combat timestamps, grace period expiry

### Arena IDs

**Format:** Lowercase alphanumeric + hyphens  
**Example:** `my-arena`, `arena-1`, `pvp-map`  
**Validation:** Enforced by Arena class

## Persistence Guidelines

### When to Save

**Arena Changes:**
- Only when admin runs `/hg arena save`
- Not after every edit command
- Not automatically on plugin disable

**Config Changes:**
- Immediately when modified (rare)
- On plugin disable (if config writable)

**Stats:**
- Currently: Never (match-scoped only)
- Future: Periodically or on significant events

### Debouncing Saves

**Problem:** Frequent saves impact performance

**Strategy:**
- Batch edits in working arena (no saves)
- Single save operation after all edits
- Use scheduled task for deferred writes if needed

**Example:**
```java
// BAD: Save after every edit
/hg minplayers 2    → SAVE
/hg maxplayers 20   → SAVE
/hg graceperiod 120 → SAVE

// GOOD: Save once after all edits
/hg minplayers 2    → in-memory only
/hg maxplayers 20   → in-memory only
/hg graceperiod 120 → in-memory only
/hg arena save      → SAVE (once)
```

### Error Handling

**Load Failures:**
- Log error with details
- Return empty configuration
- Continue plugin initialization

**Save Failures:**
- Log error with stack trace
- Notify admin in-game
- Keep working arena in memory (retry possible)

**Corruption Detection:**
- Invalid YAML syntax → log and skip
- Missing required fields → use defaults
- Invalid values → validate and reject

## Thread Safety

### Main Thread Requirement

**All FileConfiguration operations MUST run on main thread:**
```java
// GOOD: Already on main thread
public void saveArenas() {
    storage.save("arenas.yml", config);
}

// GOOD: Schedule for main thread
Bukkit.getScheduler().runTask(plugin, () -> {
    storage.save("arenas.yml", config);
});

// BAD: Async thread
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    storage.save("arenas.yml", config); // WRONG!
});
```

### Concurrent Access

**Protection:**
- Single working arena (no concurrent edits)
- Synchronized access to master arenas map
- Manager methods are effectively single-threaded

## Backup Strategy (Future)

**Planned Features:**
- `/hg backup` command to export data
- Automatic backups before saves
- Configurable backup retention (keep last N)
- Backup folder: `plugins/HungerGames/backups/`

**Backup Format:**
```
backups/
  ├─ arenas-2024-01-15-14-30-00.yml
  ├─ arenas-2024-01-15-12-00-00.yml
  └─ arenas-2024-01-14-18-45-00.yml
```

## Migration Strategy

**Future Version Changes:**

If arena format changes in future versions:
1. Detect old format on load
2. Migrate data to new format
3. Save backup of old format
4. Write new format to disk
5. Log migration success

**Backwards Compatibility:**
- New fields added with sensible defaults
- Old fields deprecated but still read
- Migration path documented in changelog

## Security Considerations

**Data Safety:**
- No sensitive data stored (all public game config)
- File permissions inherited from server
- No encryption needed

**Validation:**
- All loaded data validated before use
- Reject invalid values (prevent exploits)
- Sanitize user input in arena names

**Access Control:**
- Only admins can modify arenas
- File access limited to plugin directory
- No remote/network access

## Performance Considerations

**Optimization:**
- In-memory working arena (fast edits)
- Atomic writes minimize I/O time
- Lazy loading (load on demand, not startup)
- No disk I/O during matches

**Scalability:**
- YAML suitable for <100 arenas
- For larger scale, consider SQLite
- Current design handles typical use cases

## Troubleshooting

**Common Issues:**

1. **"Arena not found" after save**
   - Ensure arena ID matches exactly
   - Check arenas.yml syntax
   - Verify file permissions

2. **"Working arena not loaded"**
   - Run `/hg arena load <id>` first
   - Check if arena exists in arenas.yml

3. **Changes lost on restart**
   - Working arena not saved
   - Always run `/hg arena save` before reload

4. **Corrupted arenas.yml**
   - Check YAML syntax (indentation!)
   - Restore from backup
   - Validate with online YAML validator

## Best Practices

**For Developers:**
1. Always use YamlStorage for file I/O
2. Validate data before saving
3. Handle load failures gracefully
4. Document data format changes
5. Test save/load round-trips

**For Server Admins:**
1. Backup arenas.yml regularly
2. Use `/hg arena save` after edits
3. Test config changes on dev server first
4. Monitor server logs for I/O errors
5. Keep backups before major updates
