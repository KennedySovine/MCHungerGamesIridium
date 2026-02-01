package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * /hg arena addspawn <arenaName>
 *
 * Adds the player's current location as a spawn point for the arena.
 * Requires the arena to already have a lobby/center set (see data file or GUI).
 * Persists arenas.yml via ArenaManager.saveArenas() on success.
 *
 * Permission: HungerGames.admin
 */
public class AddSpawnCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena addspawn";
    }

    @Override
    public String usage() {
        return "/hg arena addspawn <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Optional<Player> pOpt = asPlayer(sender);
        if (pOpt.isEmpty()) {
            sender.sendMessage("&cThis command must be executed by a player.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0];
        HungerGames plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(HungerGames.class);
        ArenaManager mgr = plugin.getArenaManager();

        boolean added = mgr.addSpawnPoint(arenaId, pOpt.get().getLocation());
        if (!added) {
            sender.sendMessage("&cFailed to add spawn. Ensure the arena exists and its lobby/center location is set before adding spawns.");
            return true;
        }
        mgr.saveArenas();
        sender.sendMessage("&aSpawn added to arena: " + arenaId);
        return true;
    }
}
