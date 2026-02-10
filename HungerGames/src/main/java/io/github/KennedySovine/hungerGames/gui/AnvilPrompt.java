package io.github.KennedySovine.hungerGames.gui;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

/**
 * Small helper to show an Anvil-style text prompt to a player.
 * The player types text in the anvil and clicks the result to confirm.
 */
public final class AnvilPrompt {
    private AnvilPrompt() {}

    public static class AnvilHolder implements InventoryHolder {
        private Inventory inv;
        public AnvilHolder() {}
        public void setInventory(Inventory inv) { this.inv = inv; }
        @Override public Inventory getInventory() { return inv; }
    }

    public static void open(Player player, String title, String initialText, Consumer<String> onComplete) {
        JavaPlugin plugin = JavaPlugin.getPlugin(HungerGames.class);
        // Some server API versions may not expose InventoryType.ANVIL; use a 3-slot inventory as a compatibility fallback
        Inventory inv = Bukkit.createInventory(new AnvilHolder(), 3, title == null ? "Input" : title);
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(initialText == null ? "" : initialText);
        it.setItemMeta(m);
        inv.setItem(0, it);
        AnvilHolder holder = new AnvilHolder();
        holder.setInventory(inv);

        Listener listener = new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!(e.getWhoClicked() instanceof Player)) return;
                Player p = (Player) e.getWhoClicked();
                if (!p.equals(player)) return;
                Inventory clickedInv = e.getInventory();
                if (!(clickedInv.getHolder() instanceof AnvilHolder)) return;
                e.setCancelled(true);
                // result slot is raw slot 2 for ANVIL inventory
                if (e.getRawSlot() == 2) {
                    ItemStack result = e.getCurrentItem();
                    String text = null;
                    if (result != null && result.hasItemMeta()) {
                        text = result.getItemMeta().getDisplayName();
                    }
                    try {
                        if (text != null && !text.isEmpty()) onComplete.accept(text);
                    } catch (Throwable ex) {
                        plugin.getLogger().warning("AnvilPrompt handler threw: " + ex.getMessage());
                    }
                    p.closeInventory();
                    HandlerList.unregisterAll(this);
                }
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (!(e.getPlayer() instanceof Player)) return;
                Player p = (Player) e.getPlayer();
                if (!p.equals(player)) return;
                Inventory closed = e.getInventory();
                if (!(closed.getHolder() instanceof AnvilHolder)) return;
                // player closed without confirming
                HandlerList.unregisterAll(this);
            }
        };

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        player.openInventory(inv);
    }
}
