YAML Data Schemas

arenas.yml
```
arenas:
  example-arena:
    displayName: "Example Arena"
    minPlayers: 2
    maxPlayers: 24
    timeToShrinkSeconds: 600
    centerSize: 200
    gracePeriodSeconds: 120
    chestRefillSeconds: 300
    center:
      world: "world"
      x: 0.0
      y: 64.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0
    lobby:
      world: "world"
      x: 0.5
      y: 64.0
      z: -10.5
      yaw: 90.0
      pitch: 0.0
    spawns:
      - "10.5,1.0,-5.5,0.0,0.0"   # Relative offsets from center (dx,dy,dz,yaw,pitch)
      - "-8.0,0.5,12.0,180.0,0.0"
      - "15.2,2.0,8.5,90.0,0.0"
```

**Note on Spawn Points:**
- Spawn points are stored as **relative offsets** from the arena center
- Format: `dx,dy,dz,yaw,pitch` (no world name)
- When admin runs `/hg arena addspawn`, the spawn location is saved relative to the arena center
- When loading an arena for a match, spawn locations are calculated as: `center + offset`
- This allows arenas to be portable and relocated by simply updating the center point

pending-disconnects.yml
```
pending:
  uuid-string:
    disconnectTimestampMs: 1670000000000
    arenaId: "example-arena"
    reason: "GRACE" # or COMBAT_LOG
    scheduledExpireMs: 1670000000000
```

stats.yml
```
players:
  uuid-string:
    kills: 12
    deaths: 7
    wins: 1
    losses: 4
```

loot.yml (simple starter + refill tiers)
```
starter:
  - "minecraft:stone_sword:1"
  - "minecraft:apple:2"
refill:
  - "minecraft:iron_ingot:3"
  - "minecraft:golden_apple:1"
```

Notes
- **Arena spawn points** are saved as relative offsets `dx,dy,dz,yaw,pitch` from the arena center for portability. See LocationUtils for serialization helpers.
- **Lobby and center** locations are saved as absolute positions in CSV format `world,x,y,z,yaw,pitch` for readability.
- All timestamps are in epoch milliseconds.
- The working arena pattern means in-memory edits don't persist until `/hg arena save` is run.
