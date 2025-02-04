package org.squidgames.setup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.squidgames.SquidGamesPlugin;

import java.util.Objects;

public class SetLightCommand implements Listener {
    private final SquidGamesPlugin plugin;
    private Location corner1;
    private Location corner2;
    private long lastClickTime;

    public SetLightCommand(SquidGamesPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadSelectedArea();
    }

    public void execute(Player player) {
        player.sendMessage(ChatColor.GREEN + "Light setup mode enabled. Right-click two opposite corners to select the area.");
    }

    @org.bukkit.event.EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.WHITE_WOOL) {
                long currentTime = System.currentTimeMillis();
                if (corner1 == null) {
                    corner1 = block.getLocation();
                    lastClickTime = currentTime;
                    event.getPlayer().sendMessage(ChatColor.GREEN + "First corner selected: " + corner1);
                } else if (corner2 == null && (currentTime - lastClickTime) > 500) { // 500ms delay
                    corner2 = block.getLocation();
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Second corner selected: " + corner2);
                    saveSelectedArea();
                    event.getPlayer().sendMessage(ChatColor.GREEN + "Light setup mode disabled. Selected area saved to config.");
                    setLightColor(Material.RED_WOOL);
                    PlayerInteractEvent.getHandlerList().unregister(this);
                }
            }
        }
    }

    private void saveSelectedArea() {
        if (corner1 != null && corner2 != null) {
            String path = "light.area";
            plugin.getConfig().set(path + ".corner1.world", corner1.getWorld().getName());
            plugin.getConfig().set(path + ".corner1.x", corner1.getBlockX());
            plugin.getConfig().set(path + ".corner1.y", corner1.getBlockY());
            plugin.getConfig().set(path + ".corner1.z", corner1.getBlockZ());
            plugin.getConfig().set(path + ".corner2.world", corner2.getWorld().getName());
            plugin.getConfig().set(path + ".corner2.x", corner2.getBlockX());
            plugin.getConfig().set(path + ".corner2.y", corner2.getBlockY());
            plugin.getConfig().set(path + ".corner2.z", corner2.getBlockZ());
            plugin.saveConfig();
        }
    }

    private void loadSelectedArea() {
        if (plugin.getConfig().contains("light.area.corner1") && plugin.getConfig().contains("light.area.corner2")) {
            String worldName1 = plugin.getConfig().getString("light.area.corner1.world");
            double x1 = plugin.getConfig().getDouble("light.area.corner1.x");
            double y1 = plugin.getConfig().getDouble("light.area.corner1.y");
            double z1 = plugin.getConfig().getDouble("light.area.corner1.z");
            corner1 = new Location(Bukkit.getWorld(worldName1), x1, y1, z1);

            String worldName2 = plugin.getConfig().getString("light.area.corner2.world");
            double x2 = plugin.getConfig().getDouble("light.area.corner2.x");
            double y2 = plugin.getConfig().getDouble("light.area.corner2.y");
            double z2 = plugin.getConfig().getDouble("light.area.corner2.z");
            corner2 = new Location(Bukkit.getWorld(worldName2), x2, y2, z2);
        }
    }

    public void setLightColor(Material color) {
        if (corner1 != null && corner2 != null) {
            int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
            int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
            int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
            int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
            int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
            int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Block block = Objects.requireNonNull(corner1.getWorld()).getBlockAt(x, y, z);
                        if (block.getType() != color) {
                            block.setType(color);
                        }
                    }
                }
            }
        }
    }
}