Test Plan

Unit tests
- CombatManager.isInCombat(): boundary tests for 10s window and after.
- Arena name normalization: ensure case-insensitive lookup and invalid names rejected.
- Parsing utilities: location parse/format, integer parse helpers.

Integration tests (manual)
- Create arena, add spawns, start match, simulated player damage and disconnects, reconnects.
- Chest refill schedule — ensure chests refill at configured intervals.
- Border shrink schedule — ensure border reduces over time to centerSize.

Manual smoke checklist
1. Build plugin and install on Paper 1.21.5 server.
2. Confirm /hg help appears and lists subcommands.
3. Create an arena and add at least one spawn.
4. Join two players, damage and disconnect one during combat -> verify immediate death message.
5. Disconnect while not in combat -> reconnect within 2 minutes -> verify restored.
6. Let grace expire -> verify player is killed and removed from match.
7. Start and stop a match via commands and confirm GameManager state transitions.

