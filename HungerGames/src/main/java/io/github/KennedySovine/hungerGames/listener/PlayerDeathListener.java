package io.github.KennedySovine.hungerGames.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener for player death events. Responsible for notifying StatsManager
 * and transitioning players to spectator mode (when implemented).
 */
public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // TODO: hook into StatsManager and GameManager to update stats and
        // transition player to spectator mode.
    }
}

