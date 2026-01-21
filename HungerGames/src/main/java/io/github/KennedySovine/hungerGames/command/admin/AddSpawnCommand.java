package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

/**
 * /hg arena addspawn <arenaName>
 *
 * Skeleton: expects to be executed by a player. Implementation should read
 * the player's current location and add it as a spawn point to the arena
 * via ArenaManager.addSpawnPoint(arenaId, location). Consider persisting
 * after adding.
 *
 * Permission: HungerGames.admin
 */
public class AddSpawnCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena addspawn";
    }

    @Override
    public String usage() {
        return "/hg arena addspawn <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("[HG] Skeleton AddSpawnCommand. Implement player-only spawn saving logic.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

