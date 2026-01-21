Development Guide

Coding conventions
- Java 17, follow existing project's style (small classes, descriptive names).
- Javadoc on each public class and method. Inline comments for non-trivial behavior.

Testing
- Unit test pure logic in `src/test/java` using JUnit 5.
- Integration testing on a local dev server (Paper 1.21.5) to validate Bukkit interactions.

How to run locally
1. Build plugin: `./gradlew build` (Windows: `gradlew.bat build`).
2. Copy `build/libs/HungerGames-1.0-SNAPSHOT.jar` into server `plugins/` and start Paper server.

Persistence
- Data stored under plugin folder:
  - `arenas.yml` — arena definitions
  - `stats.yml` — player statistics
  - `pending-disconnects.yml` — combat logging pending entries
  - `loot.yml` — chest loot tiers

Style rules for files created by this project
- Each class must include a file header Javadoc with a short description and expected interactions.
- Methods performing I/O must document thread expectations (main thread only) and mention the saving strategy.

Deliverables for initial commands-first milestone
- ArenaManager with arenas.yml persistence.
- GameManager skeleton (start/stop/join/leave) with in-memory game state.
- CombatManager skeleton and basic event listeners.
- Command skeletons wired to managers.

Next steps after commands-first
- Implement GUI-based arena editor.
- Implement chest loot refills and border shrink logic.
- Improve storage to SQLite if required for scale.
