package org.squidgames.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.squidgames.SquidGamesPlugin;

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
    public static void outlineAreaWithParticles(SquidGamesPlugin plugin, Location corner1, Location corner2) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) { // 5s
                    cancel();
                    return;
                }

                double minX = Math.min(corner1.getX(), corner2.getX());
                double maxX = Math.max(corner1.getX(), corner2.getX());
                double minY = Math.min(corner1.getY(), corner2.getY());
                double maxY = Math.max(corner1.getY(), corner2.getY());
                double minZ = Math.min(corner1.getZ(), corner2.getZ());
                double maxZ = Math.max(corner1.getZ(), corner2.getZ());

                Location[] corners = new Location[]{
                        new Location(corner1.getWorld(), minX, minY, minZ),
                        new Location(corner1.getWorld(), minX, minY, maxZ),
                        new Location(corner1.getWorld(), minX, maxY, minZ),
                        new Location(corner1.getWorld(), minX, maxY, maxZ),
                        new Location(corner1.getWorld(), maxX, minY, minZ),
                        new Location(corner1.getWorld(), maxX, minY, maxZ),
                        new Location(corner1.getWorld(), maxX, maxY, minZ),
                        new Location(corner1.getWorld(), maxX, maxY, maxZ)
                };
                for (Location corner : corners) {
                    Objects.requireNonNull(corner.getWorld()).spawnParticle(Particle.ANGRY_VILLAGER, corner, 1);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}