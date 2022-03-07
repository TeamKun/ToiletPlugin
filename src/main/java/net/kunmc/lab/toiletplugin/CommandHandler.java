package net.kunmc.lab.toiletplugin;

import net.kunmc.lab.toiletplugin.commands.ConfigCommand;
import net.kunmc.lab.toiletplugin.commands.DebugCommand;
import net.kunmc.lab.toiletplugin.commands.HelpCommand;
import net.kunmc.lab.toiletplugin.commands.QuestCommand;
import net.kunmc.lab.toiletplugin.commands.ReloadModelCommand;
import net.kunmc.lab.toiletplugin.commands.RemoveCommand;
import net.kunmc.lab.toiletplugin.commands.ToolCommand;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.ConfigManager;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter
{
    private final HashMap<String, CommandBase> commands;
    private final HelpCommand helpInstance;

    public CommandHandler(GameMain game)
    {
        commands = new HashMap<>();

        helpInstance = new HelpCommand(commands);

        commands.put("help", helpInstance);
        commands.put("tool", new ToolCommand());
        commands.put("remove", new RemoveCommand());
        commands.put("reloadmodel", new ReloadModelCommand());
        commands.put("config", new ConfigCommand(new ConfigManager(game.getConfig())));
        commands.put("quest", new QuestCommand(game));
        commands.put("debug", new DebugCommand(game));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (CommandUtils.unPermMessage(sender, "toilet.admin"))
            return true;

        if (CommandUtils.invalidLengthMessage(sender, args, 1))
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

            String[] commandArguments = commandBase.getArguments();
            List<String> commandCompletes = commandBase.onTabComplete(sender, removeFirst(args));
            if (commandCompletes != null)
                completes.addAll(commandCompletes);
            if (commandArguments.length >= args.length - 1)
                completes.add(ChatColor.stripColor(commandArguments[args.length - 2]));
        }

        ArrayList<String> result = new ArrayList<>();

        for (String complete : completes)
        {
            if (StringUtils.containsIgnoreCase(complete, args[args.length - 1]))
                result.add(complete);
        }
        Collections.sort(result);
        return result;
    }
}
