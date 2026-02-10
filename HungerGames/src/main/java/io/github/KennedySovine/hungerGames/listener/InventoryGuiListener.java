package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Listener for GUI inventories. Prevents item movement and delegates actions
 * to the GUI controller classes.
 */
public class InventoryGuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof ArenaEditorGui.EditorHolder)) return;

        // We handle clicks in our editor; cancel default behavior
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String disp = clicked.getItemMeta().getDisplayName();
        Player clicker = (Player) event.getWhoClicked();

        // Handle Set Center click
        if (ArenaEditorGui.SET_CENTER_DISPLAY.equals(disp)) {
            // Set the working arena center to this player's location
            ArenaManager mgr = HungerGames.getPlugin(HungerGames.class).getArenaManager();
            Optional<io.github.KennedySovine.hungerGames.arena.Arena> wa = mgr.getWorkingArena();
            if (wa.isEmpty()) {
                MessageUtils.send(clicker, "&cNo working arena loaded. Load or create one first.");
                return;
            }
            wa.get().setLobbyLocation(clicker.getLocation());
            MessageUtils.send(clicker, "&aSet working arena center to your current location.");
            return;
        }

        // Other GUI actions (create, load, save, add spawn, etc.) will be handled elsewhere.
    }
}
