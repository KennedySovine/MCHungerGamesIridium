package io.github.KennedySovine.hungerGames.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Simple YAML storage helper for plugin data files.
 *
 * Responsibilities:
 * - Load a FileConfiguration for a named YAML file under the plugin data folder.
 * - Save a FileConfiguration to disk, ensuring saves happen on the main thread.
 * - Use a temporary file and atomic move when writing to reduce corruption risk.
 *
 * Usage:
 * YamlStorage storage = new YamlStorage(plugin);
 * FileConfiguration cfg = storage.load("arenas.yml");
 * // modify cfg
 * storage.save("arenas.yml", cfg);
 */
public class YamlStorage {
    private final JavaPlugin plugin;

    /**
     * Create a new YamlStorage bound to the plugin's data folder.
     */
    public YamlStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        // Ensure data folder exists
        File data = plugin.getDataFolder();
        if (data != null && !data.exists()) {
            data.mkdirs();
        }
    }

    /**
     * Load a {@link FileConfiguration} from the given file name (relative to the plugin data folder).
     * If the file does not exist an empty {@link YamlConfiguration} instance is returned.
     *
     * @param filename filename or path (e.g. "arenas.yml" or "sub/arenas.yml").
     * @return loaded FileConfiguration (never null)
     */
    public FileConfiguration load(String filename) {
        File file = getFile(filename);
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Save the provided {@link FileConfiguration} to disk under the given file name.
     * If called off the main thread the actual write will be scheduled on the main thread.
     *
     * @param filename filename or path (e.g. "arenas.yml")
     * @param cfg      the FileConfiguration to persist
     */
    public void save(String filename, FileConfiguration cfg) {
        File file = getFile(filename);
        if (Bukkit.isPrimaryThread()) {
            try {
                saveToFile(cfg, file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save configuration to " + file.getAbsolutePath(), e);
            }
        } else {
            // schedule to main thread to perform the save
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    saveToFile(cfg, file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save configuration to " + file.getAbsolutePath(), e);
                }
            });
        }
    }

    /**
     * Helper which writes to a temporary file and then atomically moves it into place.
     */
    private void saveToFile(FileConfiguration cfg, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tmp = new File(parent, file.getName() + ".tmp");
        // Write to tmp
        try {
            cfg.save(tmp);
        } catch (IOException e) {
            // If write fails, attempt to delete tmp and rethrow
            if (tmp.exists()) {
                tmp.delete();
            }
            throw e;
        }

        // Move tmp -> file atomically when possible
        try {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            // Fallback to non-atomic replace if atomic move not supported
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Return a {@link File} for the given plugin-relative filename. Ensures parent directories exist.
     *
     * @param filename filename (with or without .yml extension)
     */
    public File getFile(String filename) {
        if (!filename.endsWith(".yml")) {
            filename = filename + ".yml";
        }
        File file = new File(plugin.getDataFolder(), filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }
}

