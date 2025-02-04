package org.squidgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.block.Skull;
import org.squidgames.GameState;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.handlers.GameStateHandler;
import org.squidgames.utils.AreaUtils;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
public class PlayerMovementListener implements Listener {
    private final Map<Location, ArmorStand> corpses = new HashMap<>();
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
                    if (currentTime - redLightStartTime > 500) { // waiting .5s to give the player some time to react
                        spawnCorpse(player.getLocation(), player);
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
    private void spawnCorpse(Location location, Player player) {
        location.getBlock().setType(Material.PLAYER_HEAD);

        Skull skull = (Skull) location.getBlock().getState();
        skull.setOwningPlayer(player);
        skull.update();

        Location armorStandLocation = location.clone().add(0, 0.5, -0.2);
        ArmorStand armorStand = Objects.requireNonNull(location.getWorld()).spawn(armorStandLocation, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setCustomName(ChatColor.RED + player.getName());
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setMarker(true);

        corpses.put(location, armorStand);
    }

    public void removeAllCorpses() {
        for (Map.Entry<Location, ArmorStand> entry : corpses.entrySet()) {
            entry.getKey().getBlock().setType(Material.AIR); // remove skull
            entry.getValue().remove(); // remove armorStand
        }
        corpses.clear();
    }
}