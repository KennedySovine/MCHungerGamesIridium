package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;

/**
 * /hg start <arenaName>
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
        if (args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0];
        HungerGames plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(HungerGames.class);
        GameManager gm = plugin.getGameManager();
        gm.startGame(arenaId);
        sender.sendMessage("&aRequested start for arena: " + arenaId);
        return true;
    }
}
