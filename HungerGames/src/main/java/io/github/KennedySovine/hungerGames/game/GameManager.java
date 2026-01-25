package io.github.KennedySovine.hungerGames.game;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skeleton GameManager to manage game lifecycle and player participation.
 *
 * The implementation should manage per-arena games, schedules for chest refills
 * and border shrink, and provide join/leave APIs used by commands.
 *
 * This class now contains lightweight tracking used by CombatManager hooks
 * (handlePlayerCombatLogout/handlePlayerGraceExpired/handlePlayerReconnected).
 */
public class GameManager {

    private final HungerGames plugin;
    private final Map<String, GameState> games = new ConcurrentHashMap<>();

    // arenaId -> set of player UUIDs participating (lobby or running)
    private final Map<String, Set<UUID>> arenaPlayers = new ConcurrentHashMap<>();

    // player UUID -> arenaId they are currently associated with
    private final Map<UUID, String> playerArena = new ConcurrentHashMap<>();

    // simple inventory-save map used during join/leave flow (implementors should
    // replace with a proper InventorySnapshot class later)
    private final Map<UUID, ItemStack[]> savedInventories = new ConcurrentHashMap<>();

    public GameManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Start a game for the given arena. Minimal behavior: record state and broadcast.
     */
    public void startGame(String arenaId) {
        // TODO: schedule countdown -> running transitions and chest/border tasks
        games.put(arenaId, GameState.COUNTDOWN);
        Bukkit.broadcastMessage("[HG] Starting game for arena: " + arenaId);
    }

    /**
     * Stop a running game and perform minimal cleanup.
     */
    public void stopGame(String arenaId) {
        games.put(arenaId, GameState.FINISHED);
        // Remove all tracked players from this arena and restore any saved inventories
        Set<UUID> players = arenaPlayers.remove(arenaId);
        if (players != null) {
            for (UUID u : players) {
                playerArena.remove(u);
                // Attempt to restore inventory if player is online
                Player p = Bukkit.getPlayer(u);
                ItemStack[] saved = savedInventories.remove(u);
                if (p != null && saved != null) {
                    p.getInventory().setContents(saved);
                    p.sendMessage("[HG] Game stopped - your inventory has been restored.");
                }
            }
        }
        Bukkit.broadcastMessage("[HG] Stopped game for arena: " + arenaId);
    }

    /**
     * Add a player to an arena's lobby. Minimal behavior: save inventory, clear it, and track the player.
     */
    public void join(Player player, String arenaId) {
        if (player == null || arenaId == null) return;
        UUID u = player.getUniqueId();
        // Save inventory snapshot so it can be restored later
        savedInventories.put(u, player.getInventory().getContents());
        // Clear inventory to prepare for match (real implementation may give lobby items)
        player.getInventory().clear();

        arenaPlayers.computeIfAbsent(arenaId, k -> ConcurrentHashMap.newKeySet()).add(u);
        playerArena.put(u, arenaId);

        player.sendMessage("[HG] You joined arena: " + arenaId + " (skeleton behavior). Implement teleport/inventory logic later.");
    }

    /**
     * Remove a player from their current arena and restore inventory where possible.
     */
    public void leave(Player player) {
        if (player == null) return;
        UUID u = player.getUniqueId();
        String arenaId = playerArena.remove(u);
        if (arenaId != null) {
            Set<UUID> set = arenaPlayers.get(arenaId);
            if (set != null) set.remove(u);
        }
        ItemStack[] saved = savedInventories.remove(u);
        if (saved != null) {
            player.getInventory().setContents(saved);
        }
        player.sendMessage("[HG] You have left the arena (skeleton behavior).");
    }

    public GameState getGameState(String arenaId) {
        return games.getOrDefault(arenaId, GameState.FINISHED);
    }

    // --- Hooks used by CombatManager (minimal implementations) ---

    /**
     * Called when CombatManager determines a player disconnected while in combat.
     * Treat as an immediate death: remove player from arena bookkeeping and notify.
     */
    public void handlePlayerCombatLogout(UUID playerUuid) {
        if (playerUuid == null) return;
        String arenaId = playerArena.remove(playerUuid);
        if (arenaId != null) {
            Set<UUID> set = arenaPlayers.get(arenaId);
            if (set != null) set.remove(playerUuid);
        }
        // If player is online, show death UI/message. Otherwise log.
        Player p = Bukkit.getPlayer(playerUuid);
        if (p != null) {
            p.sendMessage("You have been killed due to combat logging.");
            // TODO: present death/spectator UI
        } else {
            plugin.getLogger().info("Player " + playerUuid + " was killed due to combat logout in arena " + arenaId);
        }
    }

    /**
     * Called when a non-combat disconnect's grace period expires. Treat as death.
     */
    public void handlePlayerGraceExpired(UUID playerUuid) {
        if (playerUuid == null) return;
        String arenaId = playerArena.remove(playerUuid);
        if (arenaId != null) {
            Set<UUID> set = arenaPlayers.get(arenaId);
            if (set != null) set.remove(playerUuid);
        }
        plugin.getLogger().info("Player " + playerUuid + " grace expired and was marked dead.");
        // Optionally notify on next login; CombatManager/PlayerConnectionListener may handle that.
    }

    /**
     * Called when a player reconnects and their pending grace was cancelled.
     * Attempt to restore their state or notify that they are dead.
     */
    public void handlePlayerReconnected(UUID playerUuid) {
        if (playerUuid == null) return;
        Player p = Bukkit.getPlayer(playerUuid);
        if (p == null) return;

        // If we still have a saved inventory, restore it (simple behavior)
        ItemStack[] saved = savedInventories.remove(playerUuid);
        if (saved != null) {
            p.getInventory().setContents(saved);
            p.sendMessage("[HG] Welcome back — your inventory was restored (skeleton behavior).");
            return;
        }

        // Otherwise, inform they were killed or need manual handling
        p.sendMessage("[HG] Welcome back. If you were marked dead while offline, you'll see spectator/death UI (to be implemented).");
    }
}
