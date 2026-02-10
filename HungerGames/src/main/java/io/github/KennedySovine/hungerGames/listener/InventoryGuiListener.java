package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import io.github.KennedySovine.hungerGames.gui.ArenaListGui;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.NamespacedKey;
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
        ArenaEditorGui.EditorHolder eh = (ArenaEditorGui.EditorHolder) holder;
        String arenaId = eh.getArenaId();

        // Permission check
        if (!clicker.hasPermission("HungerGames.admin")) {
            MessageUtils.send(clicker, "&cYou do not have permission to use the Arena Editor.");
            return;
        }

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
            // update center beacon
            String wid = wa.get().getId();
            HungerGames.getPlugin(HungerGames.class).getParticleManager().showCenter(wid, clicker.getLocation());
            // refresh spawn beacons relative to new center
            HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid, wa.get());
            return;
        }

        // Other GUI actions
        switch (disp) {
            case "Give Spawn Stick":
                if (arenaId == null || arenaId.isEmpty()) {
                    MessageUtils.send(clicker, "&cNo working arena loaded to bind the spawn stick to.");
                    return;
                }
                ArenaEditorGui gui = new ArenaEditorGui();
                gui.giveSpawnStick(clicker, arenaId);
                MessageUtils.send(clicker, "&aGave you a spawn stick bound to arena " + arenaId + ". Right-click to add spawns.");
                return;
            case "Add Spawn Point":
                // Delegate to existing command (player form)
                clicker.performCommand("hg arena addspawn");
                return;
            case "Remove Spawn Point":
                clicker.performCommand("hg arena removespawn");
                return;
            case "Save Arena":
                clicker.performCommand("hg arena save");
                return;
            case "Load Arena":
                // open list GUI to pick an arena to load
                ArenaListGui.openFor(clicker);
                return;
            case "Create Arena":
                // Creating requires a name; instruct the admin to run the command in chat.
                MessageUtils.send(clicker, "&eTo create a new arena run: &7/hg arena create <arenaId> [displayName]");
                return;
            default:
                // unknown item, ignore
                return;
        }
    }
}
