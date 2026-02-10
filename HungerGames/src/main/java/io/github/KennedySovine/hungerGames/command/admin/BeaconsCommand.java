package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;

/**
 * /hg beacons <on|off>
 * Toggle spawn/center beacon visibility globally.
 */
public class BeaconsCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "beacons";
    }

    @Override
    public String usage() {
        return "/hg beacons <on|off>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            MessageUtils.send(sender, "&cUsage: " + usage());
            return true;
        }
        String arg = args[0].toLowerCase();
        boolean enable;
        if (arg.equals("on") || arg.equals("true")) enable = true;
        else if (arg.equals("off") || arg.equals("false")) enable = false;
        else {
            MessageUtils.send(sender, "&cUsage: " + usage());
            return true;
        }
        HungerGames.getPlugin(HungerGames.class).getParticleManager().setEnabled(enable);
        MessageUtils.send(sender, (enable ? "&aBeacons enabled." : "&cBeacons disabled."));
        return true;
    }
}

