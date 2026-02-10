package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.combat.CombatManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener handling player join/quit events and delegating to CombatManager.
 */
public class PlayerConnectionListener implements Listener {

    private final CombatManager combatManager;

    public PlayerConnectionListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        combatManager.handleQuit(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        combatManager.handleJoin(event.getPlayer().getUniqueId());
        // Ensure beacon tasks are restored/refreshed when someone joins if a working arena exists
        var mgr = event.getPlayer().getServer().getPluginManager();
        var am = io.github.KennedySovine.hungerGames.HungerGames.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getArenaManager();
        am.getWorkingArena().ifPresent(a -> {
            String id = a.getId();
            io.github.KennedySovine.hungerGames.HungerGames.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getParticleManager().clearAllForArena(id);
            io.github.KennedySovine.hungerGames.HungerGames.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getParticleManager().showCenter(id, a.getLobbyLocation());
            io.github.KennedySovine.hungerGames.HungerGames.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getParticleManager().refreshAllSpawns(id, a);
        });
    }
}
