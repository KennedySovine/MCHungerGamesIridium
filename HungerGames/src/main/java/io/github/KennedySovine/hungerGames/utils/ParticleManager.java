package io.github.KennedySovine.hungerGames.utils;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Tracks active beacon particle tasks per arena. Allows adding and removing
 * spawn beacons and clearing them when the arena changes.
 */
public class ParticleManager {

    // arenaId -> list of beacon tasks for spawn points (index -> task)
    private final Map<String, Map<Integer, BukkitTask>> spawnBeacons = new HashMap<>();

    // arenaId -> center beacon task
    private final Map<String, BukkitTask> centerBeacons = new HashMap<>();

    private final HungerGames plugin;
    private boolean enabled = true;

    public ParticleManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    public void clearAllForArena(String arenaId) {
        Map<Integer, BukkitTask> map = spawnBeacons.remove(arenaId);
        if (map != null) {
            for (BukkitTask t : map.values()) if (t != null) t.cancel();
        }
        BukkitTask cb = centerBeacons.remove(arenaId);
        if (cb != null) cb.cancel();
    }

    public void clearAll() {
        // cancel all spawn and center beacons across arenas
        for (Map<Integer, BukkitTask> m : spawnBeacons.values()) if (m != null) for (BukkitTask t : m.values()) if (t != null) t.cancel();
        spawnBeacons.clear();
        for (BukkitTask t : centerBeacons.values()) if (t != null) t.cancel();
        centerBeacons.clear();
    }

    public void showCenter(String arenaId, Location loc) {
        if (!enabled) return;
        if (arenaId == null || loc == null) return;
        // cancel previous if present
        BukkitTask prev = centerBeacons.remove(arenaId);
        if (prev != null) prev.cancel();
        BukkitTask task = ParticleUtils.showCenterBeacon(plugin, loc);
        if (task != null) centerBeacons.put(arenaId, task);
    }

    public void showSpawn(String arenaId, int index, Location loc) {
        if (!enabled) return;
        if (arenaId == null || loc == null) return;
        Map<Integer, BukkitTask> map = spawnBeacons.computeIfAbsent(arenaId, k -> new HashMap<>());
        // cancel existing for index
        BukkitTask prev = map.remove(index);
        if (prev != null) prev.cancel();
        BukkitTask task = ParticleUtils.showSpawnBeacon(plugin, loc);
        if (task != null) map.put(index, task);
    }

    public void removeSpawn(String arenaId, int index) {
        Map<Integer, BukkitTask> map = spawnBeacons.get(arenaId);
        if (map == null) return;
        BukkitTask t = map.remove(index);
        if (t != null) t.cancel();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) {
        this.enabled = v;
        if (!v) clearAll();
    }

    public void refreshAllSpawns(String arenaId, Arena arena) {
        // cancel all
        Map<Integer, BukkitTask> map = spawnBeacons.remove(arenaId);
        if (map != null) for (BukkitTask t : map.values()) if (t != null) t.cancel();
        if (arena == null) return;
        // re-show beacons for all spawn offsets
        java.util.List<String> offsets = arena.getSpawnOffsets();
        for (int i = 0; i < offsets.size(); i++) {
            String off = offsets.get(i);
            // deserialize relative against arena's lobby location
            org.bukkit.Location center = arena.getLobbyLocation();
            org.bukkit.Location abs = io.github.KennedySovine.hungerGames.utils.LocationUtils.deserializeRelative(off, center);
            if (abs != null) showSpawn(arenaId, i, abs);
        }
    }
}
