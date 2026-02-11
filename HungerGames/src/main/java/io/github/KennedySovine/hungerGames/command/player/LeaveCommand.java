package io.github.KennedySovine.hungerGames.command.player;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * /hg leave
 *
 * Player leaves the current arena. Calls GameManager.leave(player)
 * and ensures inventory restore and spectator mode for non-admins.
 *
 * Permission: HungerGames.player
 */
public class LeaveCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "leave";
    }

    @Override
    public String usage() {
        return "/hg leave";
    }

    @Override
    public String permission() {
        return "HungerGames.player";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return true;
        Player player = playerOpt.get();
        
        HungerGames plugin = JavaPlugin.getPlugin(HungerGames.class);
        GameManager gameManager = plugin.getGameManager();
        
        // Check if player is in a game
        if (!gameManager.isPlayerInArena(player.getUniqueId())) {
            MessageUtils.send(player, "&cYou are not in a game!");
            return true;
        }
        
        // Leave the game
        gameManager.leave(player);
        
        // Put non-admin players back in spectator mode and give them the compass
        if (!player.hasPermission("HungerGames.admin") && !player.isOp()) {
            player.setGameMode(GameMode.SPECTATOR);
            plugin.getSpectatorManager().giveSpectatorCompass(player);
        }
        
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
