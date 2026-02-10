package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;

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
     * Execute: set center size on the working arena.
     * Parameters:
     * - sender: command issuer
     * - args[0]: size (integer)
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            MessageUtils.send(sender, "&cUsage: " + usage() + " — this command only accepts a single <size> and operates on the loaded working arena.");
            return true;
        }
        Optional<Arena> opt = HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena();
        if (opt.isEmpty()) {
            MessageUtils.send(sender, "&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }
        Optional<Integer> maybe = parseInt(sender, args[0]);
        if (maybe.isEmpty()) return true;
        int size = maybe.get();
        if (size <= 0) {
            MessageUtils.send(sender, "&cCenter size must be positive.");
            return true;
        }
        Arena working = opt.get();
        working.setCenterSize(size);
        // If the working arena has a lobby/center set, update the world border immediately
        if (working.getLobbyLocation() != null) {
            HungerGames.getPlugin(HungerGames.class).getBorderManager().applyBorder(working.getId(), working.getLobbyLocation(), size);
        }
        MessageUtils.send(sender, "&aSet center size for working arena " + working.getId() + " to " + size + " (in-memory). Use '/hg arena save' to persist.");
        return true;
    }
}
