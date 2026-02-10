package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;

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
            MessageUtils.send(sender, "&cUsage: " + usage() + " — this command only accepts a single <timeInSeconds> and operates on the loaded working arena.");
            return true;
        }

        Optional<io.github.KennedySovine.hungerGames.arena.Arena> opt = HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena();
        if (opt.isEmpty()) {
            MessageUtils.send(sender, "&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }

        Optional<Integer> maybe = parseInt(sender, args[0]);
        if (maybe.isEmpty()) return true;
        int time = maybe.get();
        if (time < 0) {
            MessageUtils.send(sender, "&cGrace period must be non-negative.");
            return true;
        }

        io.github.KennedySovine.hungerGames.arena.Arena working = opt.get();
        working.setGracePeriodSeconds(time);
        MessageUtils.send(sender, "&aSet grace period for working arena " + working.getId() + " to " + time + " (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
