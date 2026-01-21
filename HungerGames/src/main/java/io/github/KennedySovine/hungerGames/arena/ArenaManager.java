package io.github.KennedySovine.hungerGames.arena;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.storage.YamlStorage;
import org.bukkit.Bukkit;
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
     */
    public void loadArenas() {
        // TODO: implement loading using YamlStorage
    }

    /**
     * Save arenas to arenas.yml. Should be invoked after changes.
     */
    public void saveArenas() {
        // TODO: implement saving using YamlStorage
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
}

