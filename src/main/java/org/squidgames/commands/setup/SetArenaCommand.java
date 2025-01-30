package org.squidgames.commands.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.squidgames.SquidGamesPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetArenaCommand {
    private final SquidGamesPlugin plugin;
    private final Map<String, Location> firstCornerMap = new HashMap<>();

    public SetArenaCommand(SquidGamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set the arena.");
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

            plugin.getConfig().set("arena.corner1.world", Objects.requireNonNull(firstCorner.getWorld()).getName());
            plugin.getConfig().set("arena.corner1.x", firstCorner.getX());
            plugin.getConfig().set("arena.corner1.y", firstCorner.getY());
            plugin.getConfig().set("arena.corner1.z", firstCorner.getZ());

            plugin.getConfig().set("arena.corner2.world", Objects.requireNonNull(location.getWorld()).getName());
            plugin.getConfig().set("arena.corner2.x", location.getX());
            plugin.getConfig().set("arena.corner2.y", location.getY());
            plugin.getConfig().set("arena.corner2.z", location.getZ());

            plugin.saveConfig();

            firstCornerMap.remove(playerName);
            sender.sendMessage(ChatColor.GREEN + "Arena corners set!");
        }
    }
}