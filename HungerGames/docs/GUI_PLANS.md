GUI Plans

## Overview

This document describes the inventory-based GUI system for arena editing and spectator management. The framework is implemented; interactive behavior is deferred to future phases.

## Implementation Status

- ✅ **Framework Complete:** Inventory builders, GUI identification
- ⚠️ **Interactive Behavior:** Click handlers deferred
- ✅ **Spawn-Stick System:** Fully functional
- ✅ **Arena List GUI:** Implemented and working

## Arena Editor GUI ⚠️

### Access
- Command: `/hg gui` (admin only)
- Alternative: `/hg arena edit <arenaName>` (planned)
- Requires: Working arena loaded

### Layout (Planned)

**Inventory Title:** "Arena Editor: {arenaName}"

**Main Menu Slots:**
```
┌─────────────────────────────────────────┐
│ [Name] [Min]  [Max]  [Time] [Center]   │
│ [Grace][Refill][Lobby][Spawns][Beacons]│
│                                         │
│        [Save]         [Cancel]          │
└─────────────────────────────────────────┘
```

**Slot Descriptions:**

| Slot | Item | Action | Status |
|------|------|--------|--------|
| Arena Name | NAME_TAG | Click to rename (anvil prompt) | ⚠️ Framework only |
| Min Players | PLAYER_HEAD (green) | Left: +1, Right: -1, Shift: ±5 | ⚠️ Framework only |
| Max Players | PLAYER_HEAD (red) | Left: +1, Right: -1, Shift: ±5 | ⚠️ Framework only |
| Time to Shrink | CLOCK | Click to set (anvil prompt) | ⚠️ Framework only |
| Center Size | BARRIER | Click to set (anvil prompt) | ⚠️ Framework only |
| Grace Period | ENDER_PEARL | Click to set (anvil prompt) | ⚠️ Framework only |
| Chest Refill | CHEST | Click to set (anvil prompt) | ⚠️ Framework only |
| Set Lobby | BED | Click to set lobby at player location | ⚠️ Framework only |
| Spawn Points | SPAWNER | Open spawn management submenu | ⚠️ Framework only |
| Beacons | BEACON | Toggle spawn beacons on/off | ✅ Command works |
| Save | GREEN_WOOL | Save changes (runs /hg arena save) | ⚠️ Framework only |
| Cancel | RED_WOOL | Close GUI without saving | ⚠️ Framework only |

### Item Metadata

**Display Names:**
- Color-coded (GREEN for enabled, RED for disabled)
- Clear action descriptions

**Lore Lines:**
- Current value (e.g., "Current: 24 players")
- Instructions (e.g., "Left-click: +1")
- Requirements (e.g., "Requires: Working arena loaded")

**Visual Feedback:**
- Enchantment glow for active items
- Item count for numeric values where sensible
- Custom model data for visual polish (future)

### Submenu: Spawn Management ⚠️

**Access:** Click "Spawn Points" in main GUI

**Layout:**
```
┌─────────────────────────────────────────┐
│ [Add] [List] [Remove] [Clear] [Import] │
│                                         │
│         Current Spawns: 12              │
│                                         │
│              [Back]                     │
└─────────────────────────────────────────┘
```

**Actions:**
- **Add Spawn:** Give player spawn-stick item
- **List Spawns:** Show paginated list of all spawns
- **Remove Spawn:** Click spawn in list to remove
- **Clear All:** Confirmation → remove all spawns
- **Import:** Copy spawns from another arena (future)

## Spawn-Stick System ✅

### Overview

The spawn-stick is a special item used to place spawn points by right-clicking at desired locations.

### Implementation Status
- ✅ **Fully Implemented and Functional**

### Item Properties

**Item Type:** `STICK`  
**Display Name:** `§6Arena Spawn Setter`  
**Lore:**
```
§7Right-click to add a spawn point
§7Arena: §e{arenaName}
§7Current spawns: §a{count}
```

**Metadata:**
- Uses `PersistentDataContainer`
- Key: `hungergames:spawnstick`
- Value: Arena ID (String)

### Usage Flow

```
Player receives spawn-stick
    ↓
Player walks to desired spawn location
    ↓
Player right-clicks (PlayerInteractEvent)
    ↓
SpawnStickListener detects spawn-stick
    ↓
Validates working arena loaded
    ↓
Calculates relative offset from arena center
    ↓
ArenaManager.addSpawnPointToWorking(location)
    ↓
Feedback: particles + success message
```

### Visual Feedback

**On Success:**
- Particle effect at spawn location (VILLAGER_HAPPY)
- Success message: "§aSpawn point added! (Total: {count})"
- Sound effect (optional)

**On Error:**
- Error message with reason
- No particle effect
- Different sound (optional)

### Edge Cases Handled

- ✅ No working arena loaded → Error message
- ✅ Arena center not set → Error message
- ✅ Player in wrong world → Error message
- ✅ Duplicate location → Allowed (intentional)
- ✅ Stick dropped/despawned → Safe (metadata preserved)

## Spectator GUI ⚠️

### Purpose

Allow spectators to teleport to alive players during a match.

### Access
- Automatic on death (inventory given)
- Command: `/hg spectate` (planned)
- Item in spectator inventory: COMPASS

### Layout (Planned)

**Inventory Title:** "Spectate Players - {arenaName}"

**Slot Layout:**
```
┌─────────────────────────────────────────┐
│ [P1] [P2] [P3] [P4] [P5] [P6] [P7]     │
│ [P8] [P9] [P10][P11][P12][P13][P14]    │
│ [P15][P16][P17][P18][P19][P20][P21]    │
│                                         │
│         [Leave] [Refresh]               │
└─────────────────────────────────────────┘
```

