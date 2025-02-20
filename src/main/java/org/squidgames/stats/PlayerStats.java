package org.squidgames.stats;

import org.bukkit.Bukkit;

public class PlayerStats {
    private int gamesPlayed;
    private int wins;
    private int points;

    public PlayerStats() {
        this.gamesPlayed = 0;
        this.wins = 0;
        this.points = 0;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void addGamePlayed() {
        this.gamesPlayed++;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        this.wins++;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        this.points += points;
    }
}