package io.github.KennedySovine.hungerGames.command.admin;

/**
 * ArenaParentCommand removed: command routing now registers multi-token arena subcommands
 * directly (e.g. "arena create", "arena load", "arena save", etc.).
 *
 * This class is retained as a harmless stub for existing references during development,
 * but it should not be registered or used. If instantiated, it will throw
 * UnsupportedOperationException to make misuse obvious.
 */
@Deprecated
public final class ArenaParentCommand {
    public ArenaParentCommand() {
        throw new UnsupportedOperationException("ArenaParentCommand has been removed; register multi-token arena subcommands instead (e.g. 'arena create').");
    }
}
