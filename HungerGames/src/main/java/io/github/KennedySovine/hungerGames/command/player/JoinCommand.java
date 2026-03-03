package io.github.KennedySovine.hungerGames.command.player;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg join <arenaName>
 *
 * Skeleton: player joins the arena. Validate player sender, arena existence,
 * and pass to GameManager.join(player, arenaId). Provide friendly messages
 * for edge cases (full arena, already in game).
 *
 * Permission: HungerGames.player
 */
public class JoinCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "join";
    }

    @Override
    public String usage() {
        return "/hg join <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.player";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        MessageUtils.send(sender, "[HG] Skeleton JoinCommand. Implement join logic here.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
