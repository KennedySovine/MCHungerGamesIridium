package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg maxplayers <arenaName> <number>
 *
 * Skeleton: parse integer and set the arena's max players via ArenaManager.
 * Validate min/max constraints if desired.
 *
 * Permission: HungerGames.admin
 */
public class MaxPlayersCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "maxplayers";
    }

    @Override
    public String usage() {
        return "/hg maxplayers <arenaName> <number>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton MaxPlayersCommand. Implement parse and set logic.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

