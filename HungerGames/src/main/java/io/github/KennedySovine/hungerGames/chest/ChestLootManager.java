package io.github.KennedySovine.hungerGames.chest;

import io.github.KennedySovine.hungerGames.arena.Arena;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Populates and refills arena chests with progressive loot tiers.
 * Loot table is loaded from chest-loot.yml under chest-loot.{early|mid|late}.
 */
public class ChestLootManager {
    private static final double CUSTOM_BORDER_THRESHOLD = 10_000_000D;

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
        private final double enchantChance;
        private final List<Map.Entry<Enchantment, Integer>> enchants;

        private LootEntry(Material material, int minAmount, int maxAmount, int weight, double enchantChance, List<Map.Entry<Enchantment, Integer>> enchants) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.weight = weight;
            this.enchantChance = enchantChance;
            this.enchants = enchants;
        }
    }

    private static class LootTable {
        private final int minRolls;
        private final int maxRolls;
        private final double defaultEnchantChance;
        private final List<LootEntry> entries;

        private LootTable(int minRolls, int maxRolls, double defaultEnchantChance, List<LootEntry> entries) {
            this.minRolls = minRolls;
            this.maxRolls = maxRolls;
            this.defaultEnchantChance = defaultEnchantChance;
            this.entries = entries;
        }
    }

    private final Plugin plugin;
    private LootTable earlyTable;
    private LootTable midTable;
    private LootTable lateTable;

    public ChestLootManager(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration cfg = loadLootConfig();
        this.earlyTable = loadTable(cfg, "chest-loot.early", defaultEarlyTable());
        this.midTable = loadTable(cfg, "chest-loot.mid", defaultMidTable());
        this.lateTable = loadTable(cfg, "chest-loot.late", defaultLateTable());
    }

    private FileConfiguration loadLootConfig() {
        File file = new File(plugin.getDataFolder(), "chest-loot.yml");
        if (!file.exists()) {
            plugin.saveResource("chest-loot.yml", false);
        }
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void populateChest(Inventory inv) {
        populateChest(inv, LootStage.EARLY);
    }

    private void populateChest(Inventory inv, LootStage stage) {
        inv.clear();
        LootTable table = getLootTable(stage);
        if (table.entries.isEmpty()) return;

        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int rolls = rng.nextInt(table.minRolls, table.maxRolls + 1);
        for (int i = 0; i < rolls; i++) {
            LootEntry entry = pickWeighted(table.entries, rng);
            if (entry == null) break;

            int amount = rng.nextInt(entry.minAmount, entry.maxAmount + 1);
            ItemStack stack = new ItemStack(entry.material, Math.max(1, amount));
            maybeApplyEnchant(stack, table.defaultEnchantChance, entry, rng);
            placeRandom(inv, stack, rng);
        }
    }

    public int refillArenaChests(Arena arena, LootStage stage) {
        if (arena == null || arena.getLobbyLocation() == null || arena.getLobbyLocation().getWorld() == null) return 0;

        int refilled = 0;
        for (Chunk chunk : arena.getLobbyLocation().getWorld().getLoadedChunks()) {
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

    private LootTable getLootTable(LootStage stage) {
        return switch (stage) {
            case MID -> midTable;
            case LATE -> lateTable;
            default -> earlyTable;
        };
    }

    private LootEntry pickWeighted(List<LootEntry> entries, ThreadLocalRandom rng) {
        int total = 0;
        for (LootEntry entry : entries) total += Math.max(0, entry.weight);
        if (total <= 0) return null;

        int pick = rng.nextInt(total);
        int running = 0;
        for (LootEntry entry : entries) {
            running += Math.max(0, entry.weight);
            if (pick < running) return entry;
        }
        return entries.get(entries.size() - 1);
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

    private void maybeApplyEnchant(ItemStack stack, double tableEnchantChance, LootEntry entry, ThreadLocalRandom rng) {
        if (entry.enchants.isEmpty()) return;
        double chance = entry.enchantChance >= 0 ? entry.enchantChance : tableEnchantChance;
        if (chance <= 0 || rng.nextDouble() > chance) return;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        Map.Entry<Enchantment, Integer> selected = entry.enchants.get(rng.nextInt(entry.enchants.size()));
        meta.addEnchant(selected.getKey(), selected.getValue(), true);
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
        boolean hasCustomWorldBorder = wb != null && wb.getSize() < CUSTOM_BORDER_THRESHOLD;
        boolean insideWorldBorder = !hasCustomWorldBorder || wb.isInside(location);
        return insideArenaBounds && insideWorldBorder;
    }

    private LootTable loadTable(FileConfiguration cfg, String basePath, LootTable fallback) {
        ConfigurationSection section = cfg.getConfigurationSection(basePath);
        if (section == null) return fallback;

        int minRolls = Math.max(1, section.getInt("min-rolls", fallback.minRolls));
        int maxRolls = Math.max(minRolls, section.getInt("max-rolls", fallback.maxRolls));
        double defaultEnchantChance = clampChance(section.getDouble("enchant-chance", fallback.defaultEnchantChance));
        List<LootEntry> entries = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList("items")) {
            LootEntry entry = parseEntry(map, defaultEnchantChance);
            if (entry != null) entries.add(entry);
        }
        return entries.isEmpty() ? fallback : new LootTable(minRolls, maxRolls, defaultEnchantChance, entries);
    }

    private LootEntry parseEntry(Map<?, ?> map, double defaultEnchantChance) {
        if (map == null) return null;
        String materialName = String.valueOf(map.get("material"));
        Material material = Material.matchMaterial(materialName);
        if (material == null || material == Material.AIR) return null;

        int min = getInt(map, "min", 1);
        int max = getInt(map, "max", min);
        if (max < min) max = min;
        int weight = Math.max(0, getInt(map, "weight", 1));
        if (weight <= 0) return null;

        double enchantChance = defaultEnchantChance;
        if (map.containsKey("enchant-chance")) {
            Object raw = map.get("enchant-chance");
            if (raw instanceof Number n) enchantChance = clampChance(n.doubleValue());
        }

        List<Map.Entry<Enchantment, Integer>> enchants = new ArrayList<>();
        Object rawEnchants = map.get("enchants");
        if (rawEnchants instanceof List<?> enchantList) {
            for (Object enchantObj : enchantList) {
                if (!(enchantObj instanceof String s)) continue;
                String[] parts = s.split(":");
                if (parts.length != 2) continue;
                Enchantment ench = Enchantment.getByName(parts[0].trim().toUpperCase());
                if (ench == null) continue;
                int level;
                try {
                    level = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (level <= 0) continue;
                enchants.add(Map.entry(ench, level));
            }
        }

        return new LootEntry(material, Math.max(1, min), Math.max(1, max), weight, enchantChance, enchants);
    }

    private int getInt(Map<?, ?> map, String key, int defaultValue) {
        Object raw = map.get(key);
        if (raw instanceof Number n) return n.intValue();
        if (raw instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double clampChance(double chance) {
        if (chance < 0) return 0;
        return Math.min(1, chance);
    }

    private LootTable defaultEarlyTable() {
        List<LootEntry> entries = new ArrayList<>();
        add(entries, Material.WOODEN_SWORD, 1, 1, 8, 0.10, "SHARPNESS:1");
        add(entries, Material.STONE_SWORD, 1, 1, 6, 0.10, "SHARPNESS:1");
        add(entries, Material.BOW, 1, 1, 4, 0.10, "POWER:1");
        add(entries, Material.ARROW, 4, 12, 9, 0, null);
        add(entries, Material.BREAD, 1, 3, 10, 0, null);
        add(entries, Material.COOKED_BEEF, 1, 2, 7, 0, null);
        add(entries, Material.LEATHER_HELMET, 1, 1, 5, 0, null);
        add(entries, Material.LEATHER_CHESTPLATE, 1, 1, 5, 0, null);
        add(entries, Material.LEATHER_LEGGINGS, 1, 1, 5, 0, null);
        add(entries, Material.LEATHER_BOOTS, 1, 1, 5, 0, null);
        add(entries, Material.CHAINMAIL_HELMET, 1, 1, 3, 0, null);
        add(entries, Material.CHAINMAIL_CHESTPLATE, 1, 1, 3, 0, null);
        add(entries, Material.CHAINMAIL_LEGGINGS, 1, 1, 3, 0, null);
        add(entries, Material.CHAINMAIL_BOOTS, 1, 1, 3, 0, null);
        add(entries, Material.IRON_HELMET, 1, 1, 2, 0, null);
        add(entries, Material.IRON_CHESTPLATE, 1, 1, 1, 0, null);
        add(entries, Material.IRON_LEGGINGS, 1, 1, 1, 0, null);
        add(entries, Material.IRON_BOOTS, 1, 1, 2, 0, null);
        return new LootTable(3, 6, 0.10, entries);
    }

    private LootTable defaultMidTable() {
        List<LootEntry> entries = new ArrayList<>(defaultEarlyTable().entries);
        add(entries, Material.IRON_SWORD, 1, 1, 4, 0.20, "SHARPNESS:1");
        add(entries, Material.IRON_AXE, 1, 1, 3, 0.20, "SHARPNESS:1");
        add(entries, Material.CROSSBOW, 1, 1, 2, 0.20, "QUICK_CHARGE:1");
        add(entries, Material.GOLDEN_APPLE, 1, 1, 2, 0, null);
        return new LootTable(4, 7, 0.20, entries);
    }

    private LootTable defaultLateTable() {
        List<LootEntry> entries = new ArrayList<>(defaultMidTable().entries);
        add(entries, Material.DIAMOND_SWORD, 1, 1, 2, 0.33, "SHARPNESS:2");
        add(entries, Material.DIAMOND_HELMET, 1, 1, 1, 0.33, "PROTECTION:2");
        add(entries, Material.DIAMOND_CHESTPLATE, 1, 1, 1, 0.33, "PROTECTION:2");
        add(entries, Material.DIAMOND_LEGGINGS, 1, 1, 1, 0.33, "PROTECTION:2");
        add(entries, Material.DIAMOND_BOOTS, 1, 1, 1, 0.33, "PROTECTION:2");
        add(entries, Material.GOLDEN_APPLE, 1, 1, 3, 0, null);
        add(entries, Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 1, 0, null);
        return new LootTable(5, 8, 0.33, entries);
    }

    private void add(List<LootEntry> entries, Material material, int min, int max, int weight, double enchantChance, String enchantSpec) {
        List<Map.Entry<Enchantment, Integer>> parsed = new ArrayList<>();
        if (enchantSpec != null && !enchantSpec.isBlank()) {
            String[] parts = enchantSpec.split(":");
            if (parts.length == 2) {
                Enchantment enchantment = Enchantment.getByName(parts[0].trim().toUpperCase());
                if (enchantment != null) {
                    try {
                        int level = Integer.parseInt(parts[1].trim());
                        if (level > 0) parsed.add(Map.entry(enchantment, level));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        entries.add(new LootEntry(material, min, max, weight, clampChance(enchantChance), parsed));
    }
}
