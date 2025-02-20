package org.squidgames.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.squidgames.GameState;
import org.squidgames.TabManager;
import org.squidgames.handlers.GameStateHandler;
import org.squidgames.stats.ScoreboardManager;

public class PlayerJoinLeaveListener implements Listener {
    private final GameStateHandler gameStateHandler;
    private final ScoreboardManager scoreboardManager;
    private final TabManager tabManager;

    public PlayerJoinLeaveListener(GameStateHandler gameStateHandler, ScoreboardManager scoreboardManager, TabManager tabManager) {
        this.gameStateHandler = gameStateHandler;
        this.scoreboardManager = scoreboardManager;
        this.tabManager = tabManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scoreboardManager.clearScoreboard(player);
        scoreboardManager.setupScoreboard(player);
        tabManager.setPlayerListHeaderFooter(player, ChatColor.RED + "Squid Games", ChatColor.GOLD + "Foxcraft");

        if (gameStateHandler.getCurrentState() == GameState.PLAYING) {
            gameStateHandler.playerDied(player);
        }

        updatePlayerTabColor(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (gameStateHandler.getCurrentState() == GameState.PLAYING) {
            gameStateHandler.playerDied(event.getPlayer());
        }
        if (gameStateHandler.getQueuedPlayers().contains(event.getPlayer())) {
            gameStateHandler.removeQueuedPlayer(event.getPlayer());
        }
    }

    private void updatePlayerTabColor(Player player) {
        if (gameStateHandler.getPlayerStateHandler().isPlayerSafe(player)) {
            tabManager.setPlayerNameColor(player, ChatColor.GREEN);
        } else if (gameStateHandler.getPlayerStateHandler().isPlayerDead(player)) {
            tabManager.setPlayerNameColor(player, ChatColor.RED);
        } else {
            tabManager.setPlayerNameColor(player, ChatColor.GRAY);
        }
    }
}