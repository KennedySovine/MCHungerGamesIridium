package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg time <arenaName> <timeInSeconds>
 *
 * Skeleton: set the game time before border shrink/deathmatch onset.
 * Persist the value on the arena object.
 *
 * Permission: HungerGames.admin
 */
public class TimeCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "time";
    }

    @Override
    public String usage() {
        return "/hg time <arenaName> <timeInSeconds>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton TimeCommand. Implement time parsing and set logic.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

