# MCHungerGamesIridium

A comprehensive Minecraft Hunger Games plugin designed for the Iridium server events. Built for Paper/Spigot 1.21.5+, this plugin provides a complete battle royale experience with arena management, combat logging, spectator systems, and more.

## Features

### Core Gameplay
- **Arena System**: Create and manage multiple arenas with configurable spawn points, player limits, and game settings
- **Combat Logging**: 10-second combat timer with automatic elimination for combat loggers
- **Grace Period**: 120-second reconnect grace period for non-combat disconnects
- **Match Statistics**: Track kills, deaths, wins, and losses per match (match-scoped)
- **Spectator Mode**: Dead players can spectate remaining combatants

### Arena Configuration
- **Relative Spawn Points**: Spawn points saved as offsets from arena center for easy portability
- **Working Arena System**: Edit arenas in-memory without affecting saved data until explicitly saved
- **Flexible Settings**: Configure min/max players, grace periods, chest refill times, center size, and shrink timers
- **Dynamic Borders**: Configurable world border shrinking for deathmatch phase

### Administration
- **Command-Based Setup**: Full suite of admin commands for arena creation and configuration
- **GUI Editors**: Inventory-based GUI for arena editing (framework implemented)
- **Spawn Stick**: Special item for placing spawn points at your location
- **YAML Storage**: All data persists to human-readable YAML files

## Quick Start

### Installation
1. Download the latest release JAR
2. Place in your server's `plugins/` folder
3. Restart the server
4. Plugin will create default configuration files

### Creating Your First Arena

```bash
# Create a new arena
/hg arena create myarena "My First Arena"

# Stand at the arena center and set it
/hg setcenter

# Walk to each spawn point and add them
/hg arena addspawn

# Configure arena settings
/hg minplayers 2
/hg maxplayers 24
/hg graceperiod 120
/hg chestrefill 300

# Save your changes
/hg arena save
```

### Starting a Game

```bash
# Start the arena
/hg start myarena

# Players join
/hg join myarena
```

## Commands

### Player Commands
| Command | Description |
|---------|-------------|
| `/hg join <arena>` | Join the specified arena |
| `/hg leave` | Leave your current arena |
| `/hg stats [player]` | View match statistics |

### Admin Commands (require `hungergames.admin` permission)

#### Arena Management
| Command | Description |
|---------|-------------|
| `/hg arena create <id> [displayName]` | Create a new arena |
| `/hg arena load <id>` | Load arena into working memory for editing |
| `/hg arena save` | Save working arena changes to disk |
| `/hg arena list` | List all arenas |
| `/hg setcenter` | Set the center point for the current working arena |

#### Arena Configuration
| Command | Description |
|---------|-------------|
| `/hg arena addspawn` | Add spawn point at your location |
| `/hg arena removespawn <index>` | Remove spawn point by index |
| `/hg minplayers <number>` | Set minimum players required |
| `/hg maxplayers <number>` | Set maximum players allowed |
| `/hg graceperiod <seconds>` | Set disconnect grace period |
| `/hg chestrefill <seconds>` | Set chest refill interval |
| `/hg time <seconds>` | Set time before border shrink |
| `/hg centersize <blocks>` | Set final deathmatch area size |

#### Game Control
| Command | Description |
|---------|-------------|
| `/hg start [arena]` | Start a match |
| `/hg stop [arena]` | Stop the running match |
| `/hg beacons` | Toggle spawn beacons visibility |
| `/hg gui` | Open arena editor GUI |

## Architecture

### Core Components

- **ArenaManager**: Manages arena definitions, spawn points, and persistence
- **GameManager**: Controls match lifecycle (lobby → countdown → running → deathmatch → finished)
- **CombatManager**: Tracks combat status, handles disconnects, enforces combat-logging rules
- **StatsManager**: Records player performance statistics
- **ChestLootManager**: Manages chest loot tables and refills

### Data Files

All data is stored in `plugins/HungerGames/`:
- `arenas.yml` - Arena configurations and spawn points
- `config.yml` - Global settings and chest loot tables
- `stats.yml` - Player statistics (match-scoped)

### Working Arena Semantics

The plugin uses a "working arena" pattern for safe editing:
1. Load an arena into working memory with `/hg arena load <id>`
2. Make changes using edit commands (these modify in-memory data only)
3. Save changes to disk with `/hg arena save`

This prevents accidental corruption of arena data and allows experimentation before committing changes.

## Development

### Building

```bash
./gradlew build
```

Output JAR will be in `build/libs/HungerGames-1.0-SNAPSHOT.jar`

### Requirements
- Java 17+
- Paper/Spigot 1.21.5+

### Testing
Copy the built JAR to a development server's `plugins/` folder and restart.

## Documentation

Detailed documentation available in `HungerGames/docs/`:
- [TODO.md](HungerGames/docs/TODO.md) - Implementation roadmap and status
- [ARCHITECTURE.md](HungerGames/docs/ARCHITECTURE.md) - System architecture overview
- [COMMANDS.md](HungerGames/docs/COMMANDS.md) - Complete command reference
- [MANAGERS.md](HungerGames/docs/MANAGERS.md) - Manager component details
- [DEVELOPMENT_GUIDE.md](HungerGames/docs/DEVELOPMENT_GUIDE.md) - Developer setup and conventions
- [GUI_PLANS.md](HungerGames/docs/GUI_PLANS.md) - GUI implementation plans

## Current Status

The plugin is approximately **95% complete** with all core systems implemented:
- ✅ Storage & utilities (YamlStorage, LocationUtils)
- ✅ Arena system with working-arena pattern
- ✅ All admin and player commands
- ✅ Game lifecycle management
- ✅ Combat logging and grace periods
- ✅ Match statistics tracking
- ✅ Event listeners (combat, connections, death)
- ✅ GUI frameworks (interactive behavior deferred)

Remaining work focuses on testing, polish, and completing GUI interactivity. See [TODO.md](HungerGames/docs/TODO.md) for detailed status.

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]
