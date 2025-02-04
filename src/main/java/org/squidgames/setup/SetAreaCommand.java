package org.squidgames.setup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.squidgames.SquidGamesPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetAreaCommand {
    private final SquidGamesPlugin plugin;
    private static final Map<String, Location> firstCornerMap = new HashMap<>();

    public SetAreaCommand(SquidGamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String areaType) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set the " + areaType + ".");
            return;
        }

        String playerName = player.getName();
        Location location = player.getLocation();

        if (!firstCornerMap.containsKey(playerName)) {
            firstCornerMap.put(playerName, location);
            sender.sendMessage(ChatColor.GREEN + "First corner set! Now go to the second corner and execute the command again.");
        } else {
            Location firstCorner = firstCornerMap.get(playerName);

            if (!Objects.equals(firstCorner.getWorld(), location.getWorld())) {
                sender.sendMessage(ChatColor.RED + "The two corners must be in the same world.");
                return;
            }

            String path = areaType.toLowerCase();
            plugin.getConfig().set(path + ".corner1.world", Objects.requireNonNull(firstCorner.getWorld()).getName());
            plugin.getConfig().set(path + ".corner1.x", Math.round(firstCorner.getX()));
            plugin.getConfig().set(path + ".corner1.y", Math.round(firstCorner.getY()));
            plugin.getConfig().set(path + ".corner1.z", Math.round(firstCorner.getZ()));

            plugin.getConfig().set(path + ".corner2.world", Objects.requireNonNull(location.getWorld()).getName());
            plugin.getConfig().set(path + ".corner2.x", Math.round(location.getX()));
            plugin.getConfig().set(path + ".corner2.y", Math.round(location.getY()));
            plugin.getConfig().set(path + ".corner2.z", Math.round(location.getZ()));

            plugin.saveConfig();

            firstCornerMap.remove(playerName);
            sender.sendMessage(ChatColor.GREEN + areaType + " saved!");

            outlineAreaWithParticles(firstCorner, location);
        }
    }

    private void outlineAreaWithParticles(Location corner1, Location corner2) {
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