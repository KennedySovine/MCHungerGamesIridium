package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg arena delete <arenaName>
 *
 * Skeleton: delete an arena definition from storage. Implementation should
 * validate existence, handle any running matches, and persist the change
 * via ArenaManager/Storage.
 *
 * Permission: HungerGames.admin
 */
public class DeleteArenaCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena delete";
    }

    @Override
    public String usage() {
        return "/hg arena delete <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton DeleteArenaCommand. Implement validation and deletion logic.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

