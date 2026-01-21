Components Reference

For each package, a short description of responsibilities and the key classes to implement.

io.github.KennedySovine.hungerGames
- HungerGames (plugin main class) — bootstrap managers and register commands/listeners.

io.github.KennedySovine.hungerGames.arena
- Arena — data holder for arena settings and spawn points.
- ArenaManager — create/load/save arenas, add/remove spawn points, provide lookup by name.

io.github.KennedySovine.hungerGames.game
- GameManager — lifecycle for matches; start/stop/join/leave; manage timers for chest refills and border shrink.
- GameState — enum: LOBBY, COUNTDOWN, RUNNING, DEATHMATCH, FINISHED.

io.github.KennedySovine.hungerGames.combat
- CombatManager — track last-damage timestamps, schedule grace kills, persist pending disconnects.

io.github.KennedySovine.hungerGames.stats
- StatsManager — track kills/deaths/wins/losses and persist them to stats.yml.

io.github.KennedySovine.hungerGames.command
- HgCommand — central dispatcher.
- SubCommand/AbstractSubCommand — framework for subcommands.
- Per-subcommand classes (admin/player).

io.github.KennedySovine.hungerGames.gui
- ArenaEditorGui — inventory GUI for editing arena settings.
- SpectatorGui — teleport UI for spectators.

io.github.KennedySovine.hungerGames.storage
- YamlStorage — helper to manage FileConfiguration files for arenas.yml/stats.yml/pending-disconnects.yml.

io.github.KennedySovine.hungerGames.listener
- CombatListener, PlayerConnectionListener, PlayerDeathListener, ChestRefillListener, GuiClickListener, SpawnStickListener.

Implementation notes
- Each component should include JavaDoc and inline comments explaining non-trivial logic.
- Persistence must be robust to server restarts; pending disconnects should be rescheduled on plugin enable.
- Tests: unit test pure logic where possible (CombatManager.isInCombat(), Arena name normalization, parsing).
