package org.squidgames;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.squidgames.commands.setup.SetArenaCommand;
import org.squidgames.commands.setup.SetSpawnCommand;

public class CommandHandler implements CommandExecutor {
    private final SquidGamesPlugin plugin;

    public CommandHandler(SquidGamesPlugin plugin) {
        this.plugin = plugin;
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
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                break;
        }
        return true;
    }

    private void handleSetupCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /sq setup <action>");
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "setspawn":
                new SetSpawnCommand(plugin).execute(sender);
                break;
            case "setarena":
                new SetArenaCommand(plugin).execute(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown setup action: " + action);
                break;
        }
    }
}