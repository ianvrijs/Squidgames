package org.squidgames.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.squidgames.SquidGamesPlugin;
import org.bukkit.ChatColor;

public class GameTimer {
    private final SquidGamesPlugin plugin;
    private BossBar gameTimerBar;
    private int remainingTimeSeconds;
    private int gameTimeMinutes;
    private boolean gameEnded;
    private BukkitTask timerTask;


    public GameTimer(SquidGamesPlugin plugin) {
        this.plugin = plugin;
        this.gameTimeMinutes = 5; // default
    }

    public void setGameTime(int minutes) {
        this.gameTimeMinutes = minutes;
    }

    public void startGameTimer() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
        gameEnded = false;
        remainingTimeSeconds = gameTimeMinutes * 60;
        gameTimerBar = Bukkit.createBossBar("Game Time Remaining", BarColor.GREEN, BarStyle.SOLID);
        gameTimerBar.setVisible(true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            gameTimerBar.addPlayer(player);
        }
        runGameTimer();
    }

    private void runGameTimer() {
        timerTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (remainingTimeSeconds <= 0) {
                    endGame();
                    return;
                }
                updateGameTimerBar();
                remainingTimeSeconds--;
            }
        }, 0L, 20L); // Run every second
    }

    private void updateGameTimerBar() {
        double progress = (double) remainingTimeSeconds / (gameTimeMinutes * 60);
        gameTimerBar.setProgress(progress);

        int minutes = remainingTimeSeconds / 60;
        int seconds = remainingTimeSeconds % 60;
        String timeLeft = String.format("%02d:%02d", minutes, seconds);

        if (remainingTimeSeconds <= 60) {
            gameTimerBar.setColor(BarColor.RED);
            gameTimerBar.setTitle(ChatColor.RED +  timeLeft);
        } else if (remainingTimeSeconds <= 180) {
            gameTimerBar.setColor(BarColor.YELLOW);
            gameTimerBar.setTitle(ChatColor.YELLOW + timeLeft);
        } else {
            gameTimerBar.setColor(BarColor.GREEN);
            gameTimerBar.setTitle(ChatColor.GREEN + timeLeft);
        }
    }

    public void endGame() {
        if (gameEnded) {
            return;
        }
        gameEnded = true;
        if (timerTask != null) {
            timerTask.cancel();
        }
        gameTimerBar.setVisible(false);
        Bukkit.broadcastMessage(ChatColor.RED + "The game has ended because the timer ran out!");
        for (Player player : plugin.getGameStateHandler().getQueuedPlayers()) {
            if (!plugin.getGameStateHandler().getPlayerStateHandler().isPlayerSafe(player) &&
                    !plugin.getGameStateHandler().getPlayerStateHandler().isPlayerDead(player)) {
                plugin.getGameStateHandler().playerDied(player);
            }
        }
    }

    public BossBar getGameTimerBar() {
        return gameTimerBar;
    }
}