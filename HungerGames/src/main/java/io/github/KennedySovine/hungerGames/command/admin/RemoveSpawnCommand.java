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

import java.util.List;
import java.util.Optional;

/**
 * /hg arena removespawn <spawnIndex>
 * /hg arena removespawn  (as player) removes a spawn at the player's current location
 *
 * This command only operates on the loaded working arena. No arena id may be supplied.
 */
public class RemoveSpawnCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena removespawn";
    }

    @Override
    public String usage() {
        return "/hg arena removespawn <spawnIndex>  OR  /hg arena removespawn  (as player: removes spawn at your location)";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute handler for removespawn.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: optional single index argument; if omitted the sender must be a player and the nearest spawn will be removed.
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Operates only on the working arena (in-memory); does not persist changes.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Disallow providing arena id; edits must act on working arena only
        ArenaManager mgr = JavaPlugin.getPlugin(HungerGames.class).getArenaManager();
        Optional<Arena> workingOpt = mgr.getWorkingArena();
        if (workingOpt.isEmpty()) {
            MessageUtils.send(sender, "&cNo working arena loaded. Use '/hg arena load <arena>' or '/hg arena create <arena>' first.");
            return true;
        }
        Arena working = workingOpt.get();

        // If no args and sender is a player: remove spawn at player's location
        if (args.length == 0) {
            Optional<Player> pOpt = asPlayer(sender);
            if (pOpt.isEmpty()) {
                MessageUtils.send(sender, "&cThis form requires a player to be the command sender when no index is provided.");
                return true;
            }
            Player player = pOpt.get();

            List<Location> spawns = working.getAbsoluteSpawns(working.getLobbyLocation());
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
                MessageUtils.send(sender, "&cNo spawn point found near your current location in the working arena " + working.getId());
                return true;
            }

            boolean removed = mgr.removeSpawnPointFromWorking(found);
            if (!removed) {
                MessageUtils.send(sender, "&cFailed to remove spawn from working arena.");
                return true;
            }
            // remove beacon for that index and refresh remaining spawn beacons
            String wid = HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena().map(a -> a.getId()).orElse("");
            HungerGames.getPlugin(HungerGames.class).getParticleManager().removeSpawn(wid, found);
            HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid, mgr.getWorkingArena().orElse(null));
            MessageUtils.send(sender, "&aRemoved spawn at your location (index " + found + ") from working arena " + working.getId() + " (in-memory). Use '/hg arena save' to persist.");
            return true;
        }

        // If one arg present, treat as index removal
        if (args.length == 1) {
            Optional<Integer> idx = parseInt(sender, args[0]);
            if (idx.isEmpty()) return true;
            boolean removed = mgr.removeSpawnPointFromWorking(idx.get());
            if (!removed) {
                MessageUtils.send(sender, "&cFailed to remove spawn at index in working arena. Ensure index is valid.");
                return true;
            }
            // cancel beacon and refresh
            String wid2 = HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena().map(a -> a.getId()).orElse("");
            HungerGames.getPlugin(HungerGames.class).getParticleManager().removeSpawn(wid2, idx.get());
            HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid2, mgr.getWorkingArena().orElse(null));
            MessageUtils.send(sender, "&aRemoved spawn index " + idx.get() + " from working arena " + working.getId() + " (in-memory). Use '/hg arena save' to persist.");
            return true;
        }

        // Any other form is invalid
        MessageUtils.send(sender, "&cUsage: " + usage());
        return true;
    }
}
