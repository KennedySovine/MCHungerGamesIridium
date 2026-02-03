package io.github.KennedySovine.hungerGames.arena;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.storage.YamlStorage;
import io.github.KennedySovine.hungerGames.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manager responsible for creating, loading, saving and querying arenas.
 *
 * This class handles arenas.yml persistence via YamlStorage. All Bukkit API
 * calls must be made on the main thread.
 */
public class ArenaManager {

    private final HungerGames plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    // Working (loaded) arena used as a placeholder for admin edits. This arena
    // is kept in-memory until an explicit save is performed.
    private Arena workingArena = null;

    public ArenaManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Load arenas from arenas.yml. Implementers should call this during plugin
     * enable and after major changes.
     *
     * Note: spawns are stored as relative-offset strings in the file. These
     * offsets are applied at runtime using the current center passed to
     * Arena.getAbsoluteSpawns(center).
     */
    public void loadArenas() {
        FileConfiguration cfg = plugin.getStorage().load("arenas.yml");
        arenas.clear();
        if (!cfg.isConfigurationSection("arenas")) return;
        for (String key : cfg.getConfigurationSection("arenas").getKeys(false)) {
            String base = "arenas." + key + ".";
            String displayName = cfg.getString(base + "displayName", key);
            Arena a = new Arena(key, displayName);
            a.setMinPlayers(cfg.getInt(base + "minPlayers", a.getMinPlayers()));
            a.setMaxPlayers(cfg.getInt(base + "maxPlayers", a.getMaxPlayers()));
            a.setTimeToShrinkSeconds(cfg.getInt(base + "timeToShrinkSeconds", a.getTimeToShrinkSeconds()));
            a.setCenterSize(cfg.getInt(base + "centerSize", a.getCenterSize()));
            a.setGracePeriodSeconds(cfg.getInt(base + "gracePeriodSeconds", a.getGracePeriodSeconds()));
            a.setChestRefillSeconds(cfg.getInt(base + "chestRefillSeconds", a.getChestRefillSeconds()));

            // lobby location (serialized string)
            String lobbyStr = cfg.getString(base + "lobby");
            Location lobby = LocationUtils.deserialize(lobbyStr);
            if (lobby != null) a.setLobbyLocation(lobby);

            // spawns - stored as list of relative-offset strings
            List<String> spawns = cfg.getStringList(base + "spawns");
            for (String s : spawns) {
                if (s == null || s.isEmpty()) continue;
                a.addSpawnOffset(s);
            }

            arenas.put(key, a);
        }
    }

    /**
     * Save arenas to arenas.yml. Should be invoked after changes.
     *
     * Spawns are persisted as relative-offset strings. The expectation is that
     * the arena's lobbyLocation was the center used when offsets were created.
     */
    public void saveArenas() {
        FileConfiguration cfg = plugin.getStorage().load("arenas.yml");
        // reset arenas section
        cfg.set("arenas", null);
        for (Arena a : arenas.values()) {
            String base = "arenas." + a.getId() + ".";
            cfg.set(base + "displayName", a.getDisplayName());
            cfg.set(base + "minPlayers", a.getMinPlayers());
            cfg.set(base + "maxPlayers", a.getMaxPlayers());
            cfg.set(base + "timeToShrinkSeconds", a.getTimeToShrinkSeconds());
            cfg.set(base + "centerSize", a.getCenterSize());
            cfg.set(base + "gracePeriodSeconds", a.getGracePeriodSeconds());
            cfg.set(base + "chestRefillSeconds", a.getChestRefillSeconds());
            cfg.set(base + "lobby", LocationUtils.serialize(a.getLobbyLocation()));

            // store the relative-offset strings directly
            cfg.set(base + "spawns", a.getSpawnOffsets());
        }
        plugin.getStorage().save("arenas.yml", cfg);
    }

    /**
     * Create a new arena with the given id and display name. This method does
     * not persist to disk automatically; caller should call saveArenas().
     */
    public Arena createArena(String id, String displayName) {
        String key = normalizeId(id);
        if (arenas.containsKey(key)) {
            return null; // already exists
        }
        Arena a = new Arena(key, displayName == null ? id : displayName);
        arenas.put(key, a);
        return a;
    }

    public boolean deleteArena(String id) {
        return arenas.remove(normalizeId(id)) != null;
    }

    public Optional<Arena> getArena(String id) {
        return Optional.ofNullable(arenas.get(normalizeId(id)));
    }

