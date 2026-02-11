package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.game.GameState;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * /hg begin
 *
 * Begins the game for the currently loaded working arena (transitions from COUNTDOWN to RUNNING).
 * This is different from "start" which opens the game for joining.
 */
public class BeginCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "begin";
    }

    @Override
    public String usage() {
        return "/hg begin";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for begin.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: none expected
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Begins the game for the currently loaded working arena.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            MessageUtils.send(sender, "&cUsage: " + usage());
            return true;
        }

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<io.github.KennedySovine.hungerGames.arena.Arena> wa = mgr.getWorkingArena();
        if (wa.isEmpty()) {
            MessageUtils.send(sender, "&cNo working arena loaded. Use '/hg arena load <arena>' first.");
            return true;
        }
        String arenaId = wa.get().getId();

        GameManager gm = JavaPlugin.getPlugin(HungerGames.class).getGameManager();
        GameState state = gm.getGameState(arenaId);
        
        if (state != GameState.COUNTDOWN) {
            MessageUtils.send(sender, "&cCannot begin game - arena is not in COUNTDOWN state. Use '/hg start' first.");
            return true;
        }
        
        gm.beginGame(arenaId);
        MessageUtils.send(sender, "&aGame has begun for arena: " + arenaId);
        return true;
    }
}
