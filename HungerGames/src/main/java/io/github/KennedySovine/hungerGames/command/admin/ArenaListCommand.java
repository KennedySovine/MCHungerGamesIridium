package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ArenaListCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena list";
    }

    @Override
    public String usage() {
        return "/hg arena list";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ArenaManager mgr = HungerGames.getPlugin(HungerGames.class).getArenaManager();
        List<String> names = mgr.getArenaNames();
        if (names.isEmpty()) {
            MessageUtils.send(sender, "&eNo arenas are defined.");
            return true;
        }
        MessageUtils.send(sender, "&6Arenas:");
        for (String n : names) MessageUtils.send(sender, " &7- " + n);
        return true;
    }
}
