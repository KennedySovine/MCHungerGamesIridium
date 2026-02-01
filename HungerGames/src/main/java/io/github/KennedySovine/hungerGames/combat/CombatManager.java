package io.github.KennedySovine.hungerGames.combat;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * CombatManager skeleton which tracks last-damage timestamps and pending
 * disconnects. This class implements the combat-logging rules described in
 * the project TODO: 10s combat window and 120s grace period for non-combat disconnects.
 *
 * It intentionally contains only high-level logic and hooks; detailed game
 * death handling should be delegated to GameManager and StatsManager.
 */
public class CombatManager {

    private final HungerGames plugin;
    private final Map<UUID, Long> lastDamage = new ConcurrentHashMap<>();

    // Tracks pending grace expirations: player UUID -> scheduled task id
    private final Map<UUID, ScheduledFuture<?>> pendingGraceTasks = new ConcurrentHashMap<>();

    // Default values; make configurable via config.yml later
    private long combatWindowMs = 10_000;
    private long defaultGraceMs = 120_000;

    public CombatManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Record a damage event for both attacker and victim (marks both as recently in-combat).
     */
    public void recordDamage(UUID attacker, UUID victim) {
        long now = System.currentTimeMillis();
        if (attacker != null) lastDamage.put(attacker, now);
        if (victim != null) lastDamage.put(victim, now);
    }

    /**
     * Returns true if the player is considered in combat (within the combatWindowMs).
     */
    public boolean isInCombat(UUID player) {
        Long t = lastDamage.get(player);
        return t != null && (System.currentTimeMillis() - t) <= combatWindowMs;
    }

    /**
     * Handle a player quitting the server. If in combat -> immediately mark as dead via GameManager.
     * Otherwise schedule a grace expiry task which will mark the player dead if they do not return in time.
     */
    public void handleQuit(UUID playerUuid) {
        if (playerUuid == null) return;

        // Cancel any previous pending task for safety
        BukkitTask prev = (BukkitTask) pendingGraceTasks.remove(playerUuid);
        if (prev != null) prev.cancel();

        if (isInCombat(playerUuid)) {
            // Immediately treat as death: delegate to GameManager
            GameManager gm = plugin.getGameManager();
            StatsManager sm = plugin.getStatsManager();
            // Best-effort: run on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                // The GameManager should provide a method to handle player death-by-logout
                gm.handlePlayerCombatLogout(playerUuid);
                // Record death in stats manager (if available)
                sm.recordDeath(playerUuid, "combat-logout");
            });
            return;
        }

        // Not in combat: schedule grace expiry on main thread after computed ticks
        long ticks = Math.max(1L, defaultGraceMs / 50L); // approximate ms->ticks
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // When grace expires, mark player dead
            GameManager gm = plugin.getGameManager();
            StatsManager sm = plugin.getStatsManager();
            gm.handlePlayerGraceExpired(playerUuid);
            sm.recordDeath(playerUuid, "disconnected-grace-expired");
            pendingGraceTasks.remove(playerUuid);
        }, ticks);

        pendingGraceTasks.put(playerUuid, (ScheduledFuture<?>) task);
    }

    /**
     * Handle a player joining the server. Cancel pending grace and restore state if applicable.
     */
    public void handleJoin(UUID playerUuid) {
        if (playerUuid == null) return;
        // Cancel pending kill if present
        BukkitTask task = (BukkitTask) pendingGraceTasks.remove(playerUuid);
        if (task != null) {
            task.cancel();
            // Restore player state via GameManager (best-effort on main thread)
            Bukkit.getScheduler().runTask(plugin, () -> {
                GameManager gm = plugin.getGameManager();
                gm.handlePlayerReconnected(playerUuid);
                // Notify player if they're considered dead (gm will handle sending death UI if needed)
                Player p = Bukkit.getPlayer(playerUuid);
                if (p != null) {
                    p.sendMessage("Welcome back. Your reconnect grace was applied.");
                }
            });
        }
    }
}
