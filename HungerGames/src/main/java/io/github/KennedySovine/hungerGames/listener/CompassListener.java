package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.gui.SpectatorGui;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener that handles compass right-click to open the spectator menu.
 */
public class CompassListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if player is holding the spectator compass
        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !"§6§lSpectator Menu".equals(meta.getDisplayName())) {
            return;
        }

        // Cancel the event to prevent normal compass behavior
        event.setCancelled(true);

        // Only spectators should be able to use this
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        // Open the spectator menu
        SpectatorGui.openFor(player);
    }
}
