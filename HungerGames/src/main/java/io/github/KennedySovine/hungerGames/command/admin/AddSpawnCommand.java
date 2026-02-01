package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * /hg arena addspawn
 *
 * Adds the player's current location as a spawn point for the working arena only.
 * No arena name may be supplied: edits must be performed against the loaded working arena.
 * Changes are kept in-memory; use '/hg arena save' to persist.
 *
 * Permission: HungerGames.admin
 */
public class AddSpawnCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena addspawn";
    }

    @Override
    public String usage() {
        return "/hg arena addspawn";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute the addspawn command.
     *
     * Parameters:
     * - sender: the command issuer (must be a Player)
     * - args: no arguments expected
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Adds the player's current location to the loaded working arena as a spawn (in-memory).
     * - Does NOT persist changes; use `/hg arena save` to write the working arena to disk.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // No additional args allowed
        if (args.length > 0) {
            sender.sendMessage("&cUsage: " + usage() + " — do not provide an arena id. Load the arena with '/hg arena load <id>' first.");
            return true;
        }

        Optional<Player> pOpt = asPlayer(sender);
        if (pOpt.isEmpty()) {
            sender.sendMessage("&cThis command must be executed by a player.");
            return true;
        }
        Player player = pOpt.get();

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<Arena> workingOpt = mgr.getWorkingArena();
        if (workingOpt.isEmpty()) {
            sender.sendMessage("&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }

        boolean ok = mgr.addSpawnPointToWorking(player.getLocation());
        if (!ok) {
            sender.sendMessage("&cFailed to add spawn to working arena. Ensure the working arena has a center/lobby set.");
            return true;
        }
        sender.sendMessage("&aSpawn added to working arena at your location (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
