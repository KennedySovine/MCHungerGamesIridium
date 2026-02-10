package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * /hg stop
 */
public class StopCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "stop";
    }

    @Override
    public String usage() {
        return "/hg stop";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for stop.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: none expected
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Stops the running game for the currently loaded working arena.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 0) {
            MessageUtils.send(sender, "&cUsage: " + usage() + " — do not provide an arena id. Load the desired arena with '/hg arena load <id>' first.");
            return true;
        }

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<io.github.KennedySovine.hungerGames.arena.Arena> wa = mgr.getWorkingArena();
        if (wa.isEmpty()) {
            MessageUtils.send(sender, "&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }
        String arenaId = wa.get().getId();

        GameManager gm = JavaPlugin.getPlugin(HungerGames.class).getGameManager();
        gm.stopGame(arenaId);
        MessageUtils.send(sender, "&aRequested stop for arena: " + arenaId);
        return true;
    }
}
