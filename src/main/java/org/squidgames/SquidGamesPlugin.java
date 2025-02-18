package org.squidgames;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.squidgames.commands.CommandTabCompleter;
import org.squidgames.handlers.CommandHandler;
import org.squidgames.handlers.GameStateHandler;
import org.squidgames.listeners.PlayerActivityListener;
import org.squidgames.listeners.PlayerInventoryListener;
import org.squidgames.listeners.PlayerJoinLeaveListener;
import org.squidgames.listeners.PlayerMovementListener;
import org.squidgames.stats.ScoreboardManager;
import org.squidgames.stats.StatsManager;

import java.util.Objects;

public class SquidGamesPlugin extends JavaPlugin {
    private GameStateHandler gameStateHandler;
    private PlayerMovementListener playerMovementListener;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private TabManager tabManager;



    @Override
    public void onEnable() {
        getLogger().info("SquidGames plugin enabled!");
        gameStateHandler = new GameStateHandler(this);
        statsManager = new StatsManager(this);
        scoreboardManager = new ScoreboardManager(statsManager);
        tabManager = new TabManager(this);




        playerMovementListener = new PlayerMovementListener(this, gameStateHandler);
        Objects.requireNonNull(this.getCommand("sq")).setExecutor(new CommandHandler(this, gameStateHandler));
        Objects.requireNonNull(getCommand("sq")).setTabCompleter(new CommandTabCompleter());

        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(gameStateHandler, scoreboardManager, tabManager), this);
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), this);
        getServer().getPluginManager().registerEvents(playerMovementListener, this);
        getServer().getPluginManager().registerEvents(new PlayerActivityListener(gameStateHandler), this);
        new BukkitRunnable() {
            @Override
            public void run() {
                gameStateHandler.checkForAfkPlayers();
            }
        }.runTaskTimer(this, 0, 20 * 60); // Check for idle players every minute
    }
    public StatsManager getStatsManager() {
        return statsManager;
    }
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    public PlayerMovementListener getPlayerMovementListener() {
        return playerMovementListener;
    }

}