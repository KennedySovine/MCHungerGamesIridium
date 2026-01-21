I’ll produce a clear, implementable plan for both the combat-logging system and the admin inventory GUI arena editor; below is the draft plan for your review.

## Plan: Combat-logging & Arena Admin GUI

TL;DR — Add a `CombatManager` to track last-damage times and pending disconnects (10s combat window, 120s grace). Add `ArenaManager` + `ArenaEditorGui` using Bukkit inventory GUI; admins edit arena settings and place spawns with a “spawn-stick”. Persist arenas and pending disconnects to YAML. Use event listeners for damage, quit/join, GUI clicks, and stick interactions. Cancel scheduled kills on reconnect.

### Steps
1. Design class/package layout and data shapes (see classes & YAML).  
2. Implement `CombatManager` and listener handlers for damage, quit, join.  
3. Implement `ArenaManager` + persistent YAML storage and scheduled cleanup.  
4. Build `ArenaEditorGui` with inventory buttons and stick-based spawn setter.  
5. Add commands (`/arena edit`, `/arena create`) and permission `hungergames.admin`.  
6. Add tests/manual QA steps and deployment checklist.

### Further Considerations
1. Persist pending disconnects to disk to survive crashes; Option A: timestamped entry / Option B: immediate-kill on restart.  
2. Use per-player scheduled task IDs to cancel on reconnect; prefer central sweep if player counts high.  
3. Clarify cross-server behavior (Bungee/Velocity): recommend central server authority or mark as “unsupported” for now.

-------------------------
Detailed implementable plan (for developer)

1) Package & class layout (responsibilities)
- `io.github.KennedySovine.hungerGames.combat`
  - `CombatManager`
    - Responsibilities: track last-damage timestamps, record pending disconnects, schedule/cancel kill tasks, persist pending disconnects.
    - Key methods:
      - `void recordDamage(UUID attacker, UUID victim, long timestamp)`
      - `void setLastDamage(UUID player, long timestamp)`
      - `boolean isInCombat(UUID player)` (uses configurable `combatWindowMs`)
      - `void handleQuit(Player player)` (called from listener)
      - `void handleJoin(Player player)` (called from listener)
      - `void scheduleGraceKill(UUID player, long delayMs)` -> stores `BukkitTask` id for cancellation
      - `void forceKill(UUID player, String reason)`
      - `void loadPendingDisconnects()` / `void savePendingDisconnects()`
    - Internal state:
      - `ConcurrentHashMap<UUID, Long> lastDamage` (ms timestamp)
      - `ConcurrentHashMap<UUID, BukkitTask> pendingKillTasks`
      - `ConcurrentHashMap<UUID, PendingDisconnectEntry>` persisted to YAML
      - Config values: `combatWindowMs` (default 10_000), `graceMs` (default 120_000)
- `io.github.KennedySovine.hungerGames.arena`
  - `ArenaManager`
    - Responsibilities: create/edit arenas, manage in-memory arena state, persist arenas to `arenas.yml`
    - Key methods:
      - `Arena createArena(String id, String mapName)` / `void deleteArena(String id)`
      - `void addSpawnPoint(String arenaId, Location loc)` / `void removeSpawnPoint(...)`
      - `Arena getArena(String id)` and `List<Arena>`
      - `void loadArenas()` / `void saveArenas()`
    - Data model: `Arena` object fields (see YAML schema section)
  - `Arena`
    - Fields: `id`, `mapName`, `minPlayers`, `maxPlayers`, `timeToShrink`, `centerSize`, `gracePeriodSecs`, `chestRefillSecs`, `lobbyLocation`, `List<Location> spawnPoints`
- `io.github.KennedySovine.hungerGames.gui`
  - `ArenaEditorGui`
    - Responsibilities: show inventory GUI, handle clicks, open sub-menus for editing values, provide "Set Player Spawn Points" button giving the admin the spawn-stick.
    - Key methods:
      - `Inventory createMainGui(Arena arena)` / `void openFor(Player admin, Arena arena)`
      - `void handleInventoryClick(InventoryClickEvent e)`
      - `void giveSpawnStick(Player admin, String arenaId)`
- `io.github.KennedySovine.hungerGames.command`
  - `ArenaCommand` (register `/arena create|edit|delete|list` etc.)
- `io.github.KennedySovine.hungerGames.listener`
  - `CombatListener` (damage events)
  - `PlayerConnectionListener` (quit/join)
  - `GuiClickListener` (inventory click)
  - `SpawnStickListener` (player interact while holding stick)
