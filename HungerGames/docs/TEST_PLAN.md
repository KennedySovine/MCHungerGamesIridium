Test Plan

## Unit Tests (To Be Implemented)

**High Priority:**
- **LocationUtils** serialization tests:
  - `serialize()` / `deserialize()` round-trip with various locations
  - `serializeRelative()` / `deserializeRelative()` with different center points
  - Edge cases: null world, extreme coordinates, zero offsets

- **CombatManager.isInCombat()** boundary tests:
  - Exactly at 10s boundary (should return false)
  - Just before 10s (should return true)
  - Just after 10s (should return false)
  - No damage recorded (should return false)

- **Arena validation**:
  - `validate()` method with invalid configurations (min > max, negative values)
  - Case-insensitive arena ID lookup
  - Invalid arena names rejected

- **ArenaManager** persistence tests:
  - Create arena, save to YAML, load from YAML, compare all fields
  - Working arena pattern: load, modify, save, verify master updated
  - Relative spawn offset calculations

**Medium Priority:**
- **Parsing utilities**: integer parse helpers, safe number conversion
- **StatsManager**: increment operations, stats retrieval
- **GameState** transitions: valid state flow enforcement

## Integration Tests (Manual Testing Required)

**Arena Management Flow:**
1. Create arena with `/hg arena create testArena`
2. Set center with `/hg setcenter`
3. Add multiple spawns at different locations with `/hg arena addspawn`
4. Modify all arena properties (min/max players, grace, refill, etc.)
5. Save with `/hg arena save`
6. Verify `arenas.yml` contains correct data with relative spawn offsets
7. Reload plugin and verify arena persists correctly

**Match Lifecycle:**
1. Start match with `/hg start testArena`
2. Multiple players join with `/hg join testArena`
3. Verify state transitions: LOBBY → COUNTDOWN → RUNNING
4. Test player death handling and spectator mode
5. Verify winner declaration and state transition to FINISHED
6. Stop match with `/hg stop testArena`

**Combat Logging & Grace Periods:**
1. Two players damage each other (enter combat)
2. One player disconnects while in combat → verify immediate elimination message
3. Player disconnects while NOT in combat → verify grace period message
4. Reconnect within grace period → verify player restored to match
5. Let grace period expire → verify player eliminated
6. Check stats updated correctly for all scenarios

**Chest Refill Schedule:**
1. Start match with configured chest refill interval (e.g., 300 seconds)
2. Monitor server logs for chest refill task execution
3. Verify chests populate with items at scheduled intervals
4. Verify task stops when match ends

**Border Shrink Schedule:**
1. Start match with configured shrink time (e.g., 600 seconds)
2. Monitor world border size
3. Verify border shrinks to configured centerSize
4. Verify DEATHMATCH phase activates
5. Verify task stops when match ends

**GUI Testing:**
1. Open arena editor GUI with `/hg gui`
2. Verify inventory displays correctly
3. Test beacon toggle with `/hg beacons`
4. Verify spawn point particle effects appear/disappear
5. Test arena list GUI displays all arenas

## Manual Smoke Test Checklist

**Setup:**
- [ ] Build plugin: `./gradlew build`
- [ ] Install on Paper 1.21.5+ server
- [ ] Start server successfully
- [ ] Plugin loads without errors

**Basic Functionality:**
- [ ] `/hg help` displays command list
- [ ] `/hg arena list` works (even with no arenas)
- [ ] Create arena: `/hg arena create testArena "Test Arena"`
- [ ] Set center: `/hg setcenter`
- [ ] Add spawn: `/hg arena addspawn`
- [ ] Configure all settings (minplayers, maxplayers, etc.)
- [ ] Save arena: `/hg arena save`
- [ ] Verify `arenas.yml` created in plugin folder

**Match Testing:**
- [ ] Load arena: `/hg arena load testArena`
- [ ] Start match: `/hg start testArena`
- [ ] Join with player: `/hg join testArena`
- [ ] Leave with player: `/hg leave`
- [ ] Stop match: `/hg stop testArena`

**Combat & Disconnects:**
- [ ] Two players join match
- [ ] Players damage each other
- [ ] One disconnects during combat → verify immediate death
- [ ] One disconnects outside combat → verify grace period message
- [ ] Reconnect within 120s → verify player restored
- [ ] Let grace expire → verify player eliminated

**Statistics:**
- [ ] View stats: `/hg stats`
- [ ] Verify kills/deaths/wins/losses tracked correctly
- [ ] Verify stats reset between matches

**Permissions:**
- [ ] Admin commands blocked for non-admin players
- [ ] Player commands work without permissions
- [ ] Permission node `hungergames.admin` grants admin access

## Performance Testing (Future)

- [ ] Test with maximum players (24+)
- [ ] Monitor server TPS during match
- [ ] Profile chest refill and border shrink task performance
- [ ] Test multiple concurrent matches (if supported)
- [ ] Load test arena persistence with many arenas

## Regression Testing

After any significant changes, re-run:
- [ ] Arena creation and editing workflow
- [ ] Full match lifecycle (start to finish)
- [ ] Combat logging scenarios
- [ ] Grace period scenarios
- [ ] Stats tracking accuracy

## Current Test Coverage Status

✅ **Manual Testing**: Core functionality validated on dev server  
⚠️ **Unit Tests**: Framework exists; tests need implementation  
⚠️ **Integration Tests**: Manual only; automated tests planned  
❌ **Performance Tests**: Not yet conducted  
❌ **Regression Suite**: To be created

## Next Steps

1. Implement LocationUtils unit tests
2. Implement CombatManager boundary tests
3. Create ArenaManager persistence tests
4. Develop automated integration test framework
5. Document all test results and edge cases discovered

