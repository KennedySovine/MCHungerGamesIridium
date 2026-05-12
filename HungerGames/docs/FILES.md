Required Source Files - Implementation Status

Below is a complete list of Java classes and files in the project. ✅ indicates implemented, ⚠️ indicates partial implementation.

Plugin main
- ✅ src/main/java/io/github/KennedySovine/hungerGames/HungerGames.java

Commands (command framework)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/HgCommand.java
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/SubCommand.java
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/AbstractSubCommand.java

Admin Subcommands (all implemented)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/ArenaParentCommand.java (parent for arena subcommands)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/CreateArenaCommand.java (creates master arena + loads it into working memory)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/ArenaLoadCommand.java (loads master → working copy)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/ArenaSaveCommand.java (saves working → master)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/ArenaListCommand.java (lists all arenas)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/AddSpawnCommand.java (adds spawn to working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/RemoveSpawnCommand.java (removes spawn from working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/MaxPlayersCommand.java (modifies working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/MinPlayersCommand.java (modifies working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/TimeCommand.java (modifies working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/CenterSizeCommand.java (modifies working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/GracePeriodCommand.java (modifies working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/ChestRefillCommand.java (modifies working arena)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/SetCenterCommand.java (sets arena center)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/StartCommand.java (runtime game control)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/StopCommand.java (runtime game control)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/BeaconsCommand.java (toggle spawn beacons)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/admin/GuiCommand.java (open arena editor GUI)
- ❌ src/main/java/io/github/KennedySovine/hungerGames/command/admin/SetLobbyCommand.java (DELETED - no longer needed)

Player Subcommands (all implemented)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/player/JoinCommand.java
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/player/LeaveCommand.java
- ✅ src/main/java/io/github/KennedySovine/hungerGames/command/player/StatsCommand.java

Managers (core implementation complete)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/arena/Arena.java (full POJO with validation)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/arena/ArenaManager.java (working arena pattern fully implemented)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/game/GameManager.java (lifecycle + death hooks)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/game/GameState.java (enum: LOBBY, COUNTDOWN, RUNNING, DEATHMATCH, FINISHED)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/combat/CombatManager.java (combat logging + grace periods)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/stats/StatsManager.java (match-scoped stats)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/stats/PlayerStats.java (data model)
- ⚠️ src/main/java/io/github/KennedySovine/hungerGames/chest/ChestLootManager.java (skeleton; needs weighted random)

Additional Managers
- ✅ src/main/java/io/github/KennedySovine/hungerGames/particle/ParticleManager.java (spawn beacon effects)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/border/BorderManager.java (world border shrinking)

Storage and helpers
- ✅ src/main/java/io/github/KennedySovine/hungerGames/storage/YamlStorage.java (atomic YAML persistence)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/utils/LocationUtils.java (serialization + relative offsets)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/utils/MessageUtils.java (chat formatting)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/utils/ParticleUtils.java (particle effect helpers)

Listeners (event handlers)
- ⚠️ src/main/java/io/github/KennedySovine/hungerGames/listener/CombatListener.java (basic impl; projectile handling TODO)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/listener/PlayerConnectionListener.java (join/quit handling)
- ⚠️ src/main/java/io/github/KennedySovine/hungerGames/listener/PlayerDeathListener.java (skeleton; needs full integration)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/listener/InventoryGuiListener.java (GUI click handling framework)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/listener/SpawnStickListener.java (spawn-stick interactions)

GUI (inventory-based interfaces)
- ⚠️ src/main/java/io/github/KennedySovine/hungerGames/gui/ArenaEditorGui.java (skeleton with inventory builder; interactive behavior deferred)
- ⚠️ src/main/java/io/github/KennedySovine/hungerGames/gui/SpectatorGui.java (placeholder; needs implementation)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/gui/ArenaListGui.java (arena browser GUI)
- ✅ src/main/java/io/github/KennedySovine/hungerGames/gui/AnvilPrompt.java (text input via anvil GUI)

Resources (configuration files)
- ✅ src/main/resources/plugin.yml (fully configured with commands and permissions)
- ✅ src/main/resources/config.yml (global settings)
- ✅ src/main/resources/chest-loot.yml (staged chest refill loot tables)
- (Generated at runtime) arenas.yml (created on first arena save)

Docs (all updated)
- ✅ docs/ARCHITECTURE.md (system overview)
- ✅ docs/COMPONENTS.md (component reference)
- ✅ docs/DEVELOPMENT_GUIDE.md (setup and workflows)
- ✅ docs/DATA_SCHEMA.md (YAML schemas with relative spawn format)
- ✅ docs/EVENTS.md (event hooks)
- ✅ docs/FILES.md (this file)
- ✅ docs/COMMANDS.md (complete command reference)
- ✅ docs/MANAGERS.md (manager details)
- ✅ docs/GUI_PLANS.md (GUI implementation plans)
- ✅ docs/LISTENERS.md (listener reference)
- ✅ docs/TEST_PLAN.md (testing strategy)
- ✅ docs/TODO.md (implementation roadmap with status)
- ✅ docs/STORAGE.md (persistence guide)

Notes
- All core command and manager infrastructure is implemented
- Listeners are created and registered; some need completion
- GUI frameworks exist; interactive behavior is deferred
- All files include appropriate Javadoc and inline comments
- Each implemented file has been tested manually on a dev server
