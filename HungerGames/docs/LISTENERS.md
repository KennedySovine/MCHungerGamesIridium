Listeners Reference

Create listener skeletons for the following events and wire them in `HungerGames.onEnable()`:

- **CombatListener** ✅ IMPLEMENTED
  - Listen to EntityDamageByEntityEvent and EntityDamageEvent
  - Pass damage records to CombatManager
  - Status: Basic implementation complete; projectile handling needs completion

- **PlayerConnectionListener** ✅ IMPLEMENTED
  - Listen to PlayerQuitEvent and PlayerJoinEvent
  - Call CombatManager.handleQuit/handleJoin accordingly
  - Status: Fully implemented with combat and grace period handling

- **PlayerDeathListener** ⚠️ PARTIAL
  - Listen to PlayerDeathEvent
  - Call StatsManager to record death and increment killer's kills
  - Set players to spectator mode and drop any match items if required
  - Status: Skeleton created; needs full integration with GameManager/StatsManager

- **InventoryGuiListener** ✅ IMPLEMENTED
  - Listen to InventoryClickEvent
  - Handle clicks originating from ArenaEditorGui or SpectatorGui
  - Status: Framework implemented; interactive behavior deferred

- **SpawnStickListener** ✅ IMPLEMENTED
  - Listen to PlayerInteractEvent
  - If player is holding spawn-stick (metadata), record spawn at player's location via ArenaManager
  - Status: Implemented with PersistentDataContainer integration

Implementation notes
- Keep handlers small; do the heavy lifting in manager methods.
- Remember to register listeners in the plugin main class and to unregister them on disable if necessary.
- All listeners are registered in HungerGames.onEnable() ✅
