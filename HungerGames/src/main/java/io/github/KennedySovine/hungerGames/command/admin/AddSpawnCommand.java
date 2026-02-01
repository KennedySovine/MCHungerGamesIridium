package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * /hg arena addspawn <arenaName>
 *
 * Saves the command player's current location as a spawn point for the
 * specified arena. The arena must already have a center (lobby) set which
 * defines how the spawn will be stored as a relative offset.
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
        Optional<Player> optPlayer = asPlayer(sender);
        if (optPlayer.isEmpty()) {
            sender.sendMessage("&cThis command can only be used by a player.");
            return true;
        }
        Player player = optPlayer.get();

        if (args == null || args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0].trim();
        if (arenaId.isEmpty()) {
            sender.sendMessage("&cInvalid arena name.");
            return true;
        }

        HungerGames plugin = JavaPlugin.getPlugin(HungerGames.class);
        ArenaManager manager = plugin.getArenaManager();

        boolean ok = manager.addSpawnPoint(arenaId, player.getLocation());
        if (!ok) {
            sender.sendMessage("&cFailed to add spawn. Ensure the arena exists and has a center (use /hg arena create while standing at center).");
            return true;
        }

        manager.saveArenas();
        sender.sendMessage("&aSpawn added for arena: " + arenaId + " and saved to arenas.yml.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
