package io.github.KennedySovine.hungerGames.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Particle.DustOptions;

/**
 * Small utility to display vertical beacon-like particle effects at a location.
 * The implementation draws a column of REDSTONE dust up/down the world height
 * and a burst at the exact Y of the location to mark the spawn/center.
 */
public final class ParticleUtils {
    private ParticleUtils() {}

    public static BukkitTask showSpawnBeacon(JavaPlugin plugin, Location loc) {
        return showVerticalBeacon(plugin, loc, Color.fromRGB(255,255,255), 6);
    }

    public static BukkitTask showCenterBeacon(JavaPlugin plugin, Location loc) {
        return showVerticalBeacon(plugin, loc, Color.fromRGB(255,224,0), 8);
    }

    public static BukkitTask showVerticalBeacon(JavaPlugin plugin, Location loc, Color color, int durationSeconds) {
        if (loc == null || loc.getWorld() == null) return null;
        World world = loc.getWorld();
        final double x = loc.getX();
        final double z = loc.getZ();
        final double centerY = loc.getY();
        final int minY = world.getMinHeight();
        final int maxY = world.getMaxHeight();
        final DustOptions dust = new DustOptions(color, 1.0f);
        final int stepBlocks = 2; // sample every 2 blocks to reduce load
        final long intervalTicks = 2L; // run every 2 ticks
        final int iterations = Math.max(1, (durationSeconds * 20) / (int)intervalTicks);

        BukkitRunnable runnable = new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count++ >= iterations) {
                    cancel();
                    return;
                }
                // vertical column
                for (int y = minY; y <= maxY; y += stepBlocks) {
                    Location p = new Location(world, x, y, z);
                    try {
                        world.spawnParticle(Particle.valueOf("REDSTONE"), p, 1, 0.0, 0.0, 0.0, 0.0, dust);
                    } catch (IllegalArgumentException ex) {
                        // fallback: try generic particle names if available, else skip
                        try { world.spawnParticle(Particle.valueOf("SMOKE_NORMAL"), p, 1, 0.0, 0.0, 0.0); } catch (Exception ignore) {}
                    }
                }
                // accent at the exact spawn Y
                Location center = new Location(world, x, centerY, z);
                try {
                    world.spawnParticle(Particle.valueOf("SPELL_WITCH"), center, 40, 0.5, 0.5, 0.5, 0.02);
                } catch (IllegalArgumentException ex) {
                    try { world.spawnParticle(Particle.valueOf("SPELL"), center, 40, 0.5, 0.5, 0.5); } catch (Exception ignore) {}
                }
                try {
                    world.spawnParticle(Particle.valueOf("CAMPFIRE_COSY_SMOKE"), center, 8, 0.2, 0.2, 0.2, 0.0);
                } catch (IllegalArgumentException ex) {
                    // ignore fallback
                }
            }
        };
        return runnable.runTaskTimer(plugin, 0L, intervalTicks);
    }
}
