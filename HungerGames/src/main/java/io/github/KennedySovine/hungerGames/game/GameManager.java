package io.github.KennedySovine.hungerGames.game;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skeleton GameManager to manage game lifecycle and player participation.
 *
 * The implementation should manage per-arena games, schedules for chest refills
 * and border shrink, and provide join/leave APIs used by commands.
 */
public class GameManager {

    private final HungerGames plugin;
    private final Map<String, GameState> games = new ConcurrentHashMap<>();

    public GameManager(HungerGames plugin) {
        this.plugin = plugin;
    }

    public void startGame(String arenaId) {
        // TODO: start game lifecycle for arena
        games.put(arenaId, GameState.LOBBY);
    }

    public void stopGame(String arenaId) {
        // TODO: clean up game state and reset arena
        games.remove(arenaId);
    }

    public void join(Player player, String arenaId) {
        // TODO: add player to arena lobby; handle inventory save and teleports
    }

    public void leave(Player player) {
        // TODO: remove player from their current game and restore inventory
    }

    public GameState getGameState(String arenaId) {
        return games.getOrDefault(arenaId, GameState.FINISHED);
    }
}

