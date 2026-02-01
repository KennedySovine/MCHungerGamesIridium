package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * /hg minplayers <arenaName> <number>
 */
public class MinPlayersCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "minplayers";
    }

    @Override
    public String usage() {
        return "/hg minplayers <arenaName> <number>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0];
        Optional<Integer> numOpt = parseInt(sender, args[1]);
        if (numOpt.isEmpty()) return true;
        int num = numOpt.get();
        if (num <= 0) {
            sender.sendMessage("&cMin players must be positive.");
            return true;
        }

        HungerGames plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(HungerGames.class);
        ArenaManager mgr = plugin.getArenaManager();
        Optional<Arena> aOpt = mgr.getArena(arenaId);
        if (aOpt.isEmpty()) {
            sender.sendMessage("&cArena not found: " + arenaId);
            return true;
        }
        Arena a = aOpt.get();
        if (num > a.getMaxPlayers()) {
            sender.sendMessage("&cMin players cannot be greater than current max players (" + a.getMaxPlayers() + ").");
            return true;
        }
        a.setMinPlayers(num);
        mgr.saveArenas();
        sender.sendMessage("&aSet min players for arena " + arenaId + " to " + num);
        return true;
    }
}
