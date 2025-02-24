package org.squidgames;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.squidgames.commands.CommandTabCompleter;
import org.squidgames.handlers.CommandHandler;
import org.squidgames.handlers.GameStateHandler;
import org.squidgames.listeners.*;
import org.squidgames.stats.ScoreboardManager;
import org.squidgames.stats.StatsManager;

import java.util.Objects;

public class SquidGamesPlugin extends JavaPlugin {
    private GameStateHandler gameStateHandler;
    private PlayerMovementListener playerMovementListener;
    private PlayerActivityListener playerActivityListener;
    private PlayerJoinLeaveListener playerJoinLeaveListener;
    private PlayerInventoryListener playerInventoryListener;
    private PvPListener pvpListener;
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
        playerActivityListener = new PlayerActivityListener(gameStateHandler);
        playerInventoryListener = new PlayerInventoryListener();
        playerJoinLeaveListener = new PlayerJoinLeaveListener(gameStateHandler, scoreboardManager, tabManager);
        pvpListener = new PvPListener(this, gameStateHandler);

        getServer().getPluginManager().registerEvents(playerJoinLeaveListener, this);
        getServer().getPluginManager().registerEvents(pvpListener, this);

        Objects.requireNonNull(this.getCommand("sq")).setExecutor(new CommandHandler(this, gameStateHandler));
        Objects.requireNonNull(getCommand("sq")).setTabCompleter(new CommandTabCompleter());

        new BukkitRunnable() {
            @Override
            public void run() {
                gameStateHandler.checkForAfkPlayers();
            }
        }.runTaskTimer(this, 0, 20 * 5); // Check for idle players every 5s
    }
    public StatsManager getStatsManager() {
        return statsManager;
    }
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    public PlayerMovementListener getPlayerMovementListener() {
        return playerMovementListener;
    }  public PlayerActivityListener getPlayerActivityListener() {
        return playerActivityListener;
    }
    public PlayerJoinLeaveListener getPlayerJoinLeaveListener() {
        return playerJoinLeaveListener;
    }
    public PlayerInventoryListener getPlayerInventoryListener() {
        return playerInventoryListener;
    }


}