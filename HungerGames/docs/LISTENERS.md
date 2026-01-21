Listeners Reference

Create listener skeletons for the following events and wire them in `HungerGames.onEnable()`:

- CombatListener
  - Listen to EntityDamageByEntityEvent and EntityDamageEvent
  - Pass damage records to CombatManager

- PlayerConnectionListener
  - Listen to PlayerQuitEvent and PlayerJoinEvent
  - Call CombatManager.handleQuit/handleJoin accordingly

- PlayerDeathListener
  - Listen to PlayerDeathEvent
  - Call StatsManager to record death and increment killer's kills
  - Set players to spectator mode and drop any match items if required

- InventoryGuiListener
  - Listen to InventoryClickEvent
  - Handle clicks originating from ArenaEditorGui or SpectatorGui

- SpawnStickListener
  - Listen to PlayerInteractEvent
  - If player is holding spawn-stick (metadata), record spawn at player's location via ArenaManager

Implementation notes
- Keep handlers small; do the heavy lifting in manager methods.
- Remember to register listeners in the plugin main class and to unregister them on disable if necessary.
