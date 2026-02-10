package io.github.KennedySovine.hungerGames.utils;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages applying and clearing world borders for an active working arena.
 * Stores previous border states per world and restores them when cleared.
 */
public class BorderManager {

    private static class BorderState {
        final Location center;
        final double size;
        BorderState(Location center, double size) {
            this.center = center;
            this.size = size;
        }
    }

    private final HungerGames plugin;
    private final Map<String, BorderState> previous = new HashMap<>();
    private String activeArenaId = null;

    public BorderManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Apply a world border centered at the given location with the provided size (diameter).
     * Stores previous world border state for restoration.
     */
    public void applyBorder(String arenaId, Location center, double size) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();
        WorldBorder wb = world.getWorldBorder();
        // save previous state if not already saved for this world
        String worldName = world.getName();
        if (!previous.containsKey(worldName)) {
            try {
                BorderState st = new BorderState(wb.getCenter(), wb.getSize());
                previous.put(worldName, st);
            } catch (Throwable ignored) {}
        }
        try {
            wb.setCenter(center.getX(), center.getZ());
            wb.setSize(size);
            activeArenaId = arenaId;
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to apply world border for arena " + arenaId + ": " + t.getMessage());
        }
    }

    /**
     * Clear the world border applied for the given arena id, restoring previous border state if present.
     */
    public void clearBorder(String arenaId) {
        if (arenaId == null) return;
        if (!arenaId.equals(activeArenaId)) return; // only clear if matches current
        // restore all stored world borders
        for (Map.Entry<String, BorderState> e : previous.entrySet()) {
            String worldName = e.getKey();
            BorderState st = e.getValue();
            World w = plugin.getServer().getWorld(worldName);
            if (w == null) continue;
            try {
                WorldBorder wb = w.getWorldBorder();
                if (st.center != null) wb.setCenter(st.center.getX(), st.center.getZ());
                wb.setSize(st.size);
            } catch (Throwable ignored) {}
        }
        previous.clear();
        activeArenaId = null;
    }

    public Optional<String> getActiveArenaId() { return Optional.ofNullable(activeArenaId); }
}

