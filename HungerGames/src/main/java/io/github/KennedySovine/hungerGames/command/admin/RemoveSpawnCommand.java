package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

/**
 * /hg arena removespawn <arenaName> <spawnIndex>
 *
 * Removes a spawn by index from the arena's spawn list and persists.
 * If only <arenaName> is provided and the sender is a player, attempts to
 * remove a spawn located at the player's current position (within 1 block).
 */
public class RemoveSpawnCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena removespawn";
    }

    @Override
    public String usage() {
        return "/hg arena removespawn <arenaName> <spawnIndex>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // If no arguments provided, show usage
        if (args.length < 1) {
            sender.sendMessage("&cUsage: " + usage());
            return true;
        }

        // If only arena name provided, attempt to remove spawn at player's location
        if (args.length < 2) {
            Optional<Player> pOpt = asPlayer(sender);
            if (pOpt.isEmpty()) {
                sender.sendMessage("&cThis form requires a player to be the command sender.");
                return true;
            }
            Player player = pOpt.get();
            String arenaId = args[0];

            HungerGames plugin = JavaPlugin.getPlugin(HungerGames.class);
            ArenaManager mgr = plugin.getArenaManager();
            Optional<Arena> aOpt = mgr.getArena(arenaId);
            if (aOpt.isEmpty()) {
                sender.sendMessage("&cArena not found: " + arenaId);
                return true;
            }
            Arena arena = aOpt.get();
            Location center = arena.getLobbyLocation();
            if (center == null) {
                sender.sendMessage("&cArena does not have a lobby/center set. Cannot resolve relative spawns.");
                return true;
            }

            List<Location> spawns = arena.getAbsoluteSpawns(center);
            Location here = player.getLocation();
            int found = -1;
            for (int i = 0; i < spawns.size(); i++) {
                Location s = spawns.get(i);
                if (s == null) continue;
                if (!s.getWorld().getName().equals(here.getWorld().getName())) continue;
                double distance = s.distance(here);
                if (distance <= 1.0) { // within 1 block
                    found = i;
                    break;
                }
            }

            if (found == -1) {
                sender.sendMessage("&cNo spawn point found near your current location in arena " + arenaId);
                return true;
            }

            boolean removed = mgr.removeSpawnPoint(arenaId, found);
            if (!removed) {
                sender.sendMessage("&cFailed to remove spawn at your location. Ensure the arena is configured correctly.");
                return true;
            }
            mgr.saveArenas();
            sender.sendMessage("&aRemoved spawn at your location (index " + found + ") from arena " + arenaId);
            return true;
        }

        // Otherwise expect arenaName and index
        String arenaId = args[0];
        Optional<Integer> idx = parseInt(sender, args[1]);
        if (idx.isEmpty()) return true;

        HungerGames plugin = JavaPlugin.getPlugin(HungerGames.class);
        ArenaManager mgr = plugin.getArenaManager();

        boolean removed = mgr.removeSpawnPoint(arenaId, idx.get());
        if (!removed) {
            sender.sendMessage("&cFailed to remove spawn. Ensure arena exists and index is valid.");
            return true;
        }
        mgr.saveArenas();
        sender.sendMessage("&aRemoved spawn index " + idx.get() + " from arena " + arenaId);
        return true;
    }
}
