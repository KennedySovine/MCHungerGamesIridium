package io.github.KennedySovine.hungerGames.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Helper base class providing common utilities for SubCommand implementations.
 *
 * Keep this class lightweight — avoid holding references to mutable managers
 * unless passed in during execution. Use the protected helpers to convert
 * senders to players and to parse simple argument types.
 */
public abstract class AbstractSubCommand implements SubCommand {

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // Default: no suggestions. Subclasses may override.
        return Collections.emptyList();
    }

    /**
     * Safely convert a CommandSender to a Player, returning empty if not a player.
     * Subcommands that require a player should call this and send an error when empty.
     */
    protected Optional<Player> asPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return Optional.of((Player) sender);
        }
        return Optional.empty();
    }

    /**
     * Parse an integer argument returning the parsed value or an empty Optional
     * if parsing failed. Implementations should send an error message to the
     * sender when parsing fails.
     */
    protected Optional<Integer> parseInt(CommandSender sender, String token) {
        try {
            return Optional.of(Integer.parseInt(token));
        } catch (NumberFormatException ex) {
            sender.sendMessage("&cInvalid number: " + token);
            return Optional.empty();
        }
    }

    /**
     * Helper to get an arena name suggestions; Subclasses can call managers here.
     * This default implementation returns an empty list — override if you have
     * access to ArenaManager.
     */
    protected List<String> arenaNameSuggestions() {
        return Collections.emptyList();
    }
}

