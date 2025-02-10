package org.squidgames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.squidgames.handlers.GameStateHandler;

public class PlayerActivityListener implements Listener {
    private final GameStateHandler gameStateHandler;

    public PlayerActivityListener(GameStateHandler gameStateHandler) {
        this.gameStateHandler = gameStateHandler;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        gameStateHandler.updatePlayerActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        gameStateHandler.updatePlayerActivity(event.getPlayer());
    }
}