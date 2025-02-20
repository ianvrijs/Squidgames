package org.squidgames.stats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Objects;
import java.util.logging.Logger;

public class ScoreboardManager {
    private final StatsManager statsManager;
    private static final Logger logger = Logger.getLogger(ScoreboardManager.class.getName());

    public ScoreboardManager(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    public void setupScoreboard(Player player) {
        logger.info("Setting up scoreboard for player: " + player.getName());
        clearScoreboard(player);
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("stats", "dummy", ChatColor.GOLD + "Foxcraft");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScores(objective, player);

        player.setScoreboard(scoreboard);
    }

    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("stats");
        if (objective == null) {
            setupScoreboard(player);
        } else {
            updateScores(objective, player);
        }
    }

    private void updateScores(Objective objective, Player player) {
        PlayerStats stats = statsManager.getPlayerStats(player);

        String icon = ChatColor.GOLD + "★ ";

        Score gamesPlayed = objective.getScore(icon + ChatColor.GRAY + "Played: " + ChatColor.WHITE + stats.getGamesPlayed());
        gamesPlayed.setScore(3);

        Score gamesWon = objective.getScore(icon + ChatColor.GRAY + "Won: " + ChatColor.WHITE + stats.getWins());
        gamesWon.setScore(2);

        Score pointsEarned = objective.getScore(icon + ChatColor.GRAY + "Points: " + ChatColor.WHITE + stats.getPoints());
        pointsEarned.setScore(1);
    }
    public void clearScoreboard(Player player) {
        player.getScoreboard().clearSlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
    }
}