package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg graceperiod <arenaName> <timeInSeconds>
 *
 * Skeleton: sets arena-specific grace period (in seconds) used for
 * out-of-combat disconnect grace. Persist to arena config on save.
 *
 * Permission: HungerGames.admin
 */
public class GracePeriodCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "graceperiod";
    }

    @Override
    public String usage() {
        return "/hg graceperiod <arenaName> <timeInSeconds>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton GracePeriodCommand. Implement parsing and persistence.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

