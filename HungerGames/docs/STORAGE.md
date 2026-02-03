Storage Guide

Files persisted by the plugin (under plugin data folder):
- arenas.yml - arena definitions and spawn lists (master data)
- stats.yml - player statistics
- pending-disconnects.yml - entries for disconnected players during grace/combat
- loot.yml - configurable chest loot tiers

Guidelines
- Use Bukkit's FileConfiguration API to read/write YAML files.
- Prefer saving after each administrative change but debounce saves if bulk edits occur.
- On plugin enable, load all persisted files and reschedule pending tasks (e.g., pending disconnect expirations).
- All file I/O using Bukkit's FileConfiguration must be accessed from the main thread. If heavy work is required, use asynchronous I/O for serialization only and schedule writes on main thread.

Working arena persistence note
- Administrative edit commands operate on an in-memory "working" arena. Changes are kept in memory until the admin explicitly runs `/hg arena save` which replaces the master arena entry in `arenas.yml` and writes it to disk.
- The `CreateArenaCommand` creates a master entry for a new arena and loads it into working memory so that `arena save` will succeed after edits.

Helper
- Implement `YamlStorage` utility with methods:
  - `FileConfiguration load(String fileName)`
  - `void save(String fileName, FileConfiguration config)`
  - `void ensureExists(String fileName, InputStream defaultResource)`

Security
- Do not store sensitive data. All data are public game config/state.

Backup
- Provide a simple admin command `/hg backup` (future) to export current plugin data to a zip in the plugin folder.
