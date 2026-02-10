package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * /hg arena save
 *
 * Persist the currently loaded working arena back to the master arenas list
 * and to disk. This will throw an error if the working arena's id does not
 * exist in the master list (i.e., the arena was not created or persisted).
 */
public class ArenaSaveCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena save";
    }

    @Override
    public String usage() {
        return "/hg arena save";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute the save command.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: none expected
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Attempts to persist the in-memory working arena back to the master arenas and write to disk.
     * - Returns true after processing and prints an error message if saving is not possible.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ArenaManager mgr = JavaPlugin.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getArenaManager();
        try {
            mgr.saveWorkingArena();
            MessageUtils.send(sender, "&aWorking arena persisted to disk.");
        } catch (IllegalStateException ex) {
            MessageUtils.send(sender, "&cCannot save: " + ex.getMessage());
        }
        return true;
    }
}
