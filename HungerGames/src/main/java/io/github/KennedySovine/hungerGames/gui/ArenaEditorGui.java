package io.github.KennedySovine.hungerGames.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ArenaEditorGui {

    public static final String SPAWN_STICK_KEY_NAME = "hg_arena_id";

    public void openFor(Player player) {
        //
        Inventory gui = Bukkit.createInventory(new EditorHolder(null), 9, createEditorTitle());
        player.openInventory(gui);
    }

    public Inventory buildMainInventory(String arenaId) {
        return Bukkit.createInventory(new EditorHolder(arenaId), 27, createEditorTitle());
    }

    public void giveSpawnStick(Player admin, String arenaId) {
    }

    public static class EditorHolder implements InventoryHolder {
        private final String arenaId;

        public EditorHolder(String arenaId) {
            this.arenaId = arenaId;
        }

        public String getArenaId() {
            return arenaId;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
