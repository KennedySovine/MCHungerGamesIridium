package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * /hg centersize <size>
 *
 * Sets the central (final) size the border should be when the deathmatch begins.
 * Only operates on the currently loaded working arena and accepts a single numeric argument.
 * Use '/hg arena save' to persist changes.
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
        return "/hg centersize <size>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for center size.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: args[0] = desired center size (integer)
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Updates the working arena's centerSize (in-memory).
     * - Does NOT persist; use `/hg arena save` to persist.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("&cUsage: " + usage() + " — this command only accepts a single <size> and operates on the loaded working arena.");
            return true;
        }

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<Arena> workingOpt = mgr.getWorkingArena();
        if (workingOpt.isEmpty()) {
            sender.sendMessage("&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }
        Arena working = workingOpt.get();

        Optional<Integer> sizeOpt = parseInt(sender, args[0]);
        if (sizeOpt.isEmpty()) return true;
        int size = sizeOpt.get();
        if (size <= 0) {
            sender.sendMessage("&cCenter size must be positive.");
            return true;
        }

        working.setCenterSize(size);
        sender.sendMessage("&aSet center size for working arena " + working.getId() + " to " + size + " (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
