package org.squidgames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.squidgames.GameState;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.handlers.GameStateHandler;

public class PlayerJoinLeaveListener implements Listener {
    private final GameStateHandler gameStateHandler;

    public PlayerJoinLeaveListener(GameStateHandler gameStateHandler) {
        this.gameStateHandler = gameStateHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (gameStateHandler.getCurrentState() == GameState.PLAYING) {
            gameStateHandler.playerDied(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (gameStateHandler.getCurrentState() == GameState.PLAYING) {
            gameStateHandler.playerDied(event.getPlayer());
        }
    }
}