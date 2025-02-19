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
import org.bukkit.inventory.ItemStack;
import org.squidgames.SquidGamesPlugin;
import org.bukkit.inventory.meta.ItemMeta;
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
        giveSelectionTool(player);
        player.sendMessage(ChatColor.GREEN + "You have been given a selection tool. Right-click white wool blocks with it to mark out an area.");
    }
    public void giveSelectionTool(Player player) {
        ItemStack selectionTool = new ItemStack(Material.STICK);
        ItemMeta meta = selectionTool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Selection Tool");
            selectionTool.setItemMeta(meta);
        }
        player.getInventory().addItem(selectionTool);
    }
    @org.bukkit.event.EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.STICK && item.getItemMeta() != null && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Selection Tool")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                if (block != null) {
                    long currentTime = System.currentTimeMillis();
                    if (corner1 == null) {
                        corner1 = block.getLocation();
                        lastClickTime = currentTime;
                        player.sendMessage(ChatColor.GREEN + "First corner selected: " + corner1);
                    } else if (corner2 == null && (currentTime - lastClickTime) > 500) { // 500ms delay
                        corner2 = block.getLocation();
                        player.sendMessage(ChatColor.GREEN + "Second corner selected: " + corner2);
                        saveSelectedArea();
                        player.sendMessage(ChatColor.GREEN + "Light setup mode disabled. Selected area saved to config.");
                        setLightColor(Material.RED_WOOL);
                        PlayerInteractEvent.getHandlerList().unregister(this);
                    } else if (corner1 != null && corner2 != null) { // reset
                        corner1 = block.getLocation();
                        corner2 = null;
                        lastClickTime = currentTime;
                        player.sendMessage(ChatColor.GREEN + "First corner re-selected: " + corner1);
                    }
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
            assert worldName1 != null;
            corner1 = new Location(Bukkit.getWorld(worldName1), x1, y1, z1);

            String worldName2 = plugin.getConfig().getString("light.area.corner2.world");
            double x2 = plugin.getConfig().getDouble("light.area.corner2.x");
            double y2 = plugin.getConfig().getDouble("light.area.corner2.y");
            double z2 = plugin.getConfig().getDouble("light.area.corner2.z");
            assert worldName2 != null;
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