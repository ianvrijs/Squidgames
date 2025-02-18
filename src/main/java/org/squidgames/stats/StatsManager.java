package org.squidgames.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.squidgames.SquidGamesPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class StatsManager {
    private final SquidGamesPlugin plugin;
    private final File statsFile;
    private final Gson gson;
    private Map<String, PlayerStats> playerStatsMap;

    public StatsManager(SquidGamesPlugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerStatsMap = new HashMap<>();
        loadStats();
    }

    private void loadStats() {
        if (statsFile.exists()) {
            try (FileReader reader = new FileReader(statsFile)) {
                Type type = new TypeToken<Map<String, PlayerStats>>() {}.getType();
                playerStatsMap = gson.fromJson(reader, type);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load stats from stats.json", e);
            }
        }
    }

    public void saveStats() {
        try (FileWriter writer = new FileWriter(statsFile)) {
            gson.toJson(playerStatsMap, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save stats to stats.json", e);
        }
    }

    public PlayerStats getPlayerStats(Player player) {
        return playerStatsMap.computeIfAbsent(player.getName(), k -> new PlayerStats());
    }

    public void clearStats() {
        playerStatsMap.clear();
        saveStats();
    }

    public void updatePlayerStats(Player player, boolean isWinner, int rank) {
        PlayerStats stats = getPlayerStats(player);
//        stats.addGamePlayed();
        if (isWinner) {
            stats.addWin();
            stats.addPoints(60);
        }
        switch (rank) {
            case 1:
                stats.addPoints(100);
                break;
            case 2:
                stats.addPoints(90);
                break;
            case 3:
                stats.addPoints(80);
                break;
        }
        saveStats();
    }

    public void saveAllQueuedPlayers(List<Player> queuedPlayers) {
        for (Player player : queuedPlayers) {
            PlayerStats stats = getPlayerStats(player);
            stats.addGamePlayed();
        }
        saveStats();
    }
}