package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * /hg maxplayers <number>
 * Only operates on the currently loaded working arena.
 */
public class MaxPlayersCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "maxplayers";
    }

    @Override
    public String usage() {
        return "/hg maxplayers <number>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for maxplayers.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: args[0] = numeric max players value
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Updates the working arena's maxPlayers value (in-memory).
     * - Does NOT persist; use `/hg arena save` to persist.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("&cUsage: " + usage() + " — this command only accepts a single <number> and operates on the loaded working arena.");
            return true;
        }

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<Arena> workingOpt = mgr.getWorkingArena();
        if (workingOpt.isEmpty()) {
            sender.sendMessage("&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }
        Arena working = workingOpt.get();

        Optional<Integer> numOpt = parseInt(sender, args[0]);
        if (numOpt.isEmpty()) return true;
        int num = numOpt.get();
        if (num <= 0) {
            sender.sendMessage("&cMax players must be positive.");
            return true;
        }

        working.setMaxPlayers(num);
        sender.sendMessage("&aSet max players for working arena " + working.getId() + " to " + num + " (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
