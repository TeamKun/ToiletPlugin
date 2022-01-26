package net.kunmc.lab.toiletplugin;

import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SubCommandable extends CommandBase
{
    private final Map<String, CommandBase> commands = getSubCommands();

    private static String[] removeFirst(String[] args)
    {
        if (args.length == 0)
            return args;
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 1))
        {
            Bukkit.dispatchCommand(sender, "/toilet help " + getName());
            return;
        }


        if (!commands.containsKey(args[0]))
        {
            sender.sendMessage(ChatColor.RED + "E: サブコマンドが見つかりませんでした:  " + args[0]);
            Bukkit.dispatchCommand(sender, "/toilet help " + getName());
            return;
        }

        CommandBase commandBase = commands.get(args[0]);
        commandBase.onCommand(sender, removeFirst(args));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        List<String> completes = new ArrayList<>();

        if (args.length == 1)
            completes.addAll(commands.keySet());

        else if (commands.containsKey(args[0]))
        {
            CommandBase commandBase = commands.get(args[0]);
            List<String> commandCompletes = commandBase.onTabComplete(sender, removeFirst(args));
            if (commandCompletes != null)
                completes.addAll(commandCompletes);
        }

        return completes;
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("subcommand", "string"),
                optional("args", "string[]")
        };
    }

    public abstract String getName();

    public abstract Map<String, CommandBase> getSubCommands();
}
