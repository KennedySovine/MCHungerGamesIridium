Event Hooks & Listeners

## Overview

This document describes all Minecraft event hooks used by the plugin and their purposes. All listeners are registered in `HungerGames.onEnable()` and properly handle the main thread requirement.

## Implemented Event Handlers

### CombatListener ⚠️ PARTIAL

**Events Listened:**
- `EntityDamageByEntityEvent` ✅
- `EntityDamageEvent` (for projectiles) ⚠️

**Purpose:**
- Track player-vs-player damage timestamps for combat window calculations
- Identify attacker and victim UUIDs
- Handle both direct damage and projectile damage

**Implementation Status:**
- ✅ Basic player-to-player damage tracking implemented
- ⚠️ Projectile damage handling (arrows, tridents) needs completion
- ✅ Delegates to `CombatManager.recordDamage(attacker, victim)`

**Flow:**
```
Player attacks Player
    ↓
EntityDamageByEntityEvent fires
    ↓
CombatListener extracts UUIDs
    ↓
CombatManager.recordDamage(attacker, victim)
    ↓
Victim marked as "in combat" for 10 seconds
```

**Edge Cases Handled:**
- Non-player entities (ignored)
- Cancelled events (respected)
- Self-damage (ignored)
- Damage in non-HG worlds (ignored if arena-specific)

**TODO:**
- Complete projectile shooter resolution
- Handle arrow metadata for attacker tracking
- Support indirect damage sources (TNT, fire, etc.)

### PlayerConnectionListener ✅ IMPLEMENTED

**Events Listened:**
- `PlayerQuitEvent` ✅
- `PlayerJoinEvent` ✅

**Purpose:**
- Detect player disconnects during matches
- Handle combat logging (immediate elimination)
- Manage grace periods for non-combat disconnects
- Restore players who reconnect within grace period

**Implementation Status:**
- ✅ Fully implemented with combat and grace logic
- ✅ Delegates to `CombatManager.handleQuit(player)` and `CombatManager.handleJoin(player)`

**Quit Flow:**
```
Player quits
    ↓
PlayerQuitEvent fires
    ↓
PlayerConnectionListener.onPlayerQuit()
    ↓
CombatManager.handleQuit(player)
    ├─ isInCombat() == true?
    │   └─ GameManager.handlePlayerCombatLogout(player)
    │       └─ Immediate elimination
    └─ isInCombat() == false?
        └─ Schedule grace period task (120s default)
            └─ Store pending disconnect data
```

**Join Flow:**
```
Player joins
    ↓
PlayerJoinEvent fires
    ↓
PlayerConnectionListener.onPlayerJoin()
    ↓
CombatManager.handleJoin(player)
    └─ Has pending disconnect?
        ├─ Yes → Cancel grace task
        │       → GameManager.handlePlayerReconnected(player)
        │       → Restore to match
        └─ No → Normal join flow
```

**Edge Cases Handled:**
- Player not in match (ignored)
- Grace period expiry during reconnect attempt
- Multiple rapid disconnect/reconnect cycles
- Server shutdown during grace period

### PlayerDeathListener ⚠️ SKELETON

**Events Listened:**
- `PlayerDeathEvent` ⚠️

**Purpose:**
- Hook into death event to update match state
- Record kills and deaths in StatsManager
- Move dead players to spectator mode
- Check for match winner (last player standing)

**Implementation Status:**
- ⚠️ Skeleton created; needs full integration with GameManager/StatsManager

**Planned Flow:**
```
Player dies
    ↓
PlayerDeathEvent fires
    ↓
PlayerDeathListener.onPlayerDeath()
    ├─ Extract killer (if PvP death)
    ├─ StatsManager.recordDeath(victim, killer)
    ├─ GameManager.handlePlayerDeath(player)
    │   ├─ Remove from active players
    │   ├─ Set to spectator mode
    │   ├─ Teleport to spectator area
    │   └─ Clear inventory
    └─ Check for winner
        └─ Only 1 player left? → End match
```

**Edge Cases to Handle:**
- Environmental deaths (no killer)
- Simultaneous deaths
- Death during countdown phase
- Death as spectator (ignore)

**TODO:**
- Complete integration with GameManager.handlePlayerDeath()
- Implement spectator mode transition
- Add winner detection logic
- Handle death messages customization

### InventoryGuiListener ✅ IMPLEMENTED

**Events Listened:**
- `InventoryClickEvent` ✅

**Purpose:**
- Route inventory clicks to appropriate GUI handlers
- Prevent item movement in GUI inventories
- Handle button clicks for arena editor and spectator menu

**Implementation Status:**
- ✅ Framework implemented with GUI identification
- ⚠️ Interactive behavior deferred to future phase

