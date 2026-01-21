package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg stop <arenaName>
 *
 * Skeleton: request GameManager.stopGame(arenaId). Stop should validate
 * that the arena exists and is running.
 *
 * Permission: HungerGames.admin
 */
public class StopCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "stop";
    }

    @Override
    public String usage() {
        return "/hg stop <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton StopCommand. Invoke GameManager.stopGame(arenaId) here.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

