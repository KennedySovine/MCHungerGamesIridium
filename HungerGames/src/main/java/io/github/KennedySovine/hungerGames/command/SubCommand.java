package io.github.KennedySovine.hungerGames.command;

import org.bukkit.command.CommandSender;
import java.util.List;

/**
 * Contract for a single subcommand handled by the central /hg dispatcher.
 *
 * Implementations should be lightweight and avoid heavy side-effects in
 * constructors. All Bukkit API calls must run on the main thread.
 */
public interface SubCommand {

    /**
     * The primary name of the subcommand (e.g. "join", "arena:create").
     * Used for help output and registration.
     */
    String name();

    /**
     * One-line usage string, e.g. "/hg join <arenaName>". Used when showing help.
     */
    String usage();

    /**
     * Permission node required to execute this subcommand, or null if none.
     */
    String permission();

    /**
     * Execute the subcommand.
     *
     * @param sender the command sender (player or console)
     * @param args the arguments passed to the subcommand (not including the subcommand name)
     * @return true if the command was handled (Bukkit convention)
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Provide tab-completion suggestions for this subcommand.
     *
     * Implementations should return quickly and not perform blocking I/O.
     *
     * @param sender the command sender
     * @param args current arguments
     * @return a list of suggestions (may be empty)
     */
    List<String> tabComplete(CommandSender sender, String[] args);
}

