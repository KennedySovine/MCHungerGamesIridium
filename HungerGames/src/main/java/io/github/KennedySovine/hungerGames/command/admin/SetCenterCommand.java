package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class SetCenterCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "setcenter";
    }

    @Override
    public String usage() {
        return "/hg arena setcenter";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Sets the center (lobbyLocation) of the currently loaded working arena to
     * the player's current location. This only mutates the working arena in-memory.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Optional<Player> pOpt = asPlayer(sender);
        if (pOpt.isEmpty()) {
            MessageUtils.send(sender, "&cThis command must be executed by a player.");
            return true;
        }
        Player player = pOpt.get();

        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<Arena> wa = mgr.getWorkingArena();
        if (wa.isEmpty()) {
            MessageUtils.send(sender, "&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }

        Location loc = player.getLocation();
        Arena working = wa.get();
        working.setLobbyLocation(loc);
        MessageUtils.send(sender, "&aWorking arena center set to your current location: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        // update center beacon and refresh spawn beacons to reflect new center
        String wid = working.getId();
        HungerGames.getPlugin(HungerGames.class).getParticleManager().showCenter(wid, loc);
        HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid, wa.get());
        // update world border to new center
        HungerGames.getPlugin(HungerGames.class).getBorderManager().applyBorder(wid, loc, working.getCenterSize());
        return true;
    }
}
