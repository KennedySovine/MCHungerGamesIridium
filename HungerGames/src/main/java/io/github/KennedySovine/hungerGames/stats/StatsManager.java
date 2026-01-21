package io.github.KennedySovine.hungerGames.stats;

import io.github.KennedySovine.hungerGames.HungerGames;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skeleton StatsManager to store and retrieve PlayerStats. Persist to stats.yml.
 */
public class StatsManager {

    private final HungerGames plugin;
    private final Map<UUID, PlayerStats> stats = new ConcurrentHashMap<>();

    public StatsManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    public PlayerStats getStats(UUID player) {
        return stats.computeIfAbsent(player, PlayerStats::new);
    }

    public void addKill(UUID player) {
        getStats(player).addKill();
    }

    public void addDeath(UUID player) {
        getStats(player).addDeath();
    }

    public void addWin(UUID player) {
        getStats(player).addWin();
    }

    public void addLoss(UUID player) {
        getStats(player).addLoss();
    }

    public void save() {
        // TODO: persist stats to stats.yml
    }
}

