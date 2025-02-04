package org.squidgames.handlers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.setup.SetAreaCommand;
import org.squidgames.setup.SetLightCommand;
import org.squidgames.setup.SetLocationCommand;

public class CommandHandler implements CommandExecutor {
    private final SquidGamesPlugin plugin;
    private final GameStateHandler gameStateHandler;

    public CommandHandler(SquidGamesPlugin plugin, GameStateHandler gameStateHandler) {
        this.plugin = plugin;
        this.gameStateHandler = gameStateHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /sq <subcommand> [args]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "setup":
                handleSetupCommand(sender, args);
                break;
            case "start":
                gameStateHandler.startGame(sender);
                break;
            case "stop":
                gameStateHandler.stopGame(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                break;
        }
        return true;
    }

    private void handleSetupCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /sq setup <setspawn|setlobby|setarena|setsafezone>");
            return;
        }

        String action = args[1].toLowerCase();
        SetLocationCommand setLocationCommand = new SetLocationCommand(plugin);
        SetAreaCommand setAreaCommand = new SetAreaCommand(plugin);
        if (sender instanceof Player) {
            switch (action) {
                case "setspawn":
                    setAreaCommand.execute(sender, "spawn");
                    break;
                case "setlobby":
                    setLocationCommand.execute(sender, "lobby");
                    break;
                case "setarena":
                    setAreaCommand.execute(sender, "arena");
                    break;
                case "setsafezone":
                    setAreaCommand.execute(sender, "safezone");
                    break;
                case "setlight":
                    new SetLightCommand(plugin).execute((Player) sender);
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown setup action: " + action);
                    break;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
        }
    }
}