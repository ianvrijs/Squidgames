package org.squidgames.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("setup", "start", "stop");
    private static final List<String> SETUP_COMMANDS = Arrays.asList("setspawn", "setlobby", "setarena", "setsafezone", "setlight", "setrandom");
    private static final List<String> TRUE_FALSE = Arrays.asList("true", "false");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartingWith(args[0], SUB_COMMANDS);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setup")) {
            return filterStartingWith(args[1], SETUP_COMMANDS);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("setrandom")) {
            return filterStartingWith(args[2], TRUE_FALSE);
        }
        return new ArrayList<>();
    }

    private List<String> filterStartingWith(String prefix, List<String> options) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }
}