package io.github.KennedySovine.hungerGames.command.admin;

import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.command.AbstractSubCommand;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.KennedySovine.hungerGames.HungerGames;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /hg arena save
 *
 * Persist the currently loaded working arena back to the master arenas list
 * and to disk. This will throw an error if the working arena's id does not
 * exist in the master list (i.e., the arena was not created or persisted).
 */
public class ArenaSaveCommand extends AbstractSubCommand {

    @Override
    public String name() {
        return "arena save";
    }

    @Override
    public String usage() {
        return "/hg arena save";
    }

    @Override
    public String permission() {
        return "HungerGames.admin";
    }

    /**
     * Execute the save command.
     *
     * Parameters:
     * - sender: the command issuer
     * - args: none expected
     *
     * Behavior:
     * - Requires permission: HungerGames.admin
     * - Attempts to persist the in-memory working arena back to the master arenas and write to disk.
     * - Returns true after processing and prints an error message if saving is not possible.
     */
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        ArenaManager mgr = JavaPlugin.getPlugin(io.github.KennedySovine.hungerGames.HungerGames.class).getArenaManager();
        try {
            mgr.saveWorkingArena();
            // Reset beacons for the saved arena so they reflect any persisted changes
            mgr.getWorkingArena().ifPresent(a -> {
                String id = a.getId();
                HungerGames.getPlugin(HungerGames.class).getParticleManager().clearAllForArena(id);
                HungerGames.getPlugin(HungerGames.class).getParticleManager().showCenter(id, a.getLobbyLocation());
                HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(id, a);
            });
            // Remove spawn sticks from the command sender if they are a player
            if (sender instanceof Player) {
                Player p = (Player) sender;
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
                    // also remove offhand
                    ItemStack off = p.getInventory().getItemInOffHand();
                    if (off != null && off.hasItemMeta() && off.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                        p.getInventory().setItemInOffHand(null);
                    }
                    p.updateInventory();
                }
            }
            MessageUtils.send(sender, "&aWorking arena persisted to disk.");
        } catch (IllegalStateException ex) {
            MessageUtils.send(sender, "&cCannot save: " + ex.getMessage());
        }
        return true;
    }
}
