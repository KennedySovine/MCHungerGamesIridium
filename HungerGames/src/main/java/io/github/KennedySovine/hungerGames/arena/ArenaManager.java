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
}
