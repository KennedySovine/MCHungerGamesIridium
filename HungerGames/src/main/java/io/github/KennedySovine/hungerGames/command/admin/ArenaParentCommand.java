package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.command.SubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Parent dispatcher for arena-related admin subcommands. Registered as
 * a single `/hg arena` subcommand on the main dispatcher. It handles
 * create/load/save/addspawn/removespawn/list internally and delegates to
 * the concrete subcommand implementations.
 */
public class ArenaParentCommand extends AbstractSubCommand {

    private final Map<String, SubCommand> children = new LinkedHashMap<>();

    public ArenaParentCommand() {
        // Register child subcommands keyed by the second token (e.g. "create")
        registerChild("create", new CreateArenaCommand());
        registerChild("load", new ArenaLoadCommand());
        registerChild("save", new ArenaSaveCommand());
        registerChild("addspawn", new AddSpawnCommand());
        registerChild("removespawn", new RemoveSpawnCommand());
        registerChild("setcenter", new SetCenterCommand());
        registerChild("list", new ArenaListCommand());
    }

    private void registerChild(String key, SubCommand cmd) {
        children.put(key.toLowerCase(Locale.ROOT), cmd);
    }

    @Override
    public String name() {
        return "arena";
    }

    @Override
    public String usage() {
        return "/hg arena <create|load|save|addspawn|removespawn|list> [args]";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageUtils.send(sender, "&6Arena subcommands:");
            for (Map.Entry<String, SubCommand> e : children.entrySet()) {
                SubCommand sc = e.getValue();
                String node = sc.permission();
                if (node != null && !node.isEmpty() && !sender.hasPermission(node)) continue;
                MessageUtils.send(sender, " &e" + e.getKey() + " &7- " + sc.usage());
            }
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        SubCommand child = children.get(sub);
        if (child == null) {
            MessageUtils.send(sender, "&cUnknown arena subcommand: " + args[0]);
            return true;
        }
        // permission check for the child
        String perm = child.permission();
        if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
            MessageUtils.send(sender, "&cYou do not have permission to perform this command.");
            return true;
        }
        String[] childArgs = Arrays.copyOfRange(args, 1, args.length);
        try {
            return child.execute(sender, childArgs);
        } catch (Exception ex) {
            MessageUtils.send(sender, "&cAn internal error occurred while executing the arena subcommand.");
            ex.printStackTrace();
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> e : children.entrySet()) {
                String node = e.getValue().permission();
                if (node != null && !node.isEmpty() && !sender.hasPermission(node)) continue;
                suggestions.add(e.getKey());
            }
            Collections.sort(suggestions);
            return suggestions;
        }
        // Delegate tab completion to the child
        String sub = args[0].toLowerCase(Locale.ROOT);
        SubCommand child = children.get(sub);
        if (child == null) return Collections.emptyList();
        String[] childArgs = Arrays.copyOfRange(args, 1, args.length);
        try {
            return child.tabComplete(sender, childArgs);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
