package io.github.KennedySovine.hungerGames.stats;

import java.util.UUID;

/**
 * Simple data class for player statistics.
 */
public class PlayerStats {

    private final UUID playerId;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;

    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        wins++;
    }

    public int getLosses() {
        return losses;
    }

    public void addLoss() {
        losses++;
    }
}

