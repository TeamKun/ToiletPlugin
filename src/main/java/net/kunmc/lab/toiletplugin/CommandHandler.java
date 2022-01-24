package net.kunmc.lab.toiletplugin;

import net.kunmc.lab.toiletplugin.commands.HelpCommand;
import net.kunmc.lab.toiletplugin.commands.ToolCommand;
import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter
{
    private static final HashMap<String, CommandBase> commands;
    private static final HelpCommand helpInstance;

    static {
        commands = new HashMap<>();

        helpInstance = new HelpCommand(commands);

        commands.put("help", helpInstance);
        commands.put("tool", new ToolCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (CommandFeedBackUtils.unPermMessage(sender, "toilet.admin"))
            return true;

        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 1))
        {
            helpInstance.onCommand(sender, removeFirst(new String[0]));
            return true;
        }

        if (!commands.containsKey(args[0]))
        {
            sender.sendMessage(ChatColor.RED + "E: サブコマンドが見つかりませんでした:  " + args[0]);
            helpInstance.onCommand(sender, removeFirst(new String[0]));
            return true;
        }

        CommandBase commandBase = commands.get(args[0]);
        commandBase.onCommand(sender, removeFirst(args));
        return true;
    }

    private static String[] removeFirst(String[] args)
    {
        if (args.length == 0)
            return args;
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        List<String> completes = new ArrayList<>();

        if (!sender.hasPermission("toilet.admin"))
            return new ArrayList<>();

        if (args.length == 1)
            completes.addAll(commands.keySet());

        else if (commands.containsKey(args[0]))
        {
            CommandBase commandBase = commands.get(args[0]);
            List<String> commandCompletes = commandBase.onTabComplete(sender, removeFirst(args));
            if (commandCompletes != null)
                completes.addAll(commandCompletes);
        }

        ArrayList<String> asCopy = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], completes, asCopy);
        Collections.sort(asCopy);
        return asCopy;
    }
}
