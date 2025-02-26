package org.squidgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.handlers.GameStateHandler;

public class PvPListener implements Listener {
    private final SquidGamesPlugin plugin;
    private final GameStateHandler gameStateHandler;

    public PvPListener(SquidGamesPlugin plugin, GameStateHandler gameStateHandler) {
        this.plugin = plugin;
        this.gameStateHandler = gameStateHandler;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            if (!gameStateHandler.isPvpEnabled() || !isInArena(damager) || !isInArena(target)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isInArena(Player player) {
        Location loc = player.getLocation();
        Location corner1 = gameStateHandler.getArenaCorner1();
        Location corner2 = gameStateHandler.getArenaCorner2();

        if (corner1 == null || corner2 == null) {
            return false;
        }

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}