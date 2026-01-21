package io.github.KennedySovine.hungerGames.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listener for admin spawn-stick interactions. The spawn-stick should contain
 * plugin-specific metadata (PersistentDataContainer) to identify which arena
 * it is associated with. Right-click should add a spawn point.
 */
public class SpawnStickListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // TODO: check for PDC metadata on the held item and add spawn via ArenaManager
    }
}

