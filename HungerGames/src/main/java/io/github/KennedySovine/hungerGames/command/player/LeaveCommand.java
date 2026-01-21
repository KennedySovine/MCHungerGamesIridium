package io.github.KennedySovine.hungerGames.command.player;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg leave
 *
 * Skeleton: player leaves the current arena. Call GameManager.leave(player)
 * and ensure inventory restore and spectator handling as necessary.
 *
 * Permission: HungerGames.player
 */
public class LeaveCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "leave";
    }

    @Override
    public String usage() {
        return "/hg leave";
    }

    @Override
    public String permission() {
        return "HungerGames.player";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton LeaveCommand. Implement leave logic and inventory restore here.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

