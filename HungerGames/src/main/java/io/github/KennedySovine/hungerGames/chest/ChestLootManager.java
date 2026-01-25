package io.github.KennedySovine.hungerGames.chest;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Skeleton ChestLootManager.
 *
 * Responsibilities:
 * - Load a `chest-loot` mapping from the plugin config (see src/main/resources/config.yml).
 * - Provide a `populateChest(Inventory inv)` method which fills an Inventory using the configured loot.
 *
 * This class intentionally contains only loading/lookup helpers and a public API; implementors
 * should complete the weighted-random mechanics and ItemStack construction later.
 */
public class ChestLootManager {

    private final org.bukkit.plugin.Plugin plugin;
    private final List<Object> loot = new ArrayList<>(); // placeholder for ChestItem objects

    public ChestLootManager(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload loot definitions from the config.yml (`chest-loot` section).
     */
    public void reload() {
        FileConfiguration cfg = plugin.getConfig();
        // TODO: parse cfg.getConfigurationSection("chest-loot") into ChestItem instances
    }

    /**
     * Populate the provided chest inventory with loot according to the loaded table.
     * This is intentionally a stub; later implementation should choose weighted items and
     * place ItemStacks into random slots.
     */
    public void populateChest(Inventory inv) {
        // TODO: Implement population logic using weighted random selection of parsed ChestItem objects
        // For now, clear the inventory to avoid leaving stale items.
        inv.clear();
    }
}

