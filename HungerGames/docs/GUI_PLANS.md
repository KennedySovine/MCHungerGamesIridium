GUI Plans (for later)

Arena Editor GUI
- Inventory-based editor opened via `/hg gui` or `/hg arena edit <arenaName>`
- Main menu slots:
  - Map name (click -> chat input to rename)
  - Min players / Max players (left/right click to increment/decrement)
  - Time to shrink (left/right click)
  - Center size
  - Grace period
  - Chest refill
  - Set lobby (click -> uses player location)
  - Set spawn points (click -> give spawn-stick)
  - Save / Cancel buttons

Spawn-stick
- Item: STICK with PersistentDataContainer key `hungergames:spawnstick` containing `arenaId`.
- Right-click action: record player's current location as a spawn point.

Spectator GUI
- Inventory listing alive players; clicking teleports spectator to target.

Notes
- GUIs must cancel InventoryClickEvent to prevent players from moving items.
- Provide visual affordances (item meta, lore) describing actions.
- Provide a confirmation step when removing spawns or deleting arenas.
