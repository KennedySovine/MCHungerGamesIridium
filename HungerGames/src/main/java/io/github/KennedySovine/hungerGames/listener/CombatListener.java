package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.combat.CombatManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

/**
 * Listener that forwards relevant damage events to CombatManager.
 */
public class CombatListener implements Listener {

    private final CombatManager combatManager;

    public CombatListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();
            combatManager.recordDamage(attacker.getUniqueId(), victim.getUniqueId());
        }
        // TODO: handle projectile cases (arrow shot by player)
    }
}

