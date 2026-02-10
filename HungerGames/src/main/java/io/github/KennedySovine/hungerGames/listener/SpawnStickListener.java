package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;

/**
 * Listener for admin spawn-stick interactions. The spawn-stick should contain
 * plugin-specific metadata (PersistentDataContainer) to identify which arena
 * it is associated with. Right-click should add a spawn point.
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
        boolean ok = HungerGames.getPlugin(HungerGames.class).getArenaManager().addSpawnPointToWorking(p.getLocation());
        if (!ok) {
            MessageUtils.send(p, "&cFailed to add spawn to working arena. Ensure a working arena is loaded and has a center set.");
            return;
        }
        // register spawn beacon via ParticleManager
        // working arena id will be used
        String wid = HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena().map(a -> a.getId()).orElse("");
        HungerGames.getPlugin(HungerGames.class).getParticleManager().showSpawn(wid, HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena().map(a -> a.getSpawnOffsets().size()-1).orElse(0), p.getLocation());
        MessageUtils.send(p, "&aSpawn added at your location (relative offset saved in working arena). Use '/hg arena save' to persist.");
    }
}
