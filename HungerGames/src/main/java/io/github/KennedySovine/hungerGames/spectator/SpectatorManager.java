package io.github.KennedySovine.hungerGames.spectator;

import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal SpectatorManager skeleton used by the GUI listener and other systems.
 * Responsibilities:
 * - Track spectator-target relationships (who a spectator is currently watching)
 * - Provide simple setters/getters used by InventoryGuiListener
 */
public class SpectatorManager {

    // spectatorUuid -> targetUuid
    private final Map<UUID, UUID> spectatorTargets = new ConcurrentHashMap<>();

    public SpectatorManager() {}

    /**
     * Set a spectator's current target (when they click in the spectator GUI).
     */
    public void setSpectatorTarget(UUID spectatorUuid, UUID targetUuid) {
        if (spectatorUuid == null) return;
        if (targetUuid == null) {
            spectatorTargets.remove(spectatorUuid);
        } else {
            spectatorTargets.put(spectatorUuid, targetUuid);
        }
    }

    /**
     * Get the current target a spectator is watching, or null if none.
     */
    public UUID getSpectatorTarget(UUID spectatorUuid) {
        return spectatorTargets.get(spectatorUuid);
    }
}

