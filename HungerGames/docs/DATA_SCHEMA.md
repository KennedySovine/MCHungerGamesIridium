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
    lobby:
      world: "world"
      x: 0.5
      y: 64.0
      z: -10.5
      yaw: 90.0
      pitch: 0.0
    spawns:
      - "world,10.5,65.0,-5.5,0.0,0.0"
```

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
- Location strings are saved as CSV "world,x,y,z,yaw,pitch" for readability. Implement helper methods to parse/format.
- All timestamps are in epoch milliseconds.
