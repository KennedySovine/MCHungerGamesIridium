package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg arena removespawn <arenaName> <spawnIndex>
 *
 * Skeleton: remove a spawn point by index. Implementation must validate
 * that the index is within bounds and persist the change.
 *
 * Permission: HungerGames.admin
 */
public class RemoveSpawnCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena removespawn";
    }

    @Override
    public String usage() {
        return "/hg arena removespawn <arenaName> <spawnIndex>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton RemoveSpawnCommand. Implement index validation and removal.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

