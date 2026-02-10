package io.github.KennedySovine.hungerGames.utils;

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
 * The implementation draws a column of END_ROD particles from the spawn Y up to
 * the world max height on a repeating task and a small colored dust accent at
 * the exact Y of the spawn/center. The task runs until cancelled by the caller.
 */
public final class ParticleUtils {
    private ParticleUtils() {}

    public static BukkitTask showSpawnBeacon(JavaPlugin plugin, Location loc) {
        // white beam using END_ROD
        return showPersistentVerticalBeam(plugin, loc, null);
    }

    public static BukkitTask showCenterBeacon(JavaPlugin plugin, Location loc) {
        // center uses a yellow accent plus the same END_ROD beam
        return showPersistentVerticalBeam(plugin, loc, Color.fromRGB(255, 224, 0));
    }

    /**
     * Builds a persistent repeating task that spawns a vertical beam of END_ROD
     * particles from the provided location's Y up to the world max height every 5 ticks.
     * If 'accentColor' is non-null, a colored REDSTONE dust is spawned at the exact location Y.
     * Returns the BukkitTask so callers can cancel it to stop the beam.
     */
    public static BukkitTask showPersistentVerticalBeam(JavaPlugin plugin, Location loc, Color accentColor) {
        if (loc == null || loc.getWorld() == null) return null;
        World world = loc.getWorld();
        final double x = loc.getX() + 0.5;
        final double z = loc.getZ() + 0.5;
        final int startY = loc.getBlockY();
        final int maxY = world.getMaxHeight();
        final DustOptions dust = (accentColor != null) ? new DustOptions(accentColor, 1.0f) : null;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // vertical beam using END_ROD
                for (int y = startY; y < maxY; y++) {
                    Location particleLoc = new Location(world, x, y, z);
                    try {
                        world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                    } catch (Throwable t) {
                        // fail-safe: ignore
                    }
                }

                // accent at the exact spawn Y
                Location accentLoc = new Location(world, x, startY + 0.5, z);
                if (dust != null) {
                    try {
                        world.spawnParticle(Particle.valueOf("REDSTONE"), accentLoc, 1, 0, 0, 0, 0, dust);
                    } catch (Throwable t) {
                        try { world.spawnParticle(Particle.valueOf("SPELL"), accentLoc, 6, 0.2, 0.2, 0.2); } catch (Throwable ignore) {}
                    }
                } else {
                    // small burst to mark the spawn Y
                    try {
                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, accentLoc, 6, 0.2, 0.2, 0.2, 0);
                    } catch (Throwable t) {
                        // ignore
                    }
                }
            }
        };

        // run every 5 ticks (as per your example)
        return task.runTaskTimer(plugin, 0L, 5L);
    }
}
