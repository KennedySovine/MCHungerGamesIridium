package io.github.KennedySovine.hungerGames.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Skeleton for the Arena Editor GUI.
 *
 * Responsibilities:
 * - Build inventory menus for editing arena properties
 * - Handle clicks and delegate to ArenaManager (when implemented)
 * - Provide a method to give the spawn-stick to admins
 */
public class ArenaEditorGui {

    /**
     * Open the main arena editor GUI for the given player and arena id.
     */
    public void openFor(Player player, String arenaId) {
        // TODO: implement inventory creation and open
    }

    /**
     * Build the Inventory instance representing the main editor menu.
     */
    public Inventory buildMainInventory(String arenaId) {
        // TODO: create and return Inventory
        return null;
    }

    /**
     * Give the admin a spawn-stick that is associated with the arenaId.
     * The spawn-stick should use PersistentDataContainer to store arenaId.
     */
    public void giveSpawnStick(Player admin, String arenaId) {
        // TODO: create an ItemStack (STICK) with PDC metadata and give to player
    }
}

