package io.github.KennedySovine.hungerGames.gui;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Simple GUI that lists known arenas and allows an admin to click one to load it
 * into the working arena. Clicking an arena will load it and set its center to
 * the clicking player's current location (consistent with /hg arena load behavior).
 */
public class ArenaListGui {

    public static class ListHolder implements InventoryHolder {
        private final Inventory inv;

        public ListHolder(Inventory inv) {
            this.inv = inv;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    public static void openFor(Player player) {
        List<String> names = HungerGames.getPlugin(HungerGames.class).getArenaManager().getArenaNames();
        if (names.isEmpty()) {
            MessageUtils.send(player, "&eNo arenas are defined.");
            return;
        }
        int size = Math.min(54, ((names.size() + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(new ListHolder(null), size, "Load Arena");
        for (int i = 0; i < names.size(); i++) {
            String id = names.get(i);
            ItemStack it = new ItemStack(Material.PAPER);
            ItemMeta m = it.getItemMeta();
            m.setDisplayName(id);
            it.setItemMeta(m);
            inv.setItem(i, it);
        }
        ListHolder holder = new ListHolder(inv);
        player.openInventory(inv);
    }
}

