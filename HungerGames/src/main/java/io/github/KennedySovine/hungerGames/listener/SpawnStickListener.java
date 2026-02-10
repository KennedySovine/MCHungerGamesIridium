package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.List;

/**
 * Listener for admin spawn-stick interactions. The spawn-stick should contain
 * plugin-specific metadata (PersistentDataContainer) to identify which arena
 * it is associated with. Right-click should add a spawn point; left-click removes the nearest spawn.
 */
public class SpawnStickListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(HungerGames.getPlugin(HungerGames.class), ArenaEditorGui.SPAWN_STICK_KEY_NAME), PersistentDataType.STRING)) return;
        String arenaId = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(HungerGames.getPlugin(HungerGames.class), ArenaEditorGui.SPAWN_STICK_KEY_NAME), PersistentDataType.STRING);
        Player p = event.getPlayer();
        if (arenaId == null || arenaId.isEmpty()) {
            MessageUtils.send(p, "&cThis spawn stick is not bound to an arena.");
            return;
        }

        Action act = event.getAction();
        // Left click = remove nearest spawn; Right click = add spawn
        if (act == Action.LEFT_CLICK_AIR || act == Action.LEFT_CLICK_BLOCK) {
            // attempt to remove nearest spawn within 1 block
            var mgr = HungerGames.getPlugin(HungerGames.class).getArenaManager();
            var wa = mgr.getWorkingArena();
            if (wa.isEmpty()) {
                MessageUtils.send(p, "&cNo working arena loaded.");
                return;
            }
            var working = wa.get();
            List<Location> spawns = working.getAbsoluteSpawns(working.getLobbyLocation());
            Location here = p.getLocation();
            int found = -1;
            for (int i = 0; i < spawns.size(); i++) {
                Location s = spawns.get(i);
                if (s == null) continue;
                if (!s.getWorld().getName().equals(here.getWorld().getName())) continue;
                double distance = s.distance(here);
                if (distance <= 1.0) {
                    found = i;
                    break;
                }
            }
            if (found == -1) {
                MessageUtils.send(p, "&cNo spawn point found near your current location in the working arena.");
                return;
            }
            boolean removed = mgr.removeSpawnPointFromWorking(found);
            if (!removed) {
                MessageUtils.send(p, "&cFailed to remove spawn from working arena.");
                return;
            }
            // cancel beacon for that index and refresh remaining spawn beacons
            String wid = mgr.getWorkingArena().map(a -> a.getId()).orElse("");
            HungerGames.getPlugin(HungerGames.class).getParticleManager().removeSpawn(wid, found);
            HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid, mgr.getWorkingArena().orElse(null));
            MessageUtils.send(p, "&aRemoved spawn at your location (index " + found + ") from working arena " + working.getId() + " (in-memory). Use '/hg arena save' to persist.");
            return;
        }

        if (act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK) {
            boolean ok = HungerGames.getPlugin(HungerGames.class).getArenaManager().addSpawnPointToWorking(p.getLocation());
            if (!ok) {
                MessageUtils.send(p, "&cFailed to add spawn to working arena. Ensure a working arena is loaded and has a center set.");
                return;
            }
            // register spawn beacon via ParticleManager
            String wid = HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena().map(a -> a.getId()).orElse("");
            int idx = HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena().map(a -> a.getSpawnOffsets().size()-1).orElse(0);
            HungerGames.getPlugin(HungerGames.class).getParticleManager().showSpawn(wid, idx, p.getLocation());
            MessageUtils.send(p, "&aSpawn added at your location (relative offset saved in working arena). Use '/hg arena save' to persist.");
            return;
        }
    }
}
