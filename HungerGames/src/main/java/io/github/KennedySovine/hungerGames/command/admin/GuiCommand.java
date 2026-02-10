package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
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
            MessageUtils.send(sender, "&cThis command must be executed by a player.");
            return true;
        }
        Player player = pOpt.get();

        // If a working arena is loaded, open the editor for that arena; otherwise
        // open in create/load (null) mode. This avoids forcing admins to always
        // call the load command before opening the GUI but still reflects the
        // current working arena when present.
        HungerGames plugin = HungerGames.getPlugin(HungerGames.class);
        String workingId = null;
        if (plugin.getArenaManager().getWorkingArena().isPresent()) {
            workingId = plugin.getArenaManager().getWorkingArena().get().getId();
        }

        ArenaEditorGui editor = new ArenaEditorGui();
        editor.openEditor(player, workingId);

        return true;
    }
}
