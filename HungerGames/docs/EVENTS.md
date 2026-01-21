Event Hooks & Listeners

List of Minecraft events to listen and why

- EntityDamageByEntityEvent / EntityDamageEvent
  - Track player-vs-player damage timestamps for CombatManager.

- PlayerQuitEvent
  - Detect disconnects and either force-kill (in combat) or schedule grace expiration.

- PlayerJoinEvent
  - Cancel pending kill tasks, restore player state if reconnection during grace.

- PlayerDeathEvent
  - Record deaths/kills in StatsManager, set spectator mode for dead players.

- InventoryClickEvent
  - Prevent moving items in GUI inventories; handle GUI button clicks.

- PlayerInteractEvent
  - Detect spawn-stick right-clicks to add spawn points.

- BlockBreakEvent / BlockPlaceEvent (optional)
  - Restrict building in arenas; consider toggling build perms during match.

Implementation notes
- All Listener methods must be registered in HungerGames.onEnable().
- Avoid heavy logic in event handlers; delegate to manager classes.
- Ensure all Bukkit API calls are on the main thread.
