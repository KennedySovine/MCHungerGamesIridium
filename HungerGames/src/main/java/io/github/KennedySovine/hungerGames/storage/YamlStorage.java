package io.github.KennedySovine.hungerGames.storage;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Helper to load and save YAML files in the plugin data folder.
 *
 * This class performs simple sync reads/writes via Bukkit's Configuration API.
 * For large-scale data or heavy I/O, consider implementing an async serializer.
 */
public class YamlStorage {

    private final HungerGames plugin;

    public YamlStorage(HungerGames plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration load(String fileName) {
        File f = new File(plugin.getDataFolder(), fileName);
        if (!f.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                // If bundled resource exists, copy it
                InputStream def = plugin.getResource(fileName);
                if (def != null) {
                    Files.copy(def, f.toPath());
                } else {
                    f.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create default file: " + fileName);
            }
        }
        return YamlConfiguration.loadConfiguration(f);
    }

    public void save(String fileName, FileConfiguration cfg) {
        File f = new File(plugin.getDataFolder(), fileName);
        try {
            cfg.save(f);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save file: " + fileName + " -> " + e.getMessage());
        }
    }
}

