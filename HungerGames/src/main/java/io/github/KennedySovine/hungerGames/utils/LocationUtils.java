package io.github.KennedySovine.hungerGames.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Utility methods for serializing/deserializing Location objects to/from strings.
 */
public final class LocationUtils {

    private LocationUtils() {}

    /**
     * Format a Location as CSV: "world,x,y,z,yaw,pitch"
     */
    public static String serialize(Location loc) {
        if (loc == null) return "";
        return String.format("%s,%f,%f,%f,%f,%f",
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Parse a CSV location string. Returns null if parsing fails.
     */
    public static Location deserialize(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] parts = s.split(",");
        if (parts.length < 6) return null;
        World w = Bukkit.getWorld(parts[0]);
        if (w == null) return null;
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(w, x, y, z, yaw, pitch);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