- `io.github.KennedySovine.hungerGames.storage`
  - `YamlStorage` helper to read/write YAML (`arenas.yml`, `pending-disconnects.yml`)
- `io.github.KennedySovine.hungerGames.task`
  - scheduled tasks if needed (e.g., periodic cleanup/sweep)

2) Data shapes and YAML schema
- `arenas.yml` (per-arena)
  - At top-level: `arenas:` mapping
  - Example (presented as key lines):
    - arenas:
      - arenaId:
        - mapName: "IslandMap"
        - minPlayers: 2
        - maxPlayers: 24
        - timeToShrinkSeconds: 600
        - centerSize: 200
        - gracePeriodSeconds: 120
        - chestRefillSeconds: 300
        - lobby:
          - world: "world"
          - x: 0.5
          - y: 64.0
          - z: -10.5
          - yaw: 90.0
          - pitch: 0.0
        - spawns:
          - [world,x,y,z,yaw,pitch] e.g. "world,10.5,65.0,-5.5,0.0,0.0"
- `pending-disconnects.yml`
  - pending:
    - uuid-string:
      - disconnectTimestampMs: 1670000000000
      - arenaId: "arenaId"
      - reason: "COMBAT_LOG" or "GRACE"
      - scheduledExpireMs: timestamp when the grace ends (optional)
- `players.yml` (optional): per-player metadata about forced deaths etc.

3) Event listeners: concrete behavior (what to listen and do)
- Damage tracking
  - Events: `EntityDamageByEntityEvent` and `EntityDamageEvent`
  - Behavior:
    - When both damager and victim are Players (or a player damaged by a projectile shot by a player), call `combatManager.recordDamage(attackerUuid, victimUuid, nowMs)`.
    - `recordDamage` sets `lastDamage` for both attacker and victim to now.
    - Example inline logic: `lastDamage.put(victimUuid, now); lastDamage.put(attackerUuid, now);`
    - Avoid counting environmental damage as PvP unless desired.
- PlayerQuitEvent
  - Event: `PlayerQuitEvent`
  - Behavior:
    - If `combatManager.isInCombat(playerUuid)`:
      - Immediately mark player as dead/in arena (call `combatManager.forceKill(player, "Combat logging")` or `arenaManager.handlePlayerDeath(player, reason)`).
      - Broadcast to arena players that player was killed by combat-logging.
      - NOTE: Because vanilla removes the player entity on quit, no NPC required.
      - Persist death result if necessary for match state.
    - Else (not in combat):
      - Add entry to `pendingDisconnects` with `disconnectTimestampMs = now`, `arenaId = player's current arena`.
      - Schedule a `runTaskLater` for `graceMs` to check if the player reconnected; if still missing, mark dead/out-of-arena.
      - Save pending disconnects to disk.
    - Inline action to schedule:
      - `Bukkit.getScheduler().runTaskLater(plugin, () -> checkAndKillIfStillOffline(uuid), graceSeconds*20L)`
- PlayerJoinEvent
  - Event: `PlayerJoinEvent`
  - Behavior:
    - If player has a pending disconnect entry:
      - If pending entry indicates combat-logged (dead) or grace has expired: present player with death/respawn flow, show message "You were killed while disconnected (combat logging)". Teleport them to lobby or respawn as plugin expects.
      - If pending entry still in grace: cancel scheduled kill task, remove pending entry, restore their arena state (teleport back to where they were or keep as-is).
    - If player had been forced-dead when they disconnected: do respawn flow and show message.
- Inventory GUI events
  - Event: `InventoryClickEvent` for GUI inventories created by `ArenaEditorGui`
  - Behavior:
    - Prevent raw item movement when inventory is a GUI
    - Map clicked slot to button/action: open sub-guis to edit numeric fields (timeToShrink) or toggle settings. For text or numeric values, either use a sign prompt or chat-based input after clicking an "edit" slot.
    - Provide "Give Spawn-Stick" button; clicking calls `ArenaEditorGui.giveSpawnStick(admin, arenaId)` and closes GUI with instruction messages.
- Stick right-click spawn setter
  - Event: `PlayerInteractEvent`
  - Behavior:
    - If player holds a stick with required metadata (custom name / lore that includes `arenaId`), and action is RIGHT_CLICK_BLOCK or RIGHT_CLICK_AIR:
      - Use the player's current location (or clicked block location as anchor) and call `arenaManager.addSpawnPoint(arenaId, location)`.
      - Give feedback: play sound, send message "Spawn point #N saved for arena X".
    - Implement permission check: `player.hasPermission("hungergames.admin")`
    - Inline logic description: check `ItemStack` meta by `hasItemMeta()` and `getDisplayName().contains(arenaId)` or use persistent ItemStack `PersistentDataContainer` key `hg:spawnstick`.

