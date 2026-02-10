package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.combat.CombatManager;
import io.github.KennedySovine.hungerGames.game.GameManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener handling player join/quit events and delegating to CombatManager.
 * Also handles putting non-admin players in spectator mode when they join.
 */
public class PlayerConnectionListener implements Listener {

    private final CombatManager combatManager;
    private final GameManager gameManager;

    public PlayerConnectionListener(CombatManager combatManager, GameManager gameManager) {
        this.combatManager = combatManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        combatManager.handleQuit(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        combatManager.handleJoin(player.getUniqueId());
        
        // Put non-admin/non-op players in spectator mode
        // Only if they are not already in a game
        if (!player.hasPermission("HungerGames.admin") && !player.isOp()) {
            if (!gameManager.isPlayerInArena(player.getUniqueId())) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
    }
}

