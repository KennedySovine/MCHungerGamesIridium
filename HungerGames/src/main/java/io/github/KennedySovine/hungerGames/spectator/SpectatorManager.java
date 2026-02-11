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
 * - Tracking the join queue for the next game
 * - Tracking which player each spectator is currently watching
 */
public class SpectatorManager {

    private final HungerGames plugin;
    
    // Queue for players waiting to join the next game (max 100, FIFO)
    // Using LinkedList with synchronized blocks instead of ConcurrentLinkedQueue
    // because we need size limits, contains() checks, and indexOf() for position tracking
    private final LinkedList<UUID> joinQueue = new LinkedList<>();
    private static final int MAX_QUEUE_SIZE = 100;
    
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
     * Adds a player to the join queue for the next game.
     * @return true if successfully added, false if queue is full or player already in queue
     */
    public boolean addToQueue(UUID playerUuid) {
        synchronized (joinQueue) {
            if (joinQueue.contains(playerUuid)) {
                return false; // Already in queue
            }
            if (joinQueue.size() >= MAX_QUEUE_SIZE) {
                return false; // Queue is full
            }
            joinQueue.addLast(playerUuid);
            return true;
        }
    }

    /**
     * Removes a player from the join queue.
     */
    public void removeFromQueue(UUID playerUuid) {
        synchronized (joinQueue) {
            joinQueue.remove(playerUuid);
        }
    }

    /**
     * Checks if a player is in the join queue.
     */
    public boolean isInQueue(UUID playerUuid) {
        synchronized (joinQueue) {
            return joinQueue.contains(playerUuid);
        }
    }

    /**
     * Gets the position of a player in the queue (1-indexed).
     * @return position in queue, or -1 if not in queue
     */
    public int getQueuePosition(UUID playerUuid) {
        synchronized (joinQueue) {
            int pos = joinQueue.indexOf(playerUuid);
            return pos >= 0 ? pos + 1 : -1;
        }
    }

    /**
     * Gets the current queue size.
     */
    public int getQueueSize() {
        synchronized (joinQueue) {
            return joinQueue.size();
        }
    }

    /**
     * Gets the next player(s) from the queue to join a game.
     * @param count number of players to get
     * @return list of player UUIDs (may be fewer than requested if queue is smaller)
     */
    public List<UUID> getNextFromQueue(int count) {
        List<UUID> result = new ArrayList<>();
        synchronized (joinQueue) {
            for (int i = 0; i < count && !joinQueue.isEmpty(); i++) {
                result.add(joinQueue.removeFirst());
            }
        }
        return result;
    }

    /**
     * Clears the entire join queue.
     */
    public void clearQueue() {
        synchronized (joinQueue) {
            joinQueue.clear();
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

    /**
     * Gets the maximum queue size.
     */
    public int getMaxQueueSize() {
        return MAX_QUEUE_SIZE;
    }
}
