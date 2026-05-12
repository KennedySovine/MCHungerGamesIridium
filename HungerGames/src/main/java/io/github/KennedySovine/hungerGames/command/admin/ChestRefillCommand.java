package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class ChestRefillCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "chestrefill";
    }

    @Override
    public String usage() {
        return "/hg chestrefill <timeInSeconds>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for chestrefill.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: args[0] = time in seconds for chest refill
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Updates the working arena's chestRefillSeconds value (in-memory).
     * - Does NOT persist; use `/hg arena save` to persist.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            MessageUtils.send(sender, "&cUsage: " + usage() + " — this command only accepts a single <timeInSeconds> and operates on the loaded working arena.");
            return true;
        }

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<Arena> workingOpt = mgr.getWorkingArena();
        if (workingOpt.isEmpty()) {
            MessageUtils.send(sender, "&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }
        Arena working = workingOpt.get();

        Optional<Integer> tOpt = parseInt(sender, args[0]);
        if (tOpt.isEmpty()) return true;
        int t = tOpt.get();
        if (t < 0) {
            MessageUtils.send(sender, "&cChest refill time must be non-negative.");
            return true;
        }

        working.setChestRefillSeconds(t);
        if (t == 0) {
            MessageUtils.send(sender, "&eChest refills disabled for working arena " + working.getId() + " (interval set to 0). Use '/hg arena save' to persist.");
            return true;
        }

        MessageUtils.send(sender, "&aSet chest refill for working arena " + working.getId() + " to " + t + " seconds (in-memory). Standard refill pacing is around 300 seconds. Use '/hg arena save' to persist.");
        return true;
    }
}
