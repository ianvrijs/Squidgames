package org.squidgames;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class SquidGamesPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("SquidGames plugin enabled!");
        Objects.requireNonNull(this.getCommand("sq")).setExecutor(new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("SquidGames plugin disabled!");
    }
}