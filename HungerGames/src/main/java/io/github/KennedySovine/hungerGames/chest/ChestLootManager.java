package io.github.KennedySovine.hungerGames.chest;

import io.github.KennedySovine.hungerGames.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Populates and refills arena chests with progressive loot tiers.
 */
public class ChestLootManager {

    public enum LootStage {
        EARLY,
        MID,
        LATE
    }

    private static class LootEntry {
        private final Material material;
        private final int minAmount;
        private final int maxAmount;
        private final int weight;
        private final boolean canRollEnchant;

        private LootEntry(Material material, int minAmount, int maxAmount, int weight, boolean canRollEnchant) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.weight = weight;
            this.canRollEnchant = canRollEnchant;
        }
    }

    private final org.bukkit.plugin.Plugin plugin;
    private final List<LootEntry> earlyLoot = new ArrayList<>();
    private final List<LootEntry> midLoot = new ArrayList<>();
    private final List<LootEntry> lateLoot = new ArrayList<>();

    public ChestLootManager(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload loot definitions from the config.yml (`chest-loot` section).
     */
    public void reload() {
        FileConfiguration cfg = plugin.getConfig();
        // Keep compatibility with earlier config-based plans; currently we provide
        // a built-in standard loot table with progressive tiers.
        if (!cfg.isConfigurationSection("chest-loot")) {
            plugin.getLogger().fine("No chest-loot section found; using built-in loot tiers.");
        }

        earlyLoot.clear();
        midLoot.clear();
        lateLoot.clear();

        // Start loot: capped at iron armor level.
        earlyLoot.add(new LootEntry(Material.WOODEN_SWORD, 1, 1, 8, true));
        earlyLoot.add(new LootEntry(Material.STONE_SWORD, 1, 1, 6, true));
        earlyLoot.add(new LootEntry(Material.BOW, 1, 1, 4, true));
        earlyLoot.add(new LootEntry(Material.ARROW, 4, 12, 9, false));
        earlyLoot.add(new LootEntry(Material.BREAD, 1, 3, 10, false));
        earlyLoot.add(new LootEntry(Material.COOKED_BEEF, 1, 2, 7, false));
        earlyLoot.add(new LootEntry(Material.LEATHER_HELMET, 1, 1, 5, false));
        earlyLoot.add(new LootEntry(Material.LEATHER_CHESTPLATE, 1, 1, 5, false));
        earlyLoot.add(new LootEntry(Material.LEATHER_LEGGINGS, 1, 1, 5, false));
        earlyLoot.add(new LootEntry(Material.LEATHER_BOOTS, 1, 1, 5, false));
        earlyLoot.add(new LootEntry(Material.CHAINMAIL_HELMET, 1, 1, 3, false));
        earlyLoot.add(new LootEntry(Material.CHAINMAIL_CHESTPLATE, 1, 1, 3, false));
        earlyLoot.add(new LootEntry(Material.CHAINMAIL_LEGGINGS, 1, 1, 3, false));
        earlyLoot.add(new LootEntry(Material.CHAINMAIL_BOOTS, 1, 1, 3, false));
        earlyLoot.add(new LootEntry(Material.IRON_HELMET, 1, 1, 2, false));
        earlyLoot.add(new LootEntry(Material.IRON_CHESTPLATE, 1, 1, 1, false));
        earlyLoot.add(new LootEntry(Material.IRON_LEGGINGS, 1, 1, 1, false));
        earlyLoot.add(new LootEntry(Material.IRON_BOOTS, 1, 1, 2, false));

        // First refill: improved iron-tier options.
        midLoot.addAll(earlyLoot);
        midLoot.add(new LootEntry(Material.IRON_SWORD, 1, 1, 4, true));
        midLoot.add(new LootEntry(Material.IRON_AXE, 1, 1, 3, true));
        midLoot.add(new LootEntry(Material.CROSSBOW, 1, 1, 2, true));
        midLoot.add(new LootEntry(Material.GOLDEN_APPLE, 1, 1, 2, false));

        // Late refill: diamond tier and rare high-value items.
        lateLoot.addAll(midLoot);
        lateLoot.add(new LootEntry(Material.DIAMOND_SWORD, 1, 1, 2, true));
        lateLoot.add(new LootEntry(Material.DIAMOND_HELMET, 1, 1, 1, true));
        lateLoot.add(new LootEntry(Material.DIAMOND_CHESTPLATE, 1, 1, 1, true));
        lateLoot.add(new LootEntry(Material.DIAMOND_LEGGINGS, 1, 1, 1, true));
        lateLoot.add(new LootEntry(Material.DIAMOND_BOOTS, 1, 1, 1, true));
        lateLoot.add(new LootEntry(Material.GOLDEN_APPLE, 1, 1, 3, false));
        lateLoot.add(new LootEntry(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 1, false));
    }

    /**
     * Populate the provided chest inventory with loot according to the loaded table.
     */
    public void populateChest(Inventory inv) {
        populateChest(inv, LootStage.EARLY);
    }

    private void populateChest(Inventory inv, LootStage stage) {
        inv.clear();
        List<LootEntry> table = getLootTable(stage);
        if (table.isEmpty()) return;

        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int minRolls;
        int maxRolls;
        switch (stage) {
            case MID -> {
                minRolls = 4;
                maxRolls = 7;
            }
            case LATE -> {
                minRolls = 5;
                maxRolls = 8;
            }
            default -> {
                minRolls = 3;
                maxRolls = 6;
            }
        }

        int rolls = rng.nextInt(minRolls, maxRolls + 1);
        for (int i = 0; i < rolls; i++) {
            LootEntry entry = pickWeighted(table, rng);
            if (entry == null) break;
            int amount = rng.nextInt(entry.minAmount, entry.maxAmount + 1);
            ItemStack stack = new ItemStack(entry.material, Math.max(1, amount));
            maybeApplyEnchant(stack, stage, entry.canRollEnchant, rng);
            placeRandom(inv, stack, rng);
        }
    }

    public int refillArenaChests(Arena arena, LootStage stage) {
        if (arena == null || arena.getLobbyLocation() == null || arena.getLobbyLocation().getWorld() == null) {
            return 0;
        }

        int refilled = 0;
        for (org.bukkit.Chunk chunk : arena.getLobbyLocation().getWorld().getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (!(state instanceof Chest chest)) continue;
                Location chestLocation = chest.getLocation();
                if (!isInsideArenaBorder(arena, chestLocation)) continue;
                populateChest(chest.getBlockInventory(), stage);
                refilled++;
            }
        }
        return refilled;
    }

    private List<LootEntry> getLootTable(LootStage stage) {
        return switch (stage) {
            case MID -> midLoot;
            case LATE -> lateLoot;
            default -> earlyLoot;
        };
    }

    private LootEntry pickWeighted(List<LootEntry> table, ThreadLocalRandom rng) {
        int total = 0;
        for (LootEntry e : table) total += Math.max(0, e.weight);
        if (total <= 0) return null;

        int pick = rng.nextInt(total);
        int running = 0;
        for (LootEntry e : table) {
            running += Math.max(0, e.weight);
            if (pick < running) return e;
        }
        return table.get(table.size() - 1);
    }

    private void placeRandom(Inventory inv, ItemStack stack, ThreadLocalRandom rng) {
        List<Integer> slots = new ArrayList<>(inv.getSize());
        for (int i = 0; i < inv.getSize(); i++) slots.add(i);
        Collections.shuffle(slots, rng);
        for (int slot : slots) {
            if (inv.getItem(slot) == null) {
                inv.setItem(slot, stack);
                return;
            }
        }
    }

    private void maybeApplyEnchant(ItemStack stack, LootStage stage, boolean canRollEnchant, ThreadLocalRandom rng) {
        if (!canRollEnchant) return;
        double chance = switch (stage) {
            case MID -> 0.20;
            case LATE -> 0.33;
            default -> 0.10;
        };
        if (rng.nextDouble() > chance) return;

        Material material = stack.getType();
        Map<Enchantment, Integer> enchants = switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, DIAMOND_SWORD -> Map.of(Enchantment.SHARPNESS, stage == LootStage.LATE ? 2 : 1);
            case BOW, CROSSBOW -> Map.of(Enchantment.POWER, stage == LootStage.LATE ? 2 : 1);
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS,
                 CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS,
                 IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS,
                 DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS ->
                    Map.of(Enchantment.PROTECTION, stage == LootStage.LATE ? 2 : 1);
            default -> Collections.emptyMap();
        };
        if (enchants.isEmpty()) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            meta.addEnchant(e.getKey(), e.getValue(), true);
        }
        stack.setItemMeta(meta);
    }

    private boolean isInsideArenaBorder(Arena arena, Location location) {
        if (location == null || location.getWorld() == null || arena.getLobbyLocation() == null || arena.getLobbyLocation().getWorld() == null) {
            return false;
        }
        if (!location.getWorld().equals(arena.getLobbyLocation().getWorld())) {
            return false;
        }

        Location center = arena.getLobbyLocation();
        double half = Math.max(1, arena.getCenterSize()) / 2.0;
        boolean insideArenaBounds = Math.abs(location.getX() - center.getX()) <= half
                && Math.abs(location.getZ() - center.getZ()) <= half;

        WorldBorder wb = location.getWorld().getWorldBorder();
        boolean hasCustomWorldBorder = wb != null && wb.getSize() < 10_000_000;
        boolean insideWorldBorder = !hasCustomWorldBorder || wb.isInside(location);
        return insideArenaBounds && insideWorldBorder;
    }
}
