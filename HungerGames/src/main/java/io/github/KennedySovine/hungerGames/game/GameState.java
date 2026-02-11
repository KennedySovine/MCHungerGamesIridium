package io.github.KennedySovine.hungerGames.game;

/**
 * Simple enum for game lifecycle states.
 * LOBBY - Arena is not yet started, players can join
 * COUNTDOWN - Game has started, players can join but cannot move
 * RUNNING - Game has begun, players can move and play
 * DEATHMATCH - Final phase of the game
 * FINISHED - Game is complete
 */
public enum GameState {
    LOBBY,
    COUNTDOWN,
    RUNNING,
    DEATHMATCH,
    FINISHED
}

