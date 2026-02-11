package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.ItemStack;

/**
 * /hg arena load <arenaName>
 *
 * Load an arena from the master list into the in-memory working placeholder.
 */
public class ArenaLoadCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena load";
    }

    @Override
    public String usage() {
        return "/hg arena load <arenaName>";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute the load command.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: args[0] = arenaId to load
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Copies the master arena into the working (in-memory) arena placeholder.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageUtils.send(sender, "&cUsage: " + usage());
            return true;
        }
        String arenaId = args[0];
        ArenaManager mgr = JavaPlugin.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getArenaManager();
        boolean ok = mgr.loadWorkingArena(arenaId);
        if (!ok) {
            MessageUtils.send(sender, "&cArena not found: " + arenaId);
            return true;
        }

        // If sender is a player, set the working arena center to their current location
        if (sender instanceof Player) {
            Player p = (Player) sender;
            mgr.getWorkingArena().ifPresent(a -> a.setLobbyLocation(p.getLocation()));

            // Remove any spawn sticks from the player's inventory now that a new arena is loaded
            NamespacedKey key = new NamespacedKey(HungerGames.getPlugin(HungerGames.class), io.github.KennedySovine.hungerGames.gui.ArenaEditorGui.SPAWN_STICK_KEY_NAME);
            int removed = 0;
            for (int i = 0; i < p.getInventory().getSize(); i++) {
                ItemStack it = p.getInventory().getItem(i);
                if (it == null || !it.hasItemMeta()) continue;
                if (it.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    p.getInventory().setItem(i, null);
                    removed++;
                }
            }
            if (removed > 0) {
                MessageUtils.send(p, "&aRemoved " + removed + " spawn stick(s) from your inventory.");
                // also remove from offhand if present
                ItemStack off = p.getInventory().getItemInOffHand();
                if (off != null && off.hasItemMeta() && off.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    p.getInventory().setItemInOffHand(null);
                }
                p.updateInventory();
            }

            MessageUtils.send(sender, "&aLoaded arena into working memory: " + arenaId + " and set center to your current location.");
            // show center beacon (yellow) and refresh spawn beacons
            String wid = mgr.getWorkingArena().map(a -> a.getId()).orElse("");
            // clear any previous beacons for a clean reset then re-show
            HungerGames.getPlugin(HungerGames.class).getParticleManager().clearAllForArena(wid);
            HungerGames.getPlugin(HungerGames.class).getParticleManager().showCenter(wid, p.getLocation());
            HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid, mgr.getWorkingArena().orElse(null));
            // apply world border for this arena (centered at player location)
            mgr.getWorkingArena().ifPresent(a -> HungerGames.getPlugin(HungerGames.class).getBorderManager().applyBorder(a.getId(), p.getLocation(), a.getCenterSize()));
            return true;
        }

        MessageUtils.send(sender, "&aLoaded arena into working memory: " + arenaId + " (further edit commands will use this arena by default)");
        return true;
    }
}
