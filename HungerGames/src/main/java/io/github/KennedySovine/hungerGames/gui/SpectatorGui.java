package io.github.KennedySovine.hungerGames.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Skeleton spectator GUI that will allow teleporting to alive players.
 */
public class SpectatorGui {

    public static class SpectatorHolder implements InventoryHolder {
        private Inventory inv;
        public SpectatorHolder(Inventory inv) { this.inv = inv; }
        @Override public Inventory getInventory() { return inv; }
    }

    public void openFor(Player spectator) {
        // TODO: build inventory listing alive players and open for spectator
        Inventory inv = Bukkit.createInventory(new SpectatorHolder(null), 9, "Spectator Menu");
        spectator.openInventory(inv);
    }
}
