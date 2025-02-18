package org.squidgames;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TabManager {

    private final JavaPlugin plugin;

    public TabManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setPlayerListHeaderFooter(Player player, String header, String footer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setPlayerListHeaderFooter(header, footer);
            }
        }.runTask(plugin);
    }

    public void setPlayerNameColor(Player player, ChatColor color) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setPlayerListName(color + player.getName());
            }
        }.runTask(plugin);
    }
}