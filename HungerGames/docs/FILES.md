Required Source Files (skeletons to implement)

Below is a complete list of Java classes and files the project will need as part of the commands-first plan. Many command skeletons already exist under `src/main/java/.../command`.

Plugin main
- src/main/java/io/github/KennedySovine/hungerGames/HungerGames.java

Commands (already present as skeletons)
- src/main/java/io/github/KennedySovine/hungerGames/command/HgCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/SubCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/AbstractSubCommand.java

Admin Subcommands (skeletons already created)
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/CreateArenaCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/AddSpawnCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/RemoveSpawnCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/MaxPlayersCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/MinPlayersCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/TimeCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/CenterSizeCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/GracePeriodCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/ChestRefillCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/StartCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/admin/StopCommand.java

Player Subcommands (skeletons already created)
- src/main/java/io/github/KennedySovine/hungerGames/command/player/JoinCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/player/LeaveCommand.java
- src/main/java/io/github/KennedySovine/hungerGames/command/player/StatsCommand.java

Managers (create skeletons with Javadoc)
- src/main/java/io/github/KennedySovine/hungerGames/arena/Arena.java
- src/main/java/io/github/KennedySovine/hungerGames/arena/ArenaManager.java
- src/main/java/io/github/KennedySovine/hungerGames/game/GameManager.java
- src/main/java/io/github/KennedySovine/hungerGames/game/GameState.java
- src/main/java/io/github/KennedySovine/hungerGames/combat/CombatManager.java
- src/main/java/io/github/KennedySovine/hungerGames/stats/StatsManager.java

Storage and helpers
- src/main/java/io/github/KennedySovine/hungerGames/storage/YamlStorage.java
- src/main/java/io/github/KennedySovine/hungerGames/utils/LocationUtils.java

Listeners
- src/main/java/io/github/KennedySovine/hungerGames/listener/CombatListener.java
- src/main/java/io/github/KennedySovine/hungerGames/listener/PlayerConnectionListener.java
- src/main/java/io/github/KennedySovine/hungerGames/listener/PlayerDeathListener.java
- src/main/java/io/github/KennedySovine/hungerGames/listener/InventoryGuiListener.java
- src/main/java/io/github/KennedySovine/hungerGames/listener/SpawnStickListener.java

GUI (implemented later)
- src/main/java/io/github/KennedySovine/hungerGames/gui/ArenaEditorGui.java
- src/main/java/io/github/KennedySovine/hungerGames/gui/SpectatorGui.java

Resources (already created/updated)
- src/main/resources/plugin.yml (fully implemented)
- src/main/resources/arenas.yml (created on first save)
- src/main/resources/config.yml (to add default values: combatWindowMs=10000, graceMs=120000, chestRefill=300)
- src/main/resources/loot.yml (starter/refill tiers)

Docs (created)
- docs/ARCHITECTURE.md
- docs/COMPONENTS.md
- docs/DEVELOPMENT_GUIDE.md
- docs/DATA_SCHEMA.md
- docs/EVENTS.md
- docs/FILES.md
- docs/COMMANDS.md
- docs/MANAGERS.md
- docs/GUI_PLANS.md
- docs/LISTENERS.md
- docs/TEST_PLAN.md
- docs/TODO.md

Notes
- Each listed Java file should include top-level Javadoc describing responsibilities and method stubs.
- The command skeletons are present and registered in `HungerGames.onEnable()`; managers will be wired into these commands when their skeletons are implemented.

If you'd like, I can now generate empty skeleton class files for each manager/listener/storage entry above (with Javadocs and TODOs), or I can stop here and let you review the docs first. Which do you prefer?
