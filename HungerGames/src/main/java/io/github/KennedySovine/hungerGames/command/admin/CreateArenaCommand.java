package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg arena create <arenaName>
 *
 * Skeleton: validate inputs and call ArenaManager.createArena(arenaName).
 * This file intentionally contains only the command contract and Javadoc.
 * Implementation should persist the new arena to arenas.yml via ArenaManager.
 *
 * Permission: HungerGames.admin
 */
public class CreateArenaCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena create"; // primary identifier; dispatcher should register alias handling
    }

    @Override
    public String usage() {
        return "/hg arena create <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // TODO: validate args length, normalize arena name, call ArenaManager.createArena
        sender.sendMessage("[HG] This is a skeleton CreateArenaCommand. Implement logic to create and persist arenas.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

