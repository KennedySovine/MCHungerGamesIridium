package io.github.KennedySovine.hungerGames.gui;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.game.GameState;
import io.github.KennedySovine.hungerGames.spectator.SpectatorManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Spectator GUI that provides different menus based on game state:
 * - LOBBY/COUNTDOWN: Shows JOIN and SPECTATE options
 * - RUNNING: Shows player heads to spectate and queue option
 */
public class SpectatorGui {

    /**
     * Custom inventory holder to identify spectator GUI inventories.
     */
    public static class SpectatorHolder implements InventoryHolder {
        private Inventory inventory;
        private final GameState state;

        public SpectatorHolder(GameState state) {
            this.state = state;
        }
        
        public void setInventory(Inventory inv) {
            this.inventory = inv;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public GameState getState() {
            return state;
        }
    }

    /**
     * Opens the appropriate spectator menu for the player based on current game state.
     */
    public static void openFor(Player spectator) {
        HungerGames plugin = HungerGames.getPlugin(HungerGames.class);
        GameManager gameManager = plugin.getGameManager();
        ArenaManager arenaManager = plugin.getArenaManager();

        // Get the working arena
        Optional<Arena> arenaOpt = arenaManager.getWorkingArena();
        if (arenaOpt.isEmpty()) {
            spectator.sendMessage("§cNo arena is currently active!");
            return;
        }

        Arena arena = arenaOpt.get();
        String arenaId = arena.getId();
        GameState state = gameManager.getGameState(arenaId);

        // Open different GUIs based on state
        if (state == GameState.LOBBY || state == GameState.COUNTDOWN) {
            openLobbyMenu(spectator, arena, gameManager, plugin);
        } else if (state == GameState.RUNNING || state == GameState.DEATHMATCH) {
            openRunningMenu(spectator, arena, gameManager, plugin);
        } else {
            spectator.sendMessage("§cNo game is currently active!");
        }
    }

    /**
     * Opens the LOBBY menu with JOIN and SPECTATE options.
     */
    private static void openLobbyMenu(Player spectator, Arena arena, GameManager gameManager, HungerGames plugin) {
        // Create holder and inventory
        SpectatorHolder holder = new SpectatorHolder(GameState.LOBBY);
        Inventory inv = Bukkit.createInventory(holder, 9, "§6Spectator Menu");
        holder.setInventory(inv);

        int playerCount = gameManager.getPlayerCount(arena.getId());
        int maxPlayers = arena.getMaxPlayers();

        // JOIN item (slot 3)
        ItemStack joinItem = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta joinMeta = joinItem.getItemMeta();
        if (joinMeta != null) {
            joinMeta.setDisplayName("§a§lJOIN");
            List<String> joinLore = new ArrayList<>();
            joinLore.add("§7Click to join the game");
            joinLore.add("§7Players: §e" + playerCount + "§7/§e" + maxPlayers);
            joinMeta.setLore(joinLore);
            joinItem.setItemMeta(joinMeta);
        }
        inv.setItem(3, joinItem);

        // SPECTATE item (slot 5)
        ItemStack spectateItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta spectateMeta = spectateItem.getItemMeta();
        if (spectateMeta != null) {
            spectateMeta.setDisplayName("§b§lSPECTATE");
            List<String> spectateLore = new ArrayList<>();
            spectateLore.add("§7Continue spectating");
            spectateMeta.setLore(spectateLore);
            spectateItem.setItemMeta(spectateMeta);
        }
        inv.setItem(5, spectateItem);

        spectator.openInventory(inv);
    }

    /**
     * Opens the RUNNING menu with player heads and queue option.
     */
    private static void openRunningMenu(Player spectator, Arena arena, GameManager gameManager, HungerGames plugin) {
        String arenaId = arena.getId();
        
        // Get all players in the arena (alive players)
        Set<UUID> alivePlayers = gameManager.getArenaPlayers(arenaId);
        List<Player> onlinePlayers = new ArrayList<>();
        
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                onlinePlayers.add(p);
            }
        }

        // Calculate inventory size (must be multiple of 9)
        int playerSlots = onlinePlayers.size();
        int rows = Math.max(1, (int) Math.ceil(playerSlots / 9.0)); // At least 1 row
        int size = rows * 9;

        // Create holder and inventory
        SpectatorHolder holder = new SpectatorHolder(GameState.RUNNING);
        Inventory inv = Bukkit.createInventory(holder, size, "§6Spectator Menu - Players");
        holder.setInventory(inv);

        // Add player heads
        for (int i = 0; i < onlinePlayers.size() && i < size; i++) {
            Player target = onlinePlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (head.getItemMeta() instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                skullMeta.setOwningPlayer(target);
                skullMeta.setDisplayName("§e" + target.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Click to spectate this player");
                skullMeta.setLore(lore);
                head.setItemMeta(skullMeta);
            }
            inv.setItem(i, head);
        }

        spectator.openInventory(inv);
    }
}

