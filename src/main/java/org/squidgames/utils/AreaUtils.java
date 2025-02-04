package org.squidgames.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class AreaUtils {

    public static boolean isInsideArea(Location location, String areaType, FileConfiguration config) {
        String path = areaType.toLowerCase();
        if (!config.contains(path + ".corner1") || !config.contains(path + ".corner2")) {
            return false;
        }

        Location corner1 = new Location(
                Bukkit.getWorld(Objects.requireNonNull(config.getString(path + ".corner1.world"))),
                config.getDouble(path + ".corner1.x"),
                config.getDouble(path + ".corner1.y"),
                config.getDouble(path + ".corner1.z")
        );

        Location corner2 = new Location(
                Bukkit.getWorld(Objects.requireNonNull(config.getString(path + ".corner2.world"))),
                config.getDouble(path + ".corner2.x"),
                config.getDouble(path + ".corner2.y"),
                config.getDouble(path + ".corner2.z")
        );

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }
}