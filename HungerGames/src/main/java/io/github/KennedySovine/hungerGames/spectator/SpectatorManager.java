package io.github.KennedySovine.hungerGames.spectator;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages spectator-related functionality including:
 * - Giving spectators a compass item for navigation
 * - Tracking which player each spectator is currently watching
 */
public class SpectatorManager {

    private final HungerGames plugin;
    
    // Track which player each spectator is currently spectating (spectatorUUID -> targetPlayerUUID)
    private final Map<UUID, UUID> spectatorTargets = new ConcurrentHashMap<>();

    public SpectatorManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Gives a spectator compass to the player in their 9th inventory slot (index 8).
     * The compass can be right-clicked to open the spectator menu.
     */
    public void giveSpectatorCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lSpectator Menu");
            List<String> lore = new ArrayList<>();
            lore.add("§7Right-click to open menu");
            meta.setLore(lore);
            compass.setItemMeta(meta);
        }
        
        // Place in slot 9 (index 8)
        player.getInventory().setItem(8, compass);
    }

    /**
     * Removes the spectator compass from the player's inventory.
     */
    public void removeSpectatorCompass(Player player) {
        ItemStack item = player.getInventory().getItem(8);
        if (item != null && item.getType() == Material.COMPASS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && "§6§lSpectator Menu".equals(meta.getDisplayName())) {
                player.getInventory().setItem(8, null);
            }
        }
    }

    /**
     * Sets which player a spectator is currently watching.
     */
    public void setSpectatorTarget(UUID spectatorUuid, UUID targetPlayerUuid) {
        if (targetPlayerUuid == null) {
            spectatorTargets.remove(spectatorUuid);
        } else {
            spectatorTargets.put(spectatorUuid, targetPlayerUuid);
        }
    }

    /**
     * Gets the player UUID that a spectator is currently watching.
     * @return target player UUID, or null if not spectating anyone
     */
    public UUID getSpectatorTarget(UUID spectatorUuid) {
        return spectatorTargets.get(spectatorUuid);
    }

    /**
     * Clears the spectator target for a player.
     */
    public void clearSpectatorTarget(UUID spectatorUuid) {
        spectatorTargets.remove(spectatorUuid);
    }
}
