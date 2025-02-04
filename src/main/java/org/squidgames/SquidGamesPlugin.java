package org.squidgames;

import org.bukkit.plugin.java.JavaPlugin;
import org.squidgames.commands.CommandTabCompleter;
import org.squidgames.handlers.CommandHandler;
import org.squidgames.handlers.GameStateHandler;
import org.squidgames.listeners.PlayerInventoryListener;
import org.squidgames.listeners.PlayerMovementListener;

import java.util.Objects;

public class SquidGamesPlugin extends JavaPlugin {
    private GameStateHandler gameStateHandler;
    private PlayerMovementListener playerMovementListener;


    @Override
    public void onEnable() {
        getLogger().info("SquidGames plugin enabled!");
        gameStateHandler = new GameStateHandler(this);
        playerMovementListener = new PlayerMovementListener(this, gameStateHandler);
        Objects.requireNonNull(this.getCommand("sq")).setExecutor(new CommandHandler(this, gameStateHandler));
        Objects.requireNonNull(getCommand("sq")).setTabCompleter(new CommandTabCompleter());

        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(), this);
        getServer().getPluginManager().registerEvents(playerMovementListener, this);
    }
    public PlayerMovementListener getPlayerMovementListener() {
        return playerMovementListener;
    }
    public GameStateHandler getGameStateHandler() {
        return gameStateHandler;
    }
}