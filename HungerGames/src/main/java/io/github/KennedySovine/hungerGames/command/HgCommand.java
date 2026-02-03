package io.github.KennedySovine.hungerGames.command;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

/**
 * Central dispatcher for the /hg command. Holds a registry of SubCommand
 * implementations and routes execution and tab-completion to them.
 *
 * This class is intentionally small: heavy logic should live in subcommands
 * or manager classes. All subcommands should be registered during plugin
 * initialization (onEnable) via registerSubCommand(...).
 */
public class HgCommand implements CommandExecutor, TabCompleter {

    private final HungerGames plugin;
    private final Map<String, SubCommand> commands = new LinkedHashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    public HgCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a subcommand under the provided key and optional aliases.
     * The primary key should be lower-case and may contain spaces (e.g. "arena create").
     */
    public void registerSubCommand(String key, SubCommand cmd, String... aliases) {
        commands.put(key.toLowerCase(Locale.ROOT), cmd);
        for (String a : aliases) {
            aliasMap.put(a.toLowerCase(Locale.ROOT), key.toLowerCase(Locale.ROOT));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            //sendHelp(sender);
            return true;
        }

        // Find the longest registered command key that matches the start of args.
        String matchedKey = null;
        int matchedTokens = 0;
        String[] lowerArgs = Arrays.stream(args).map(s -> s.toLowerCase(Locale.ROOT)).toArray(String[]::new);
        for (String key : commands.keySet()) {
            String[] keyTokens = key.split("\\s+");
            if (keyTokens.length > lowerArgs.length) continue; // can't match
            boolean ok = true;
            for (int i = 0; i < keyTokens.length; i++) {
                if (!keyTokens[i].equals(lowerArgs[i])) {
                    ok = false;
                    break;
                }
            }
            if (ok && keyTokens.length > matchedTokens) {
                matchedKey = key;
                matchedTokens = keyTokens.length;
            }
        }

        // If no direct registered key matched, try alias map for single-token aliases
        if (matchedKey == null && args.length >= 1) {
            String first = args[0].toLowerCase(Locale.ROOT);
            String aliased = aliasMap.get(first);
            if (aliased != null && commands.containsKey(aliased)) {
                matchedKey = aliased;
                matchedTokens = 1;
            }
        }

        if (matchedKey == null) {
            sender.sendMessage("&cUnknown subcommand: " + args[0]);
            //sendHelp(sender);
            return true;
        }

        String resolved = aliasMap.getOrDefault(matchedKey, matchedKey);
        SubCommand cmd = commands.get(resolved);
        if (cmd == null) {
            sender.sendMessage("&cUnknown subcommand: " + matchedKey);
            return true;
        }

        // permission check
        String perm = cmd.permission();
        if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
            sender.sendMessage("&cYou do not have permission to perform this command.");
            return true;
        }

        // pass remaining args after the matched key tokens
        String[] subArgs = Arrays.copyOfRange(args, matchedTokens, args.length);
        try {
            return cmd.execute(sender, subArgs);
        } catch (Exception ex) {
            plugin.getLogger().severe("Error executing subcommand " + cmd.name());
            ex.printStackTrace();
            sender.sendMessage("&cAn internal error occurred while executing the command.");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0 || args.length == 1) {
            // suggest top-level subcommand keys (first token only) filtered by permission
            Set<String> seen = new HashSet<>();
            List<String> suggestions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> e : commands.entrySet()) {
                String key = e.getKey();
                String node = e.getValue().permission();
                if (node != null && !node.isEmpty() && !sender.hasPermission(node)) continue;
                String first = key.split("\\s+")[0];
                if (seen.add(first)) suggestions.add(first);
            }
            Collections.sort(suggestions);
            return suggestions;
        }

        // delegate to the matched subcommand's tabComplete (match longest key as above)
        String matchedKey = null;
        int matchedTokens = 0;
        String[] lowerArgs = Arrays.stream(args).map(s -> s.toLowerCase(Locale.ROOT)).toArray(String[]::new);
        for (String key : commands.keySet()) {
            String[] keyTokens = key.split("\\s+");
            if (keyTokens.length > lowerArgs.length) continue;
            boolean ok = true;
            for (int i = 0; i < keyTokens.length; i++) {
                if (!keyTokens[i].equals(lowerArgs[i])) {
                    ok = false;
                    break;
                }
            }
            if (ok && keyTokens.length > matchedTokens) {
                matchedKey = key;
                matchedTokens = keyTokens.length;
            }
        }

        if (matchedKey == null) return Collections.emptyList();
        String resolved = aliasMap.getOrDefault(matchedKey, matchedKey);
        SubCommand cmd = commands.get(resolved);
        if (cmd == null) return Collections.emptyList();
        String[] subArgs = Arrays.copyOfRange(args, matchedTokens, args.length);
        try {
            return cmd.tabComplete(sender, subArgs);
        } catch (Exception ex) {
            plugin.getLogger().warning("Tab-complete error for subcommand " + matchedKey + ": " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("\n&6HungerGames Commands &7(usage)");
        for (Map.Entry<String, SubCommand> e : commands.entrySet()) {
            SubCommand sc = e.getValue();
            String node = sc.permission();
            if (node != null && !node.isEmpty() && !sender.hasPermission(node)) continue;
            sender.sendMessage("&e/hg " + e.getKey() + " &7- " + sc.usage());
        }
    }
}

