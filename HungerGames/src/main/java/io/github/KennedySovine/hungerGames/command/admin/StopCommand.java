package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;

/**
 * /hg stop <arenaName>
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
        if (args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0];
        HungerGames plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(HungerGames.class);
        GameManager gm = plugin.getGameManager();
        gm.stopGame(arenaId);
        sender.sendMessage("&aRequested stop for arena: " + arenaId);
        return true;
    }
}
