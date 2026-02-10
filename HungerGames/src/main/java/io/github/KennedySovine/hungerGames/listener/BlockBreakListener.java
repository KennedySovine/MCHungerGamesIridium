package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listener to prevent non-admin players from breaking blocks.
 */
public class BlockBreakListener implements Listener {

    private final GameManager gameManager;

    public BlockBreakListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Allow admins and ops to break blocks
        if (player.hasPermission("HungerGames.admin") || player.isOp()) {
            return;
        }
        
        // Cancel block breaking for non-admin players
        event.setCancelled(true);
        MessageUtils.send(player, "&cYou cannot break blocks!");
    }
}
