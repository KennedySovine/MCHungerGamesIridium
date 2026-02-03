package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class MinPlayersCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "minplayers";
    }

    @Override
    public String usage() {
        return "/hg minplayers <number>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for minplayers.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: args[0] = numeric min players value
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Updates the working arena's minPlayers value (in-memory).
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
            sender.sendMessage("&cMin players must be positive.");
            return true;
        }

        if (num > working.getMaxPlayers()) {
            sender.sendMessage("&cMin players cannot be greater than current max players (" + working.getMaxPlayers() + ").");
            return true;
        }
        working.setMinPlayers(num);
        sender.sendMessage("&aSet min players for working arena " + working.getId() + " to " + num + " (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
