package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
            MessageUtils.send(sender, "&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0];
        ArenaManager mgr = JavaPlugin.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getArenaManager();
        boolean ok = mgr.loadWorkingArena(arenaId);
        if (!ok) {
            MessageUtils.send(sender, "&cArena not found: " + arenaId);
            return true;
        }

        // If sender is a player, set the working arena center to their current location
        if (sender instanceof Player) {
            Player p = (Player) sender;
            mgr.getWorkingArena().ifPresent(a -> a.setLobbyLocation(p.getLocation()));
            MessageUtils.send(sender, "&aLoaded arena into working memory: " + arenaId + " and set center to your current location.");
            return true;
        }

        MessageUtils.send(sender, "&aLoaded arena into working memory: " + arenaId + " (further edit commands will use this arena by default)");
        return true;
    }
}
