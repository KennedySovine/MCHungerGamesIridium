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
 * /hg start
 *
 * Opens the game for players to join (sets state to COUNTDOWN).
 * Players can join via /hg join but cannot move until the game begins.
 * Use /hg begin to actually start the game and allow movement.
 * No arena id may be supplied; to start a different arena, first use '/hg arena load <id>'.
 */
public class StartCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "start";
    }

    @Override
    public String usage() {
        return "/hg start";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for start.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: none expected
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Opens the game for players to join (sets state to COUNTDOWN).
     * - Players can join via /hg join but cannot move until /hg begin is used.
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
        gm.startGame(arenaId);
        MessageUtils.send(sender, "&aGame opened for joining! Players can now use /hg join. Use /hg begin to start the game.");
        return true;
    }
}
