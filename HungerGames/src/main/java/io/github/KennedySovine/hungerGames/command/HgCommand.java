package io.github.KennedySovine.hungerGames.command;

import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Central dispatcher for the /hg command. Holds a registry of SubCommand
 * implementations and routes execution to them.
 *
 * NOTE: Tab-completion logic has been removed for now per project requirements.
 */
public class HgCommand implements CommandExecutor {

    private final HungerGames plugin;
    private final Map<String, SubCommand> commands = new LinkedHashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    public HgCommand(HungerGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a subcommand under the provided key and optional aliases.
     * The primary key should be lower-case.
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
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        String resolved = aliasMap.getOrDefault(sub, sub);
        SubCommand cmd = commands.get(resolved);
        if (cmd == null) {
            sender.sendMessage("&cUnknown subcommand: " + args[0]);
            sendHelp(sender);
            return true;
        }

        // permission check
        String perm = cmd.permission();
        if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
            sender.sendMessage("&cYou do not have permission to perform this command.");
            return true;
        }

        // pass remaining args (without the subcommand token)
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        try {
            return cmd.execute(sender, subArgs);
        } catch (Exception ex) {
            plugin.getLogger().severe("Error executing subcommand " + cmd.name());
            ex.printStackTrace();
            sender.sendMessage("&cAn internal error occurred while executing the command.");
            return true;
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
