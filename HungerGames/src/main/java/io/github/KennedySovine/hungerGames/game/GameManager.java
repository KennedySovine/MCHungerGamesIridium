package io.github.KennedySovine.hungerGames.game;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
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
    
    // arenaId -> next spawn index to assign (using AtomicInteger for thread safety)
    private final Map<String, java.util.concurrent.atomic.AtomicInteger> nextSpawnIndex = new ConcurrentHashMap<>();

    public GameManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Start a game for the given arena (opens it for players to join).
     * Sets the game state to COUNTDOWN. Players can join but cannot move.
     */
    public void startGame(String arenaId) {
        // TODO: schedule countdown -> running transitions and chest/border tasks
        games.put(arenaId, GameState.COUNTDOWN);
        Bukkit.broadcastMessage(MessageUtils.color("&a[HG] Game is now open for joining! Use /hg join to participate."));
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
                if (p != null) {
                    if (saved != null) {
                        p.getInventory().setContents(saved);
                    }
                    // Put non-admin players back in spectator mode
                    if (!p.hasPermission("HungerGames.admin") && !p.isOp()) {
                        p.setGameMode(GameMode.SPECTATOR);
                    }
                    MessageUtils.send(p, "[HG] Game stopped - your inventory has been restored.");
                }
            }
        }
        // Reset spawn index for next game
        nextSpawnIndex.remove(arenaId);
        Bukkit.broadcastMessage(MessageUtils.color("[HG] Stopped game for arena: " + arenaId));
    }

    /**
     * Add a player to an arena's game. Teleports them to a spawn point, saves inventory, and tracks them.
     * Returns true if successful, false if there are no spawn points available.
     */
    public boolean join(Player player, String arenaId, io.github.KennedySovine.hungerGames.arena.Arena arena) {
        if (player == null || arenaId == null || arena == null) return false;
        UUID u = player.getUniqueId();
        
        // Get spawn points
        List<Location> spawns = arena.getAbsoluteSpawns(arena.getLobbyLocation());
        if (spawns.isEmpty()) {
            MessageUtils.send(player, "&cNo spawn points configured for this arena!");
            return false;
        }
        
        // Get next available spawn point atomically (thread-safe)
        java.util.concurrent.atomic.AtomicInteger counter = nextSpawnIndex.computeIfAbsent(arenaId, 
            k -> new java.util.concurrent.atomic.AtomicInteger(0));
        int spawnIdx = counter.getAndIncrement() % spawns.size();
        Location spawnLoc = spawns.get(spawnIdx);
        
        // Save inventory snapshot so it can be restored later
        savedInventories.put(u, player.getInventory().getContents());
        // Clear inventory to prepare for match
        player.getInventory().clear();

        arenaPlayers.computeIfAbsent(arenaId, k -> ConcurrentHashMap.newKeySet()).add(u);
        playerArena.put(u, arenaId);
        
        // Teleport player to spawn
        player.teleport(spawnLoc);
        
        // Set player to survival mode (remove from spectator)
        player.setGameMode(GameMode.SURVIVAL);
        
        MessageUtils.send(player, "&aYou joined the game! Wait for the game to begin...");
        
        // Check if we should auto-begin the game
        if (getPlayerCount(arenaId) >= arena.getMaxPlayers()) {
            beginGame(arenaId);
        }
        
        return true;
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
        MessageUtils.send(player, "[HG] You have left the arena (skeleton behavior).");
    }

    public GameState getGameState(String arenaId) {
        return games.getOrDefault(arenaId, GameState.FINISHED);
    }
    
    /**
     * Get the arena ID for a player, or null if they are not in any arena.
     */
    public String getPlayerArena(UUID playerUuid) {
        return playerArena.get(playerUuid);
    }
    
    /**
     * Check if a player is in an arena.
     */
    public boolean isPlayerInArena(UUID playerUuid) {
        return playerArena.containsKey(playerUuid);
    }
    
    /**
     * Begin a game for the given arena (transition from COUNTDOWN to RUNNING).
     */
    public void beginGame(String arenaId) {
        GameState currentState = games.get(arenaId);
        if (currentState != GameState.COUNTDOWN) {
            plugin.getLogger().warning("Cannot begin game for arena " + arenaId + " - game is not in COUNTDOWN state");
            return;
        }
        games.put(arenaId, GameState.RUNNING);
        Bukkit.broadcastMessage(MessageUtils.color("&a[HG] Game has begun! Fight for survival!"));
    }
    
    /**
     * Get the number of players currently in an arena.
     */
    public int getPlayerCount(String arenaId) {
        Set<UUID> players = arenaPlayers.get(arenaId);
        return players != null ? players.size() : 0;
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
            MessageUtils.send(p, "You have been killed due to combat logging.");
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
            MessageUtils.send(p, "[HG] Welcome back — your inventory was restored (skeleton behavior).");
            return;
        }

        // Otherwise, inform they were killed or need manual handling
        MessageUtils.send(p, "[HG] Welcome back. If you were marked dead while offline, you'll see spectator/death UI (to be implemented).");
    }
}
