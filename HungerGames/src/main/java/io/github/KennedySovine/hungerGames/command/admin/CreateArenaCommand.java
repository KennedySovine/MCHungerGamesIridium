package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * /hg arena create <arenaName>
 *
 * Creates a new arena and persists it to arenas.yml. If run by a player, the
 * player's current location will be stored as the arena center (lobby) which
 * is used when adding spawn points.
 *
 * Permission: HungerGames.admin
 */
public class CreateArenaCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena create"; // primary identifier; dispatcher should register alias handling
    }

    @Override
    public String usage() {
        return "/hg arena create <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
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

        if (manager.getArena(arenaId).isPresent()) {
            sender.sendMessage("&cAn arena with that id already exists: " + arenaId);
            return true;
        }

        Arena created = manager.createArena(arenaId, arenaId);
        if (created == null) {
            sender.sendMessage("&cFailed to create arena. An arena with that id may already exist.");
            return true;
        }

        // If a player created the arena, set their current location as the center
        Optional<Player> p = asPlayer(sender);
        if (p.isPresent()) {
            created.setLobbyLocation(p.get().getLocation());
            sender.sendMessage("&aCreated arena '" + arenaId + "' and set its center to your current location.");
        } else {
            sender.sendMessage("&aCreated arena '" + arenaId + "'. Note: no center was set because you are not a player.");
        }

        // Persist changes
        manager.saveArenas();
        sender.sendMessage("&eSaved arenas.yml.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
