package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class TimeCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "time";
    }

    @Override
    public String usage() {
        return "/hg time <timeInSeconds>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for time.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: args[0] = time in seconds (integer)
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Updates the working arena's time-to-shrink setting (in-memory).
     * - Does NOT persist; use `/hg arena save` to persist.
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
            sender.sendMessage("&cTime must be non-negative.");
            return true;
        }

        working.setTimeToShrinkSeconds(t);
        sender.sendMessage("&aSet time-to-shrink for working arena " + working.getId() + " to " + t + " seconds (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
