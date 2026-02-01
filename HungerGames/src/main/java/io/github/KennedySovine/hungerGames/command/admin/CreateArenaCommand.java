package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * /hg arena create <arenaName>
 *
 * Creates a new arena id and display name (display name is optional; when not
 * provided the id will be used). Persists arenas.yml via ArenaManager.saveArenas().
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
        return "/hg arena create <arenaName> [displayName]";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String id = args[0];
        String display = (args.length >= 2) ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : id;

        HungerGames plugin = JavaPlugin.getPlugin(HungerGames.class);
        ArenaManager mgr = plugin.getArenaManager();

        Arena created = mgr.createArena(id, display);
        if (created == null) {
            sender.sendMessage("&cAn arena with id '" + id + "' already exists.");
            return true;
        }

        mgr.saveArenas();
        sender.sendMessage("&aArena created: " + created.getId() + " (display: " + created.getDisplayName() + ")");
        return true;
    }
}
