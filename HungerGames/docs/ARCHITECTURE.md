HungerGames — Architecture Overview

Purpose
- High-level overview of components and how they interact.

Core modules
- ArenaManager: manages arena definitions, spawn points, lobby, and persistence (arenas.yml).
- GameManager: manages running games (lobby, countdown, running, deathmatch, finished).
- CombatManager: tracks damage timestamps, pending disconnects, and enforces combat-logging rules.
- StatsManager: records/persists player stats (kills, deaths, wins, losses) to stats.yml.
- Storage: YamlStorage helper to load/save plugin YAML files safely on the main thread.
- GUI: Inventory-based admin GUIs (ArenaEditorGui, SpectatorGui) — implemented after commands.
- Commands: central `HgCommand` dispatcher and subcommand implementations.
- Listeners: event handlers (damage, join/quit, death, chest open, inventory, interact).

Data flow
1. Admin uses `/hg` commands (or GUI later) to create/edit arenas -> ArenaManager persists to arenas.yml.
2. Players join via GameManager; GameManager uses Arena spawns to teleport players and manage inventories.
3. CombatManager listens to damage events; on quit it schedules/executes kills.
4. StatsManager updates kills/deaths/wins/losses on player death and round end.

Threading and persistence
- All Bukkit API calls must be performed on the main server thread.
- FileConfiguration reads/writes are performed on the main thread or scheduled via Bukkit task; use debounce when saving frequently.

Notes
- Commands-first approach: implement command wiring and managers before GUI work.
- Kit and Teams are deferred for now (out of scope).