4) Critical inline code examples (short, inline, not full classes)
- Tracking last damage (inline):
  - On damage: `long now = System.currentTimeMillis(); lastDamage.put(victimUuid, now); lastDamage.put(attackerUuid, now);`
  - `isInCombat(UUID u)`: `Long t = lastDamage.get(u); return t != null && (System.currentTimeMillis() - t) <= combatWindowMs;`
- Scheduling kill task (inline):
  - `Bukkit.getScheduler().runTaskLater(plugin, () -> { if (!playerIsOnline(uuid)) forceKill(uuid, "Grace expired"); }, graceSeconds * 20L);`
  - Store returned `BukkitTask` to `pendingKillTasks.put(uuid, task)` to allow cancellation.
- Quit handling (inline flow):
  - `onPlayerQuit(e): if (combatManager.isInCombat(uuid)) { forceKill(uuid, "Combat logging"); } else { recordPendingDisconnect(uuid, now); scheduleGraceKill(uuid, graceMs); }`
- Join handling (inline flow):
  - `onPlayerJoin(e): if (pendingDisconnects.contains(uuid)) { cancelScheduledKill(uuid); removePendingDisconnect(uuid); sendMessage("You rejoined during grace"); } else if (wasMarkedDead(uuid)) { handleRespawnAndNotify("You were killed while offline"); }`
- Stick right-click to save spawn (inline):
  - `onPlayerInteract(e): if (isAdmin(p) && isSpawnStick(p.getItemInHand()) && e.getAction().isRightClick()) { arenaManager.addSpawnPoint(arenaId, p.getLocation()); p.sendMessage("Saved spawn #"+n); }`

5) Persistence & scheduled tasks
- Persist `arenas.yml` using Bukkit `FileConfiguration`. Save after every change and on shutdown.
- Persist `pending-disconnects.yml` (uuid -> disconnectTimestamp, arenaId, reason, scheduledExpireMs) whenever a pending is added/removed. On plugin enable, load these and:
  - For entries where scheduledExpireMs <= now: treat as expired and mark the player dead in the arena.
  - For entries with expire > now and player still offline: reschedule task to the remainder.
- Task management:
  - Use `BukkitScheduler.runTaskLater` per pending disconnect to simplify cancellation.
  - Store task handles (`BukkitTask`) in `pendingKillTasks` to cancel on reconnect.
  - For large servers, consider a single periodic sweep task: run every 5 seconds and check pending entries whose expire <= now.

6) Edge cases and handling
- Server crash while player disconnected within grace:
  - On plugin enable, read `pending-disconnects.yml`. If `scheduledExpireMs` still in future and player still offline, reschedule a kill for the remaining time. If expired, immediately process death.
- Player disconnects multiple times quickly:
  - Always cancel previous scheduled kill if they reconnect; for multiple disconnects without reconnecting (rare), ensure only one pending entry per UUID; update `disconnectTimestampMs` to the newest disconnect (but better: if no reconnect, first disconnect controls).
- Forced kicks or `Player#kickPlayer(String)`:
  - Treat like `PlayerQuitEvent`. If server kicked while in combat -> immediate death. If not in combat -> grace applies.
- Cross-server proxies (Bungee/Velocity):
  - Best practice: track combat state on the authority server where matches run. If players can move between servers, consider a centralized data store (Redis or database) or mark cross-server as unsupported. Document recommended approach: deny transfers while in combat or before grace expiry.
- Plugin reloads:
  - Persist `lastDamage`? Not necessary: treat plugin reload as process restart; if someone had pending disconnect, rely on `pending-disconnects.yml`. For active in-combat players who are online during reload, rebuild `lastDamage` only from memory is lost; to be conservative, on reload clear combat state (or persist lastDamage if desired). Recommend: do not support hot-reload; require server restart for safety or persist combat timestamps on shutdown.
- Player visibility while disconnected:
  - Vanilla already removes the player on disconnect. We will not spawn an NPC proxy to avoid traps. If you must spawn an NPC for killable presence, add separate NPC module; otherwise, do nothing on disconnect (no entity), only manage arena states and kills on backend.
- Items on disconnect:
  - By default, the player's inventory is kept with them on reconnect. If the match design requires leaving items, on quit process drop items at their last location or store and drop them when marking dead.

