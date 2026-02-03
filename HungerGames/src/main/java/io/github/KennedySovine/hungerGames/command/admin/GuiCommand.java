package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GuiCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "gui";
    }

    @Override
    public String usage() {
        return "/hg gui";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Optional<Player> pOpt = asPlayer(sender);
        if (pOpt.isEmpty()) {
            sender.sendMessage("&cThis command must be executed by a player.");
            return true;
        }
        Player player = pOpt.get();

        ArenaEditorGui editor = new ArenaEditorGui();
        editor.openEditor(player, null);

        return true;
    }
}
