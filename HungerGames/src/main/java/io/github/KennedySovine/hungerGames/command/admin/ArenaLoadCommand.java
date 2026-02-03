package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * /hg arena load <arenaName>
 *
 * Load an arena from the master list into the in-memory working placeholder.
 */
public class ArenaLoadCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena load";
    }

    @Override
    public String usage() {
        return "/hg arena load <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute the load command.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: args[0] = arenaId to load
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Copies the master arena into the working (in-memory) arena placeholder.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0];
        ArenaManager mgr = JavaPlugin.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getArenaManager();
        boolean ok = mgr.loadWorkingArena(arenaId);
        if (!ok) {
            sender.sendMessage("&cArena not found: " + arenaId);
            return true;
        }
        sender.sendMessage("&aLoaded arena into working memory: " + arenaId + " (further edit commands will use this arena by default)");
        return true;
    }
}
