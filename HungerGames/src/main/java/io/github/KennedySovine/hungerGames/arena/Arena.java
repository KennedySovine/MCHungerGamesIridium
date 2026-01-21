package io.github.KennedySovine.hungerGames.arena;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for a HungerGames arena configuration.
 *
 * This class is a simple POJO used by ArenaManager. It stores arena settings
 * and spawn locations. Implementers should expand it as needed (e.g. validation).
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
    private final List<Location> spawns = new ArrayList<>();

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

    public List<Location> getSpawns() {
        return spawns;
    }

    public void addSpawn(Location loc) {
        spawns.add(loc);
    }

    public void removeSpawn(int index) {
        if (index >= 0 && index < spawns.size()) spawns.remove(index);
    }
}