**Player Head Items:**
- Display Name: Player's name with color
- Lore: 
  - Health: ❤❤❤ (hearts)
  - Kills: {count}
  - Location: {x}, {y}, {z}
- Click: Teleport to player

**Special Actions:**
- Leave: Exit match and return to lobby
- Refresh: Update player list (auto-refreshes every 5s)

### Features

**Dynamic Updates:**
- Inventory refreshes as players die
- Dead players removed from list
- Health bars update in real-time (future)

**Sorting Options:**
- By health (lowest first)
- By kills (highest first)
- By name (alphabetical)
- By distance (closest first)

**Click Actions:**
- Left-click: Teleport to player
- Right-click: Lock camera to player (future)
- Shift-click: Add to favorites (future)

## Arena List GUI ✅

### Implementation Status
- ✅ **Fully Implemented**

### Purpose
Browse all available arenas and load them into working memory.

### Layout

**Inventory Title:** "HG Arenas"

**Per Arena:**
- Item: MAP or PAPER
- Display Name: Arena display name
- Lore:
  - ID: {id}
  - Players: {min}-{max}
  - Spawns: {count}
  - Status: {AVAILABLE|IN_USE}
- Click: Load into working arena

### Features
- ✅ Pagination support (if >27 arenas)
- ✅ Color-coded status indicators
- ✅ Click to load arena
- ✅ Visual feedback for loaded arena

## Anvil Prompt GUI ✅

### Implementation Status
- ✅ **Fully Implemented**

### Purpose
Text input for numeric values and names (arena renaming, value setting).

### Usage

```java
AnvilPrompt.prompt(player, "Enter value:", (input) -> {
    // Process input
    int value = Integer.parseInt(input);
    workingArena.setMinPlayers(value);
    player.sendMessage("§aMin players set to " + value);
});
```

### Features
- ✅ Default text displayed
- ✅ Callback on completion
- ✅ Validation hooks
- ✅ Cancel handling

## Event Handling

### InventoryGuiListener ✅

**Purpose:** Route inventory clicks to appropriate GUI handlers

**Flow:**
```
Player clicks in inventory
    ↓
InventoryClickEvent fires
    ↓
Check: Is this a managed GUI?
    ├─ Yes → Identify GUI type
    │   ├─ ArenaEditorGui?
    │   ├─ SpectatorGui?
    │   └─ ArenaListGui?
    ├─ Cancel event (prevent item movement)
    └─ Execute button action
```

**Managed Inventories:**
- ✅ Arena Editor GUI (framework)
- ✅ Arena List GUI (implemented)
- ⚠️ Spectator GUI (framework)
- ✅ Anvil Prompt (implemented)

**Click Types Handled:**
- Left-click: Primary action
- Right-click: Secondary action
- Shift-click: Modified action
- All cancelled to prevent item movement

## Future Enhancements

### Drag-and-Drop Spawn Editing
- Visual map of arena
- Drag spawn markers to reposition
- Requires custom inventory rendering

### Live Preview
- Show arena bounds in-world
- Highlight spawn locations with beacons
- Display border shrink visualization

### Templates
- Save arena as template
- Load template to new location
- Share templates between servers

### Multi-Page Editors
- Separate pages for different settings
- Tabs for organization
- Breadcrumb navigation

### Confirmation Dialogs
- Delete spawn: "Are you sure?"
- Clear all: "This will remove {count} spawns"
- Delete arena: "This cannot be undone"

### Help System
- In-GUI tooltips
- Tutorial mode for first-time users
- Video links in lore (future)

## Design Guidelines

### Visual Consistency
- Use standard Minecraft items where possible
- Color-code for status (green=good, red=bad, yellow=warning)
- Keep layouts simple and intuitive

### Accessibility
- Clear item names and descriptions
- Keyboard shortcuts (future)
- Sound feedback for actions

### Performance
- Minimize inventory refreshes
- Cache GUI states
- Lazy-load dynamic content

### User Experience
- Immediate feedback on all actions
- Undo/redo for destructive operations (future)
- Save progress automatically (working arena pattern)

## Implementation Priorities

**Phase 1 (Current):** ✅
- ✅ Framework: inventory builders, GUI identification
- ✅ Spawn-stick system
- ✅ Arena list GUI
- ✅ Anvil prompt

**Phase 2 (Next):**
- ⚠️ Arena editor click handlers
- ⚠️ Numeric value editing with increment/decrement
- ⚠️ Confirmation dialogs

**Phase 3 (Future):**
- ⚠️ Spectator GUI full implementation
- Spawn management submenu
- Live preview features

**Phase 4 (Advanced):**
- Templates system
- Multi-page editors
- Advanced visualizations

## Testing GUI

### Manual Test Checklist

**Arena Editor:**
- [ ] GUI opens with `/hg gui`
- [ ] All items display correctly
- [ ] Click handlers work (once implemented)
- [ ] Save button persists changes
- [ ] Cancel button discards changes

**Spawn-Stick:**
- [x] Stick received with correct metadata
- [x] Right-click adds spawn at location
- [x] Feedback messages display
- [x] Particles appear at spawn location
- [x] Spawns stored as relative offsets

**Arena List:**
- [x] Lists all arenas
- [x] Click loads arena
- [x] Status indicators accurate
- [x] Pagination works (if needed)

**Spectator GUI:**
- [ ] Opens on death
- [ ] Shows alive players
- [ ] Teleport works
- [ ] Updates when players die

## Notes

- GUIs must cancel `InventoryClickEvent` to prevent item movement
- Use item meta and lore for visual affordances
- Provide confirmation for destructive operations
- All GUI interactions logged for debugging
- Framework is complete; interactive behavior deferred for focused development after core systems
