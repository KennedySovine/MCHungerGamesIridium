package io.github.KennedySovine.hungerGames.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * Utility methods for serializing/deserializing Location objects to/from strings.
 *
 * Notes:
 * - serialize(Location) and deserialize(String) work with a world-qualified CSV.
 * - serializeRelative(Location center, Location loc) stores offsets (dx,dy,dz,dyaw,dpitch) relative to a center.
 * - deserializeRelative supports both orders via a small overload for convenience.
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

    /**
     * Serialize a location as an offset relative to the provided center.
     * Output format: "dx,dy,dz,dyaw,dpitch" (no world). The values are in
     * absolute units (blocks/coords) and degrees for rotation.
     *
     * Returns empty string if center or loc is null.
     */
    public static String serializeRelative(Location center, Location loc) {
        if (center == null || loc == null) return "";
        double dx = loc.getX() - center.getX();
        double dy = loc.getY() - center.getY();
        double dz = loc.getZ() - center.getZ();
        double dyaw = loc.getYaw() - center.getYaw();
        double dpitch = loc.getPitch() - center.getPitch();
        return String.format("%f,%f,%f,%f,%f", dx, dy, dz, dyaw, dpitch);
    }

    /**
     * Deserialize a relative-offset string using the provided center as the
     * origin. The string must be in the form produced by serializeRelative.
     * Returns null on parse error.
     *
     * @param center center location (provides world and base coords)
     * @param s      relative string produced by serializeRelative
     */
    public static Location deserializeRelative(Location center, String s) {
        if (center == null || s == null || s.isEmpty()) return null;
        String[] parts = s.split(",");
        if (parts.length < 5) return null;
        try {
            double dx = Double.parseDouble(parts[0]);
            double dy = Double.parseDouble(parts[1]);
            double dz = Double.parseDouble(parts[2]);
            float dyaw = Float.parseFloat(parts[3]);
            float dpitch = Float.parseFloat(parts[4]);
            World w = center.getWorld();
            float yaw = center.getYaw() + dyaw;
            float pitch = center.getPitch() + dpitch;
            return new Location(w, center.getX() + dx, center.getY() + dy, center.getZ() + dz, yaw, pitch);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Convenience overload: accept (String relative, Location center) order.
     */
    public static Location deserializeRelative(String s, Location center) {
        return deserializeRelative(center, s);
    }

    /**
     * Return the vector from a -> b (b - a).
     */
    public static Vector toVector(Location a, Location b) {
        if (a == null || b == null) return null;
        return new Vector(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());
    }
}
