package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * /hg centersize <arenaName> <size>
 *
 * Skeleton: sets the central (final) size the border should be when the
 * deathmatch begins. Persist to arena configuration.
 *
 * Permission: HungerGames.admin
 */
public class CenterSizeCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "centersize";
    }

    @Override
    public String usage() {
        return "/hg centersize <arenaName> <size>";
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
        Optional<Integer> sizeOpt = parseInt(sender, args[1]);
        if (sizeOpt.isEmpty()) return true;
        int size = sizeOpt.get();
        if (size <= 0) {
            sender.sendMessage("&cCenter size must be positive.");
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
        a.setCenterSize(size);
        mgr.saveArenas();
        sender.sendMessage("&aSet center size for arena " + arenaId + " to " + size);
        return true;
    }
}
