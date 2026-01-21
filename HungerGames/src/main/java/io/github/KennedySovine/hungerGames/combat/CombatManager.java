package io.github.KennedySovine.hungerGames.combat;

import io.github.KennedySovine.hungerGames.HungerGames;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skeleton CombatManager which tracks last-damage timestamps and pending
 * disconnects. Implementations should persist pending disconnects and
 * schedule grace expiration tasks.
 */
public class CombatManager {

    private final HungerGames plugin;
    private final Map<UUID, Long> lastDamage = new ConcurrentHashMap<>();

    // Default values; make configurable via config.yml
    private long combatWindowMs = 10_000;
    private long defaultGraceMs = 120_000;

    public CombatManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    public void recordDamage(UUID attacker, UUID victim) {
        long now = System.currentTimeMillis();
        lastDamage.put(attacker, now);
        lastDamage.put(victim, now);
    }

    public boolean isInCombat(UUID player) {
        Long t = lastDamage.get(player);
        return t != null && (System.currentTimeMillis() - t) <= combatWindowMs;
    }

    public void handleQuit(UUID playerUuid) {
        // TODO: schedule grace or force-kill depending on isInCombat
    }

    public void handleJoin(UUID playerUuid) {
        // TODO: cancel pending kill if present and restore player state
    }
}

