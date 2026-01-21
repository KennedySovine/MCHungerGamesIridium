package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg minplayers <arenaName> <number>
 *
 * Skeleton: parse integer and set the arena's min players via ArenaManager.
 * Validate min/max constraints if desired.
 *
 * Permission: HungerGames.admin
 */
public class MinPlayersCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "minplayers";
    }

    @Override
    public String usage() {
        return "/hg minplayers <arenaName> <number>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton MinPlayersCommand. Implement parse and set logic.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

