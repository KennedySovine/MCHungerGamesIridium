package io.github.KennedySovine.hungerGames.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ArenaEditorGui {

    public static final String SPAWN_STICK_KEY_NAME = "hg_arena_id";

    /**
     * Public opener: builds the editor inventory and opens it for the player.
     * arenaId may be null to indicate create/load mode.
     */
    public void openEditor(Player player, String arenaId) {
        Inventory inv = buildInventory(arenaId);
        player.openInventory(inv);
    }

    /**
     * Build the inventory instance for the editor. This is a pure builder and
     * does not perform any player interactions.
     */
    private Inventory buildInventory(String arenaId) {
        EditorHolder holder = new EditorHolder(arenaId);
        String title = createEditorTitle(arenaId);
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inv);
        return inv;
    }

    public void giveSpawnStick(Player admin, String arenaId) {
        // Implement when ready
    }

    private String createEditorTitle(String arenaId) {
        return (arenaId == null || arenaId.isEmpty()) ? "Arena Editor" : "Arena Editor - " + arenaId;
    }

    public static class EditorHolder implements InventoryHolder {
        private final String arenaId;
        private Inventory inventory;

        public EditorHolder(String arenaId) {
            this.arenaId = arenaId;
        }

        public String getArenaId() {
            return arenaId;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
