Components Reference

For each package, a description of responsibilities and the key classes implemented.

**io.github.KennedySovine.hungerGames** ✅
- **HungerGames** (plugin main class) — Bootstraps all managers, registers commands and listeners, handles plugin lifecycle.

**io.github.KennedySovine.hungerGames.arena** ✅
- **Arena** — Data holder for arena settings, spawn points (as relative offsets), and validation logic.
- **ArenaManager** — Create/load/save arenas, manage working arena pattern, add/remove spawn points (stored relative to center), provide lookup by ID.

**io.github.KennedySovine.hungerGames.game** ✅
- **GameManager** — Manages match lifecycle (LOBBY → COUNTDOWN → RUNNING → DEATHMATCH → FINISHED); handles player join/leave; schedules chest refills and border shrink tasks; integrates with CombatManager for disconnect handling.
- **GameState** — Enum representing game phases: LOBBY, COUNTDOWN, RUNNING, DEATHMATCH, FINISHED.

**io.github.KennedySovine.hungerGames.combat** ✅
- **CombatManager** — Tracks last-damage timestamps, determines combat status (10s window), handles quit/join events, schedules grace period tasks (120s default), delegates death handling to GameManager.

**io.github.KennedySovine.hungerGames.stats** ✅
- **StatsManager** — Tracks kills/deaths/wins/losses per player (match-scoped, non-persistent), provides query API.
- **PlayerStats** — Data model for player statistics.

**io.github.KennedySovine.hungerGames.chest** ⚠️
- **ChestLootManager** — Reads chest loot tables from chest-loot.yml, provides weighted random item selection.

**io.github.KennedySovine.hungerGames.command** ✅
- **HgCommand** — Central command dispatcher for `/hg` command.
- **SubCommand** / **AbstractSubCommand** — Framework for subcommand implementations.
- **Admin Commands** (17 commands) — All arena management and game control commands implemented.
- **Player Commands** (3 commands) — Join, leave, stats commands implemented.

**io.github.KennedySovine.hungerGames.gui** ⚠️
- **ArenaEditorGui** — Inventory-based GUI for editing arena settings (framework implemented; interactive behavior deferred).
- **SpectatorGui** — Teleport UI for spectators (placeholder; needs implementation).
- **ArenaListGui** — Browser for viewing all arenas (implemented).
- **AnvilPrompt** — Text input via anvil GUI (implemented).

**io.github.KennedySovine.hungerGames.storage** ✅
- **YamlStorage** — Helper to safely manage YAML file I/O with atomic writes and thread safety.

**io.github.KennedySovine.hungerGames.utils** ✅
- **LocationUtils** — Serialize/deserialize locations; relative offset calculations for portable spawn points.
- **MessageUtils** — Chat message formatting and color code support.
- **ParticleUtils** — Particle effect generation helpers.

**io.github.KennedySovine.hungerGames.listener** ⚠️✅
- **CombatListener** ⚠️ — Listens for EntityDamageByEntity events, records damage in CombatManager (basic impl; projectile handling TODO).
- **PlayerConnectionListener** ✅ — Handles join/quit events, delegates to CombatManager for combat/grace logic.
- **PlayerDeathListener** ⚠️ — Hooks death events to update stats and set spectator mode (skeleton; needs full integration).
- **InventoryGuiListener** ✅ — Routes inventory click events to appropriate GUI handlers.
- **SpawnStickListener** ✅ — Detects spawn-stick right-clicks to add spawn points via ArenaManager.

**io.github.KennedySovine.hungerGames.particle** ✅
- **ParticleManager** — Manages spawn beacon particle effects for visualization.

**io.github.KennedySovine.hungerGames.border** ✅
- **BorderManager** — Manages world border shrinking for deathmatch phase.

## Implementation Notes

**Persistence:**
- All data persists to YAML files under the plugin data folder
- Working arena pattern ensures safe editing without corruption
- Atomic file writes prevent data loss during saves

**Thread Safety:**
- All Bukkit API calls executed on main server thread
- Asynchronous tasks scheduled via Bukkit scheduler where appropriate
- FileConfiguration I/O performed on main thread for safety

**Component Integration:**
- Managers are loosely coupled through well-defined interfaces
- Commands delegate to managers for business logic
- Listeners delegate to managers for event handling
- GUIs interact with managers for state queries and modifications

**Testing Strategy:**
- Unit tests for pure logic (LocationUtils, validation, parsing)
- Integration tests via manual testing on dev server
- Comprehensive smoke test checklist for each release

## Architecture Highlights

**Working Arena Pattern:**
- Separates editing from persistence
- Allows experimentation without risk
- Clear save semantics via explicit `/hg arena save`

**Relative Spawn System:**
- Spawns stored as offsets from arena center
- Enables arena relocation by changing center only
- Portable arena configurations

**Combat & Grace System:**
- 10-second combat window for logout protection
- 120-second grace period for legitimate disconnects
- Seamless reconnection within grace period
- Automatic cleanup on expiry

**Match Lifecycle:**
- Clear state transitions through GameState enum
- Scheduled tasks for chest refills and border shrinking
- Proper cleanup on match end
- Integration with combat/stats systems
