package org.squidgames.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.squidgames.SquidGamesPlugin;

import java.util.Objects;

public class SetLocationCommand {
    private final SquidGamesPlugin plugin;

    public SetLocationCommand(SquidGamesPlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String locationType) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return;
        }

        Location location = player.getLocation();
        String path = locationType.toLowerCase();
        //TODO round the values to avoid floating point errors

        plugin.getConfig().set(path + ".world", Objects.requireNonNull(location.getWorld()).getName());
        plugin.getConfig().set(path + ".x", location.getX());
        plugin.getConfig().set(path + ".y", location.getY());
        plugin.getConfig().set(path + ".z", location.getZ());
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Set " + locationType + " location to your current position.");
    }
}