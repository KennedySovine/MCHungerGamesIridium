package io.github.KennedySovine.hungerGames.command.player;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg stats [player]
 *
 * Skeleton: display kills/deaths/wins/losses for a player. If no player
 * argument is given, show stats for the command sender (if a player).
 * Pull data from StatsManager.
 *
 * Permission: no special admin permission required by default (viewing own stats).
 */
public class StatsCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public String usage() {
        return "/hg stats [player]";
    }

    @Override
    public String permission() {
        return null; // no special permission by default
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton StatsCommand. Implement stats lookup and formatting.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

