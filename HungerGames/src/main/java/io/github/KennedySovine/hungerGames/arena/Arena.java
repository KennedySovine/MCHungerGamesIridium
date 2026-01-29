package io.github.KennedySovine.hungerGames.arena;

import io.github.KennedySovine.hungerGames.utils.LocationUtils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for a HungerGames arena configuration.
 *
 * This class stores configuration for an arena. Spawn points are stored as
 * relative-offset strings (see LocationUtils.serializeRelative). The intended
 * workflow:
 * - An admin stands in the arena center and runs the "add spawn" command.
 * - The arena's lobbyLocation must be set to the center before adding spawns.
 * - addSpawnRelative(center, loc) will store the offset string.
 * - When the arena is loaded/activated for a player, call
 *   getAbsoluteSpawns(currentCenter) to compute absolute spawn Locations.
 */
public class Arena {

    private final String id; // internal id (lowercase)
    private String displayName;
    private int minPlayers = 2;
    private int maxPlayers = 24;
    private int timeToShrinkSeconds = 600;
    private int centerSize = 200;
    private int gracePeriodSeconds = 120;
    private int chestRefillSeconds = 300;
    private Location lobbyLocation;

    // Spawn points stored as relative offset strings (dx,dy,dz,dyaw,dpitch)
    private final List<String> spawnOffsets = new ArrayList<>();

    public Arena(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getTimeToShrinkSeconds() {
        return timeToShrinkSeconds;
    }

    public void setTimeToShrinkSeconds(int timeToShrinkSeconds) {
        this.timeToShrinkSeconds = timeToShrinkSeconds;
    }

    public int getCenterSize() {
        return centerSize;
    }

    public void setCenterSize(int centerSize) {
        this.centerSize = centerSize;
    }

    public int getGracePeriodSeconds() {
        return gracePeriodSeconds;
    }

    public void setGracePeriodSeconds(int gracePeriodSeconds) {
        this.gracePeriodSeconds = gracePeriodSeconds;
    }

    public int getChestRefillSeconds() {
        return chestRefillSeconds;
    }

    public void setChestRefillSeconds(int chestRefillSeconds) {
        this.chestRefillSeconds = chestRefillSeconds;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    /**
     * Returns the raw relative-offset strings stored for this arena.
     */
    public List<String> getSpawnOffsets() {
        return spawnOffsets;
    }

    /**
     * Add a spawn offset string directly. The string must be in the format
     * produced by LocationUtils.serializeRelative(center, loc) or may be an
     * absolute location string produced by LocationUtils.serialize(loc) to
     * preserve compatibility with older files.
     */
    public void addSpawnOffset(String offsetStr) {
        spawnOffsets.add(offsetStr);
    }

    /**
     * Add a spawn by providing an absolute Location and the center Location
     * that was used when the admin added the spawn. This method will compute
     * and store the relative offset string.
     *
     * Note: callers are expected to pass the arena's lobbyLocation as the
     * center if they follow the admin workflow (admin stands in center).
     */
    public void addSpawnRelative(Location center, Location absoluteLoc) {
        String s = LocationUtils.serializeRelative(center, absoluteLoc);
        spawnOffsets.add(s);
    }

    /**
     * Remove the spawn offset at the given index. Safe if index invalid.
     */
    public void removeSpawn(int index) {
        if (index >= 0 && index < spawnOffsets.size()) spawnOffsets.remove(index);
    }

    /**
     * Compute absolute spawn Locations by applying the stored relative offsets
     * to the provided center Location. Behavior:
     * - If a stored string has 5 comma-separated parts it is treated as a
     *   relative offset and is applied to the provided center using
     *   LocationUtils.deserializeRelative.
     * - If a stored string has 6+ parts it is treated as a legacy absolute
     *   location and parsed via LocationUtils.deserialize.
     * Offsets that fail to parse are skipped.
     */
    public List<Location> getAbsoluteSpawns(Location center) {
        List<Location> out = new ArrayList<>();
        if (spawnOffsets.isEmpty()) return out;
        for (String s : spawnOffsets) {
            if (s == null || s.isEmpty()) continue;
            String[] parts = s.split(",");
            Location loc = null;
            if (parts.length >= 6) {
                // Legacy absolute location: world,x,y,z,yaw,pitch
                loc = LocationUtils.deserialize(s);
            } else if (parts.length == 5) {
                // Relative offset: dx,dy,dz,dyaw,dpitch
                if (center != null) loc = LocationUtils.deserializeRelative(center, s);
            }
            if (loc != null) out.add(loc);
        }
        return out;
    }
}
