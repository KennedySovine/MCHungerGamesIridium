package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg chestrefill <arenaName> <timeInSeconds>
 *
 * Skeleton: configure how often chests should be refilled during a match.
 * Persist the value on the arena object.
 *
 * Permission: HungerGames.admin
 */
public class ChestRefillCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "chestrefill";
    }

    @Override
    public String usage() {
        return "/hg chestrefill <arenaName> <timeInSeconds>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton ChestRefillCommand. Implement parsing and persistence.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

