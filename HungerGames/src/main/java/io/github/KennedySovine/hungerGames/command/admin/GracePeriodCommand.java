package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class GracePeriodCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "graceperiod";
    }

    @Override
    public String usage() {
        return "/hg graceperiod <timeInSeconds>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for the subcommand.
     *
     * Parameters:
     * - sender: the command sender (Console or Player). We require a working arena to be loaded.
     * - args: expected to be a single element array containing the grace period in seconds.
     *
     * Behavior:
     * - Validates argument count and numeric value.
     * - Updates the grace period on the currently loaded working arena (in-memory only).
     * - Does NOT persist changes; admin must run `/hg arena save` to persist.
     *
     * Returns: true if command processing is complete.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("&cUsage: " + usage() + " — this command only accepts a single <timeInSeconds> and operates on the loaded working arena.");
            return true;
        }

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<Arena> workingOpt = mgr.getWorkingArena();
        if (workingOpt.isEmpty()) {
            sender.sendMessage("&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }
        Arena working = workingOpt.get();

        Optional<Integer> tOpt = parseInt(sender, args[0]);
        if (tOpt.isEmpty()) return true;
        int t = tOpt.get();
        if (t < 0) {
            sender.sendMessage("&cGrace period must be non-negative.");
            return true;
        }

        working.setGracePeriodSeconds(t);
        sender.sendMessage("&aSet grace period for working arena " + working.getId() + " to " + t + " seconds (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
