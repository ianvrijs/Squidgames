package org.squidgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.squidgames.GameState;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.handlers.GameStateHandler;
import org.squidgames.utils.AreaUtils;
import org.bukkit.entity.Player;

public class PlayerMovementListener implements Listener {
    private final SquidGamesPlugin plugin;
    private final GameStateHandler gameStateHandler;
    private long redLightStartTime;

    public PlayerMovementListener(SquidGamesPlugin plugin, GameStateHandler gameStateHandler) {
        this.plugin = plugin;
        this.gameStateHandler = gameStateHandler;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameStateHandler.getCurrentState() != GameState.PLAYING) {
            return;
        }
        Location to = event.getTo();
        Location from = event.getFrom();

        if (to == null) {
            Bukkit.getLogger().info("Location 'to' or 'from' is null");
            return;
        }

        GameState currentState = gameStateHandler.getCurrentState();
        Player player = event.getPlayer();

        if (currentState == GameState.PLAYING && gameStateHandler.getPlayerStateHandler().isPlayerSafe(player)) {
            if (!AreaUtils.isInsideArea(to, "safezone", plugin.getConfig())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot leave the safezone while the game is ongoing.");
                return;
            }
        }

        if (!isSameBlock(from, to)) {
            if (AreaUtils.isInsideArea(to, "arena", plugin.getConfig())) {
                if (currentState != GameState.PLAYING || gameStateHandler.getPlayerStateHandler().isPlayerDead(player)) {
                    event.setCancelled(true);
                    player.sendMessage("You cannot enter the arena right now.");
                    return;
                } else if (gameStateHandler.isRedLight()) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - redLightStartTime > 500) { // 0.5 second cooldown
                        gameStateHandler.playerDied(player);
                        player.sendMessage(ChatColor.RED + "You moved.. RIP");
                    }
                    return;
                }
            }

            if (AreaUtils.isInsideArea(to, "safezone", plugin.getConfig())) {
                if (currentState == GameState.PLAYING) {
                    gameStateHandler.markPlayerAsSafe(player);
                }
            }
        }
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

    public void setRedLightStartTime(long redLightStartTime) {
        this.redLightStartTime = redLightStartTime;
    }
}