package io.github.KennedySovine.hungerGames.gui;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaEditorGui {

    public static final String SPAWN_STICK_KEY_NAME = "hg_arena_id";
    public static final String SET_CENTER_DISPLAY = "Set Center";

    /**
     * Public opener: builds the editor inventory and opens it for the player.
     * arenaId may be null to indicate create/load mode.
     */
    public void openEditor(Player player, String arenaId) {
        Inventory inv = buildInventory(arenaId);
        player.openInventory(inv);

        // Mark the player as having the Arena Editor open so listeners can detect state.
        JavaPlugin plugin = JavaPlugin.getPlugin(HungerGames.class);
        player.setMetadata("ArenaEditorGUI", new FixedMetadataValue(plugin, true));
    }

    /**
     * Build the inventory instance for the editor. This is a pure builder and
     * does not perform any player interactions.
     */
    private Inventory buildInventory(String arenaId) {
        EditorHolder holder = new EditorHolder(arenaId);
        String title = createEditorTitle(arenaId);
        Inventory inv;

        ItemStack createArena = new ItemStack(Material.BLUE_WOOL);
        ItemMeta createMeta = createArena.getItemMeta();
        createMeta.setDisplayName("Create Arena");
        createArena.setItemMeta(createMeta);

        ItemStack loadArena = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta loadMeta = loadArena.getItemMeta();
        loadMeta.setDisplayName("Load Arena");
        loadArena.setItemMeta(loadMeta);

        ItemStack saveArena = new ItemStack(Material.GREEN_WOOL);
        ItemMeta saveMeta = saveArena.getItemMeta();
        saveMeta.setDisplayName("Save Arena");
        saveArena.setItemMeta(saveMeta);

        ItemStack addSpawn = new ItemStack(Material.OAK_SIGN);
        ItemMeta addMeta = addSpawn.getItemMeta();
        addMeta.setDisplayName("Add Spawn Point");
        addSpawn.setItemMeta(addMeta);

        ItemStack removeSpawn = new ItemStack(Material.BIRCH_SIGN);
        ItemMeta removeMeta = removeSpawn.getItemMeta();
        removeMeta.setDisplayName("Remove Spawn Point");
        removeSpawn.setItemMeta(removeMeta);

        ItemStack giveSpawnStick = new ItemStack(Material.STICK);
        ItemMeta stickMeta = giveSpawnStick.getItemMeta();
        stickMeta.setDisplayName("Give Spawn Stick");
        giveSpawnStick.setItemMeta(stickMeta);

        ItemStack setCenter = new ItemStack(Material.COMPASS);
        ItemMeta setCenterMeta = setCenter.getItemMeta();
        setCenterMeta.setDisplayName(SET_CENTER_DISPLAY);
        setCenter.setItemMeta(setCenterMeta);
        // Add other items as needed for the other admin commands, if its even necessary for the other commands.

        // If there is no currently loaded arena, we are in create/load mode
        if (arenaId == null || arenaId.isEmpty()){
            inv = Bukkit.createInventory(holder, 9, title);
            inv.setItem(2, createArena);
            inv.setItem(6, loadArena);
        }
        else {
            inv = Bukkit.createInventory(holder, 18, title);
            inv.setItem(1, addSpawn);
            inv.setItem(4, giveSpawnStick);
            inv.setItem(7, removeSpawn);
            inv.setItem(2, setCenter);
            inv.setItem(12, createArena);
            inv.setItem(13, saveArena);
            inv.setItem(14, loadArena);
        }
        holder.setInventory(inv);

        return inv;
    }

    public void giveSpawnStick(Player admin, String arenaId) {
        // Give a stick with PDC metadata so SpawnStickListener can detect it.
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName("Spawn Stick - " + (arenaId == null ? "(none)" : arenaId));
        // Store arena id in persistent data container so listener can read it
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(JavaPlugin.getPlugin(HungerGames.class), SPAWN_STICK_KEY_NAME);
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, arenaId == null ? "" : arenaId);
        stick.setItemMeta(meta);
        admin.getInventory().addItem(stick);
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
