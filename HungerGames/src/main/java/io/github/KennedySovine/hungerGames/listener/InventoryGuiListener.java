package io.github.KennedySovine.hungerGames.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Listener for GUI inventories. Prevents item movement and delegates actions
 * to the GUI controller classes.
 */
public class InventoryGuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // TODO: detect our plugin's GUI inventories and delegate handling.
    }
}

