package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * /hg maxplayers <arenaName> <number>
 */
public class MaxPlayersCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "maxplayers";
    }

    @Override
    public String usage() {
        return "/hg maxplayers <arenaName> <number>";
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
            sender.sendMessage("&cMax players must be positive.");
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
        a.setMaxPlayers(num);
        mgr.saveArenas();
        sender.sendMessage("&aSet max players for arena " + arenaId + " to " + num);
        return true;
    }
}
