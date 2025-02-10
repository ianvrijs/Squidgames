package org.squidgames.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.utils.AreaUtils;

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

            AreaUtils.outlineAreaWithParticles(plugin, firstCorner, location);
        }
    }
}