7) Permission nodes
- `hungergames.admin` — full access to GUI and arena commands.
- `hungergames.admin.create`, `hungergames.admin.edit`, `hungergames.admin.delete` — optional granular permissions.
- `hungergames.overridecombat` — optional for admins exempted from combat logging.

8) Defaults & configuration
- `combatWindowMs` default: 10_000 (10s) — configurable in plugin config
- `graceMs` default: 120_000 (120s / 2 minutes) — configurable per arena by `gracePeriodSeconds`
- Keep chestRefill default: 300 seconds
- Provide plugin `config.yml` entries to override defaults.

9) Tests and manual QA steps
- Unit testable parts (pure logic):
  - `CombatManager.isInCombat()` unit tests (timestamps before/after boundary).
  - `pending-disconnect` expiry logic: simulate now+grace and ensure expiry triggers kill.
- Manual QA steps (in-game):
  1. Create arena via `/arena create testArena` and set lobby/spawn via GUI.
  2. Join as Player A and Player B; have A damage B; A disconnect within 10s -> verify B receives message and A marked dead in arena.
  3. Disconnect while not in combat -> verify reconnect within 2 minutes restores; after 2 minutes, ensure marked dead.
  4. Give admin the spawn-stick; right-click at several locations and check `arenas.yml` contains spawn entries and that players are teleported to those spawns on match start.
  5. Plugin restart while player is disconnected during grace -> verify pending disconnect is processed correctly.
  6. Try forced kick while in combat, and non-combat kick to ensure correct behavior.
- Edge validation:
  - Rapid multiple disconnections, cross-server reconnects (if applicable), plugin reload behavior.

10) Performance and concurrency notes
- Use `ConcurrentHashMap` for `lastDamage` and `pendingKillTasks`.
- Avoid scheduling thousands of per-player tasks on very large servers — if expecting >1k concurrent matches, implement a single periodic sweep for pending disconnects.
- Persist writes: batch or debounce saves to disk to avoid I/O storms (save on change but debounce within 1–2s).
- Access to `FileConfiguration` should be synchronized or only used from the main thread (Bukkit API rule).
- All Bukkit API calls must be performed on the main server thread; scheduler's `runTaskLater` runs on main thread by default.

11) API 1.21 & Java 17 notes
- Use `PersistentDataContainer` on `ItemMeta` for spawn-stick metadata (recommended over displayName parsing).
- Use `Player#getUniqueId()` for identity.
- Use `Bukkit.getScheduler()` to schedule tasks; times are in ticks (20 ticks = 1 second).
- Use modern `var` sparingly; follow project coding style.

12) Minimal command & GUI flow suggestions (UX)
- `/arena create <id>` — creates minimal arena and opens `ArenaEditorGui`.
- `ArenaEditorGui` main inventory:
  - Slot X: Map name (click to change — chat prompt)
  - Slot Y: minPlayers/maxPlayers (left/right click to increment/decrement)
  - Slot Z: Time to shrink (left/right)
  - Slot S: "Set Player Spawn Points" (click -> give spawn-stick)
  - Slot L: Set Lobby (click -> uses current location as lobby)
  - Slot Save: Save and exit
  - Slot Cancel: Discard changes
- When admin clicks "Set Player Spawn Points", plugin closes GUI, gives stick with persistent data `hg:arenaId` and lore "Right-click to save spawn".

13) Minimal developer contract (inputs/outputs)
- Inputs: damage events, player quit/join, admin GUI clicks and right-clicks with the spawn-stick.
- Outputs: arena data saved to `arenas.yml`, pending disconnects saved to `pending-disconnects.yml`, scheduled kills executed as needed, GUI feedback messages.
- Error modes: plugin reload/persistence failure, disk I/O failure — log and notify admins.

14) Deliverable checklist for implementer
- Create classes described above (CombatManager, ArenaManager, ArenaEditorGui, listeners, commands).
- Add YAML storage files: `arenas.yml`, `pending-disconnects.yml`.
- Register listeners in `onEnable`.
- Add default config with `combatWindowMs=10000`, `graceMs=120000`.
- Add permission checks for GUI and commands.
- Implement tests for `CombatManager` logic and manual QA checklist.

That’s the draft plan — would you like me to:
- A) produce the exact class skeletons and method signatures next, or  
- B) produce the GUI inventory slot layout and exact item metadata for the spawn-stick, or  
- C) produce the YAML serializer/deserializer pseudocode?