    public List<String> getArenaNames() {
        return new ArrayList<>(arenas.keySet());
    }

    private String normalizeId(String id) {
        return id == null ? null : id.toLowerCase(Locale.ROOT).trim();
    }

    /**
     * Add a spawn point to the arena identified by arenaId. Returns true on
     * success (arena exists) and false if the arena was not found.
     *
     * Important: This method requires the arena to already have a
     * lobbyLocation set. The lobbyLocation is considered the center position
     * that was used when the admin created the spawn (per project workflow).
     */
    public boolean addSpawnPoint(String arenaId, Location loc) {
        Optional<Arena> o = getArena(arenaId);
        if (o.isEmpty()) return false;
        Arena a = o.get();
        Location center = a.getLobbyLocation();
        if (center == null) {
            // We require the arena's lobby/center to be set before adding spawns.
            return false;
        }
        a.addSpawnRelative(center, loc);
        return true;
    }

    /**
     * Remove the spawn at the given index for the arena. Returns true on success.
     */
    public boolean removeSpawnPoint(String arenaId, int index) {
        Optional<Arena> o = getArena(arenaId);
        if (o.isEmpty()) return false;
        Arena a = o.get();
        if (index < 0 || index >= a.getSpawnOffsets().size()) return false;
        a.removeSpawn(index);
        return true;
    }

    // --- Working (placeholder) arena APIs ---

    /**
     * Load the working arena by copying the master arena into a separate in-memory
     * placeholder. Returns true if the arena was found and loaded.
     */
    public boolean loadWorkingArena(String id) {
        Optional<Arena> o = getArena(id);
        if (o.isEmpty()) return false;
        Arena src = o.get();
        Arena copy = new Arena(src.getId(), src.getDisplayName());
        copy.setMinPlayers(src.getMinPlayers());
        copy.setMaxPlayers(src.getMaxPlayers());
        copy.setTimeToShrinkSeconds(src.getTimeToShrinkSeconds());
        copy.setCenterSize(src.getCenterSize());
        copy.setGracePeriodSeconds(src.getGracePeriodSeconds());
        copy.setChestRefillSeconds(src.getChestRefillSeconds());
        copy.setLobbyLocation(src.getLobbyLocation());
        // copy spawn offsets
        for (String s : src.getSpawnOffsets()) {
            copy.addSpawnOffset(s);
        }
        this.workingArena = copy;
        return true;
    }

    /**
     * Return the working arena if one is loaded.
     */
    public Optional<Arena> getWorkingArena() {
        return Optional.ofNullable(workingArena);
    }

    /**
     * Persist the currently loaded working arena back into the master arenas map
     * and save to disk. Throws IllegalStateException if no working arena is loaded
     * or if the working arena's id does not exist in the master list.
     */
    public void saveWorkingArena() {
        if (workingArena == null) throw new IllegalStateException("No working arena loaded");
        String id = workingArena.getId();
        if (!arenas.containsKey(id)) throw new IllegalStateException("Working arena id '" + id + "' is not present in master arenas list");
        // Replace master arena with working copy
        Arena copy = new Arena(workingArena.getId(), workingArena.getDisplayName());
        copy.setMinPlayers(workingArena.getMinPlayers());
        copy.setMaxPlayers(workingArena.getMaxPlayers());
        copy.setTimeToShrinkSeconds(workingArena.getTimeToShrinkSeconds());
        copy.setCenterSize(workingArena.getCenterSize());
        copy.setGracePeriodSeconds(workingArena.getGracePeriodSeconds());
        copy.setChestRefillSeconds(workingArena.getChestRefillSeconds());
        copy.setLobbyLocation(workingArena.getLobbyLocation());
        for (String s : workingArena.getSpawnOffsets()) copy.addSpawnOffset(s);
        arenas.put(id, copy);
        // Persist to disk
        saveArenas();
    }

    /**
     * Add a spawn point to the working arena (if loaded). Returns true on success.
     */
    public boolean addSpawnPointToWorking(Location loc) {
        if (workingArena == null) return false;
        Location center = workingArena.getLobbyLocation();
        if (center == null) return false;
        workingArena.addSpawnRelative(center, loc);
        return true;
    }

    /**
     * Remove a spawn point by index from the working arena. Returns true on success.
     */
    public boolean removeSpawnPointFromWorking(int index) {
        if (workingArena == null) return false;
        if (index < 0 || index >= workingArena.getSpawnOffsets().size()) return false;
        workingArena.removeSpawn(index);
        return true;
    }
}
