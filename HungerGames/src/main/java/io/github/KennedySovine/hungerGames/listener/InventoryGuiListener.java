package io.github.KennedySovine.hungerGames.listener;

import io.github.KennedySovine.hungerGames.HungerGames;
import io.github.KennedySovine.hungerGames.gui.ArenaEditorGui;
import io.github.KennedySovine.hungerGames.gui.ArenaListGui;
import io.github.KennedySovine.hungerGames.gui.SpectatorGui;
import io.github.KennedySovine.hungerGames.arena.Arena;
import io.github.KennedySovine.hungerGames.arena.ArenaManager;
import io.github.KennedySovine.hungerGames.game.GameManager;
import io.github.KennedySovine.hungerGames.game.GameState;
import io.github.KennedySovine.hungerGames.spectator.SpectatorManager;
import io.github.KennedySovine.hungerGames.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Optional;
import java.util.Locale;
import java.util.regex.Pattern;
import io.github.KennedySovine.hungerGames.gui.AnvilPrompt;

/**
 * Listener for GUI inventories. Prevents item movement and delegates actions
 * to the GUI controller classes.
 */
public class InventoryGuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();

        // Handle Spectator GUI clicks
        if (holder instanceof SpectatorGui.SpectatorHolder) {
            event.setCancelled(true);
            handleSpectatorGuiClick(event);
            return;
        }

        // Handle Arena List GUI clicks (select an arena to load)
        if (holder instanceof ArenaListGui.ListHolder) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;
            if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;
            Player clicker = (Player) event.getWhoClicked();
            if (!clicker.hasPermission("HungerGames.admin")) {
                MessageUtils.send(clicker, "&cYou do not have permission to load arenas.");
                return;
            }
            String arenaId = clicked.getItemMeta().getDisplayName();
            // Close inventory and run the load command as the player (will set center to player's location)
            clicker.closeInventory();
            clicker.performCommand("hg arena load " + arenaId);
            return;
        }

        // Handle Editor GUI interactions
        if (!(holder instanceof ArenaEditorGui.EditorHolder)) return;

        // We handle clicks in our editor; cancel default behavior
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String disp = clicked.getItemMeta().getDisplayName();
        Player clicker = (Player) event.getWhoClicked();
        ArenaEditorGui.EditorHolder eh = (ArenaEditorGui.EditorHolder) holder;
        String arenaId = eh.getArenaId();

        // Permission check
        if (!clicker.hasPermission("HungerGames.admin")) {
            MessageUtils.send(clicker, "&cYou do not have permission to use the Arena Editor.");
            return;
        }

        // Handle Set Center click
        if (ArenaEditorGui.SET_CENTER_DISPLAY.equals(disp)) {
            // Set the working arena center to this player's location
            ArenaManager mgr = HungerGames.getPlugin(HungerGames.class).getArenaManager();
            Optional<io.github.KennedySovine.hungerGames.arena.Arena> wa = mgr.getWorkingArena();
            if (wa.isEmpty()) {
                MessageUtils.send(clicker, "&cNo working arena loaded. Load or create one first.");
                return;
            }
            wa.get().setLobbyLocation(clicker.getLocation());
            MessageUtils.send(clicker, "&aSet working arena center to your current location.");
            // update center beacon
            String wid = wa.get().getId();
            HungerGames.getPlugin(HungerGames.class).getParticleManager().showCenter(wid, clicker.getLocation());
            // refresh spawn beacons relative to new center
            HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid, wa.get());
            return;
        }

        // Other GUI actions
        switch (disp) {
            case "Give Spawn Stick":
                if (arenaId == null || arenaId.isEmpty()) {
                    MessageUtils.send(clicker, "&cNo working arena loaded to bind the spawn stick to.");
                    return;
                }
                ArenaEditorGui gui = new ArenaEditorGui();
                gui.giveSpawnStick(clicker, arenaId);
                MessageUtils.send(clicker, "&aGave you a spawn stick bound to arena " + arenaId + ". Right-click to add spawns.");
                return;
            case "Add Spawn Point":
                // Delegate to existing command (player form)
                clicker.performCommand("hg arena addspawn");
                return;
            case "Remove Spawn Point":
                clicker.performCommand("hg arena removespawn");
                return;
            case "Save Arena":
                clicker.performCommand("hg arena save");
                return;
            case "Load Arena":
                // open list GUI to pick an arena to load
                ArenaListGui.openFor(clicker);
                return;
            case "Create Arena":
                // Open an anvil prompt so the admin can type an arena id (and optional display name separated by a space)
                AnvilPrompt.open(clicker, "Create Arena - enter id [displayName]", "my_arena Display Name", text -> {
                    // parse id and optional display name
                    String[] parts = text.split(" ", 2);
                    String rawId = parts[0].trim();
                    String id = rawId.toLowerCase(Locale.ROOT);
                    String display = parts.length > 1 ? parts[1].trim() : id;

                    // Validate id: only lowercase letters, numbers, '_' and '-' allowed; length 1-64
                    Pattern valid = Pattern.compile("^[a-z0-9_-]{1,64}$");
                    if (!valid.matcher(id).matches()) {
                        MessageUtils.send(clicker, "&cInvalid arena id. Use 1-64 chars: lowercase a-z, digits, '-' or '_'.");
                        return;
                    }

                    // Create arena and persist; createArena normalizes id internally but we pass lowercase id
                    boolean ok = HungerGames.getPlugin(HungerGames.class).getArenaManager().createArena(id, display) != null;
                    if (!ok) {
                        MessageUtils.send(clicker, "&cArena with id '" + id + "' already exists.");
                        return;
                    }
                    // Save arenas to disk, load into working, set center to player's location, refresh beacons
                    HungerGames.getPlugin(HungerGames.class).getArenaManager().saveArenas();
                    HungerGames.getPlugin(HungerGames.class).getArenaManager().loadWorkingArena(id);
                    HungerGames.getPlugin(HungerGames.class).getArenaManager().getWorkingArena().ifPresent(a -> {
                        a.setLobbyLocation(clicker.getLocation());
                        String wid = a.getId();
                        HungerGames.getPlugin(HungerGames.class).getParticleManager().showCenter(wid, clicker.getLocation());
                        HungerGames.getPlugin(HungerGames.class).getParticleManager().refreshAllSpawns(wid, a);
                    });
                    MessageUtils.send(clicker, "&aCreated and loaded arena: " + id);
                });
                return;
            default:
                // unknown item, ignore
                return;
        }
    }

    /**
     * Handles clicks in the spectator GUI.
     */
    private void handleSpectatorGuiClick(InventoryClickEvent event) {
        Player clicker = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        HungerGames plugin = HungerGames.getPlugin(HungerGames.class);
        GameManager gameManager = plugin.getGameManager();
        SpectatorManager spectatorManager = plugin.getSpectatorManager();
        ArenaManager arenaManager = plugin.getArenaManager();
        
        Optional<Arena> arenaOpt = arenaManager.getWorkingArena();
        if (arenaOpt.isEmpty()) {
            clicker.closeInventory();
            MessageUtils.send(clicker, "&cNo arena is currently active!");
            return;
        }
        
        Arena arena = arenaOpt.get();
        String arenaId = arena.getId();
        GameState state = gameManager.getGameState(arenaId);
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Handle LOBBY menu clicks
        if (state == GameState.LOBBY || state == GameState.COUNTDOWN) {
            if ("§a§lJOIN".equals(displayName)) {
                clicker.closeInventory();
                clicker.performCommand("hg join");
            } else if ("§b§lSPECTATE".equals(displayName)) {
                clicker.closeInventory();
                MessageUtils.send(clicker, "&7You are already spectating.");
            }
            return;
        }
        
        // Handle RUNNING menu clicks
        if (state == GameState.RUNNING || state == GameState.DEATHMATCH) {
            // Handle player head clicks (teleport to player)
            if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta() instanceof SkullMeta) {
                SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                if (meta.getOwningPlayer() != null) {
                    Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                    if (target != null && target.isOnline()) {
                        clicker.teleport(target.getLocation());
                        spectatorManager.setSpectatorTarget(clicker.getUniqueId(), target.getUniqueId());
                        clicker.closeInventory();
                        // Update action bar to show who they're spectating
                        clicker.sendActionBar("§7Currently Spectating: §e" + target.getName());
                    } else {
                        MessageUtils.send(clicker, "&cThat player is no longer available.");
                        clicker.closeInventory();
                    }
                }
            }
            // Handle queue button clicks
            else if ("§a§lJOIN QUEUE".equals(displayName)) {
                boolean added = spectatorManager.addToQueue(clicker.getUniqueId());
                if (added) {
                    int position = spectatorManager.getQueuePosition(clicker.getUniqueId());
                    MessageUtils.send(clicker, "&aYou joined the queue! Position: &e" + position);
                    clicker.closeInventory();
                    // Reopen to show updated status
                    Bukkit.getScheduler().runTaskLater(plugin, () -> SpectatorGui.openFor(clicker), 1L);
                } else {
                    if (spectatorManager.getQueueSize() >= spectatorManager.getMaxQueueSize()) {
                        MessageUtils.send(clicker, "&cThe queue is full!");
                    } else {
                        MessageUtils.send(clicker, "&cYou are already in the queue!");
                    }
                }
            } else if ("§c§lLEAVE QUEUE".equals(displayName)) {
                spectatorManager.removeFromQueue(clicker.getUniqueId());
                MessageUtils.send(clicker, "&7You left the queue.");
                clicker.closeInventory();
                // Reopen to show updated status
                Bukkit.getScheduler().runTaskLater(plugin, () -> SpectatorGui.openFor(clicker), 1L);
            }
        }
    }
}
