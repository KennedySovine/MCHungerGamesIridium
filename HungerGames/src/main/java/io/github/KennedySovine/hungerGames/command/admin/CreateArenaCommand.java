package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * /hg arena create <arenaName> [displayName]
 *
 * Creates a new arena entry in the master list, persists it to disk, and
 * loads it into the working placeholder so admins don't need to specify the
 * arena name for subsequent edit commands.
 */
public class CreateArenaCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena create";
    }

    @Override
    public String usage() {
        return "/hg arena create <arenaName> [displayName]";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute the create command.
     *
     * Parameters:
     * - sender: the command issuer (Console or Player)
     * - args: args[0] = arenaId, args[1..] = optional displayName parts
     *
     * Behavior:
     * - Requires permission: "HungerGames.admin".
     * - Creates the master arena entry, persists it to disk so the id exists,
     *   then loads the arena into the working (in-memory) arena for edits.
     * - Returns true once the command is processed.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String id = args[0];
        String display = (args.length >= 2) ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : id;

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Arena created = mgr.createArena(id, display);
        if (created == null) {
            sender.sendMessage("&cAn arena with id '" + id + "' already exists.");
            return true;
        }
        // Persist new arena to disk so it exists in master list
        mgr.saveArenas();
        // Load into working placeholder for further edits
        mgr.loadWorkingArena(created.getId());

        sender.sendMessage("&aArena created and loaded for editing: " + created.getId());
        return true;
    }
}
