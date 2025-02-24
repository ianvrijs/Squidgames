package org.squidgames.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.squidgames.GameState;
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
            case "exempt":
                gameStateHandler.exemptPlayer((Player) sender);
                break;
            case "pvp":
                if (args.length == 2) {
                    boolean enablePvp = Boolean.parseBoolean(args[1]);
                    gameStateHandler.setPvpEnabled(enablePvp);
                    sender.sendMessage(ChatColor.GREEN + "PvP has been " + (enablePvp ? "enabled" : "disabled") + ".");
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /sq pvp <true/false>");
                }
                break;
            case "clearstats":
                if (sender instanceof Player) {
                    plugin.getStatsManager().clearStats();
                    sender.sendMessage(ChatColor.GREEN + "All player stats have been cleared.");
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                break;
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sq remove <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                if(gameStateHandler.getCurrentState() == GameState.PLAYING) {
                    gameStateHandler.playerDied(target);
                    sender.sendMessage(ChatColor.GREEN + target.getName() + " has been removed from the ongoing match.");
                    break;
                } else {
                    sender.sendMessage(ChatColor.RED + "There is no ongoing game to remove players from...");
                    break;
                }

        }
        return true;
    }

    private void handleSetupCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /sq setup <setspawn|setlobby|setarena|setsafezone|setrandom>");
            return;
        }

        String action = args[1].toLowerCase();
        SetLocationCommand setLocationCommand = new SetLocationCommand(plugin);
        SetAreaCommand setAreaCommand = new SetAreaCommand(plugin);
        if (sender instanceof Player) {
            switch (action) {
                case "setspawn":
                    setLocationCommand.execute(sender, "spawn");
                    break;
                case "setlobby":
                    setLocationCommand.execute(sender, "lobby");
                    break;
                case "setinterval":
                    if (args.length != 4) {
                        sender.sendMessage(ChatColor.RED + "Usage: /sq setup setinterval <min> <max>");
                        return;
                    }
                    try {
                        int minSeconds = Integer.parseInt(args[2]);
                        int maxSeconds = Integer.parseInt(args[3]);
                        plugin.getConfig().set("interval.min", minSeconds);
                        plugin.getConfig().set("interval.max", maxSeconds);
                        plugin.saveConfig();
                        sender.sendMessage(ChatColor.GREEN + "Interval updated: min = " + minSeconds + " seconds, max = " + maxSeconds + " seconds.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid number format. Please enter valid integers for min and max seconds.");
                    }
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
                case "setrandom":
                    if (args.length != 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /sq setup setrandom <true/false>");
                        return;
                    }
                    boolean useRandomInterval = Boolean.parseBoolean(args[2]);
                    gameStateHandler.setUseRandomInterval(useRandomInterval);
                    sender.sendMessage(ChatColor.GREEN + "Random interval set to " + useRandomInterval);
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