**Flow:**
```
Player clicks in inventory
    ↓
InventoryClickEvent fires
    ↓
InventoryGuiListener.onInventoryClick()
    ├─ Is this a managed GUI?
    │   ├─ ArenaEditorGui? → Route to editor handler
    │   ├─ SpectatorGui? → Route to spectator handler
    │   └─ ArenaListGui? → Route to list handler
    ├─ Cancel event (prevent item movement)
    └─ Execute button action
```

**Managed GUIs:**
- ArenaEditorGui (framework only)
- SpectatorGui (framework only)
- ArenaListGui (implemented)
- AnvilPrompt (implemented)

**Edge Cases Handled:**
- Non-GUI inventory clicks (pass through)
- Shift-clicking attempts (cancelled)
- Outside-inventory clicks (allowed)

### SpawnStickListener ✅ IMPLEMENTED

**Events Listened:**
- `PlayerInteractEvent` ✅

**Purpose:**
- Detect spawn-stick right-clicks
- Add spawn points at player location
- Provide visual/audio feedback

**Implementation Status:**
- ✅ Fully implemented with PersistentDataContainer integration

**Flow:**
```
Player right-clicks with item
    ↓
PlayerInteractEvent fires
    ↓
SpawnStickListener.onPlayerInteract()
    ├─ Is item a spawn-stick?
    │   └─ Check PersistentDataContainer for key
    ├─ Extract arena ID from stick metadata
    ├─ ArenaManager.addSpawnPointToWorking(location)
    ├─ Calculate relative offset from center
    ├─ Add to working arena spawn list
    └─ Provide feedback (message + particles)
```

**Spawn-Stick Format:**
- Item: `STICK`
- Metadata: PersistentDataContainer key `hungergames:spawnstick`
- Value: Arena ID string
- Given via GUI or admin command

**Edge Cases Handled:**
- No working arena loaded (error message)
- Player not standing in valid location
- Duplicate spawn at same location (allowed)
- Arena center not set (error message)

## Optional/Future Event Handlers

### BlockBreakEvent / BlockPlaceEvent (Not Implemented)
**Purpose:**
- Restrict building/breaking in arenas during matches
- Could toggle build permissions based on match phase

**Consideration:**
- May want unrestricted building for arena setup
- Consider per-arena configuration

### ChestOpenEvent (Not Implemented)
**Purpose:**
- Trigger chest loot population on first open
- Track chest states for refill scheduling

**Status:**
- Currently using scheduled refills instead
- On-demand population deferred

### ProjectileLaunchEvent (Not Implemented)
**Purpose:**
- Track projectile metadata for damage attribution
- Handle special projectile types

**Status:**
- Deferred; basic projectile handling in CombatListener TODO

## Event Handler Best Practices

**Implementation Guidelines:**
1. **Keep handlers thin** - Delegate logic to managers
2. **Respect cancelled events** - Check `event.isCancelled()` where appropriate
3. **Main thread only** - No async operations in event handlers
4. **Null safety** - Check for null entities, locations, etc.
5. **Arena context** - Verify player is in an HG match before processing

**Thread Safety:**
- All events fire on main server thread
- No synchronization needed for event handlers
- Manager methods called from handlers must be thread-safe

**Performance Considerations:**
- Minimize work in high-frequency events (damage, move)
- Use early returns for non-HG events
- Cache manager references (don't look up each event)

## Registration

All listeners are registered in `HungerGames.onEnable()`:

```java
@Override
public void onEnable() {
    // ... initialize managers ...
    
    // Register listeners
    getServer().getPluginManager().registerEvents(new CombatListener(combatManager), this);
    getServer().getPluginManager().registerEvents(new PlayerConnectionListener(combatManager, gameManager), this);
    getServer().getPluginManager().registerEvents(new PlayerDeathListener(gameManager, statsManager), this);
    getServer().getPluginManager().registerEvents(new InventoryGuiListener(), this);
    getServer().getPluginManager().registerEvents(new SpawnStickListener(arenaManager), this);
}
```

**Unregistration:**
- Bukkit automatically unregisters on plugin disable
- No explicit cleanup needed
- Scheduled tasks are cancelled separately

## Testing Event Handlers

**Manual Testing Checklist:**
- [ ] PvP damage updates combat status
- [ ] Combat logout eliminates player immediately
- [ ] Non-combat disconnect triggers grace period
- [ ] Reconnect within grace restores player
- [ ] Grace expiry eliminates player
- [ ] Death updates stats correctly
- [ ] Spectator mode activated on death
- [ ] Spawn-stick adds spawns correctly
- [ ] GUI clicks route to correct handlers

**Edge Cases to Test:**
- Rapid disconnect/reconnect
- Death during grace period
- Simultaneous player deaths
- Invalid spawn-stick usage
- GUI interactions while inventory full
