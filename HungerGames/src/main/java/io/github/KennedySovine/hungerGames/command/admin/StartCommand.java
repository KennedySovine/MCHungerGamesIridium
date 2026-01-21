package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg start <arenaName>
 *
 * Skeleton: request GameManager.startGame(arenaId). Start should validate
 * that the arena exists and is not already running.
 *
 * Permission: HungerGames.admin
 */
public class StartCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "start";
    }

    @Override
    public String usage() {
        return "/hg start <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton StartCommand. Invoke GameManager.startGame(arenaId) here.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

