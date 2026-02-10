package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listener to prevent player movement when game is in COUNTDOWN state.
 */
public class PlayerMoveListener implements Listener {

    private final GameManager gameManager;

    public PlayerMoveListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String arenaId = gameManager.getPlayerArena(player.getUniqueId());
        
        // Only restrict movement if player is in an arena
        if (arenaId == null) return;
        
        GameState state = gameManager.getGameState(arenaId);
        
        // Prevent movement in COUNTDOWN state
        if (state == GameState.COUNTDOWN) {
            // Only cancel if player actually moved (not just looking around)
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }
}
