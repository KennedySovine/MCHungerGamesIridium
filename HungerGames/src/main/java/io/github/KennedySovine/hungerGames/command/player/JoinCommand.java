package io.github.KennedySovine.hungerGames.command.player;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.game.GameState;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * /hg join
 *
 * Player joins the currently loaded arena. Validates player sender, arena existence,
 * and passes to GameManager.join(player, arenaId, arena). Provides friendly messages
 * for edge cases (full arena, already in game, game not in COUNTDOWN state).
 *
 * Permission: HungerGames.player
 */
public class JoinCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "join";
    }

    @Override
    public String usage() {
        return "/hg join";
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
        ArenaManager arenaManager = plugin.getArenaManager();
        GameManager gameManager = plugin.getGameManager();
        
        // Check if player is already in a game
        if (gameManager.isPlayerInArena(player.getUniqueId())) {
            MessageUtils.send(player, "&cYou are already in a game!");
            return true;
        }
        
        // Get the working arena
        Optional<Arena> arenaOpt = arenaManager.getWorkingArena();
        if (arenaOpt.isEmpty()) {
            MessageUtils.send(player, "&cNo arena is currently loaded. Ask an admin to load an arena!");
            return true;
        }
        
        Arena arena = arenaOpt.get();
        String arenaId = arena.getId();
        
        // Check game state - must be in COUNTDOWN to join
        GameState state = gameManager.getGameState(arenaId);
        if (state != GameState.COUNTDOWN) {
            if (state == GameState.LOBBY) {
                MessageUtils.send(player, "&cThe game hasn't started yet! Ask an admin to start the game with /hg start.");
            } else if (state == GameState.FINISHED) {
                MessageUtils.send(player, "&cThe game has already finished! Ask an admin to start a new game.");
            } else {
                MessageUtils.send(player, "&cThe game has already begun! You cannot join now.");
            }
            return true;
        }
        
        // Check if arena is full
        if (gameManager.getPlayerCount(arenaId) >= arena.getMaxPlayers()) {
            MessageUtils.send(player, "&cThe arena is full!");
            return true;
        }
        
        // Join the game
        boolean success = gameManager.join(player, arenaId, arena);
        if (!success) {
            MessageUtils.send(player, "&cFailed to join the game. Contact an admin.");
        }
        
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

