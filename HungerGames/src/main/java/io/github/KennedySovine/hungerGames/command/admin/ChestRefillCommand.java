package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * /hg chestrefill <arenaName> <timeInSeconds>
 */
public class ChestRefillCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "chestrefill";
    }

    @Override
    public String usage() {
        return "/hg chestrefill <arenaName> <timeInSeconds>";
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
        Optional<Integer> tOpt = parseInt(sender, args[1]);
        if (tOpt.isEmpty()) return true;
        int t = tOpt.get();
        if (t < 0) {
            sender.sendMessage("&cChest refill time must be non-negative.");
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
        a.setChestRefillSeconds(t);
        mgr.saveArenas();
        sender.sendMessage("&aSet chest refill for arena " + arenaId + " to " + t + " seconds.");
        return true;
    }
}
