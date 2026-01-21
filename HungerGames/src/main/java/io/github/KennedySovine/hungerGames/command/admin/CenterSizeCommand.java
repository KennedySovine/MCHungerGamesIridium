package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg centersize <arenaName> <size>
 *
 * Skeleton: sets the central (final) size the border should be when the
 * deathmatch begins. Persist to arena configuration.
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
        return "/hg centersize <arenaName> <size>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton CenterSizeCommand. Implement parsing and setting of center size.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

