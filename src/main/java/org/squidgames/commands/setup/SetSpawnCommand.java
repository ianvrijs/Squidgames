package org.squidgames.commands.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.squidgames.SquidGamesPlugin;

import java.util.Objects;

public class SetSpawnCommand {
    private final SquidGamesPlugin plugin;

    public SetSpawnCommand(SquidGamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set the spawn point.");
            return;
        }

        Location location = player.getLocation();

        plugin.getConfig().set("spawn.world", Objects.requireNonNull(location.getWorld()).getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", location.getYaw());
        plugin.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Spawn point set!");
    }
}