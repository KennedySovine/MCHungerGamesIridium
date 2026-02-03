package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.Inventory;

/**
 * Listener for GUI inventories. Prevents item movement and delegates actions
 * to the GUI controller classes.
 */
public class InventoryGuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        Inventory top = event.getView().getTopInventory();
        boolean isEditorByHolder = top.getHolder() instanceof ArenaEditorGui.EditorHolder;
        boolean isEditorByMeta = player.hasMetadata("ArenaEditorGUI");
        if (!isEditorByHolder && !isEditorByMeta) return;

        // Only handle clicks inside the GUI (top inventory). Allow clicks in the player's own inventory.
        if (event.getRawSlot() >= top.getSize()) {
            return;
        }

        // Prevent any item movement in the Arena Editor GUI as early as possible
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        if (!clickedItem.hasItemMeta()) return;
        if (clickedItem.getItemMeta().getDisplayName() == null) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        switch (itemName) {
            case "Create Arena":
                player.sendMessage("Create Arena clicked!");
                break;
            case "Load Arena":
                player.sendMessage("Load Arena clicked!");
                break;
            case "Save Arena":
                player.sendMessage("Save Arena clicked!");
                break;
            case "Add Spawn Point":
                player.sendMessage("Add Spawn Point clicked!");
                break;
            case "Remove Spawn Point":
                player.sendMessage("Remove Spawn Point clicked!");
                break;
            case "Give Spawn Stick":
                player.sendMessage("Give Spawn Stick clicked!");
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof ArenaEditorGui.EditorHolder) && !player.hasMetadata("ArenaEditorGUI")) return;

        // Cancel drags that touch the top inventory slots
        for (int slot : event.getRawSlots()) {
            if (slot < top.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof ArenaEditorGui.EditorHolder) {
            // Clear metadata flag if present
            player.removeMetadata("ArenaEditorGUI", JavaPlugin.getPlugin(HungerGames.class));
        }
    }
}
