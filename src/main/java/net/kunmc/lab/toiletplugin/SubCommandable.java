package net.kunmc.lab.toiletplugin;

import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SubCommandable extends CommandBase
{

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
        if (CommandUtils.invalidLengthMessage(sender, args, 1))
        {
            Bukkit.dispatchCommand(sender, "toilet help " + getName());
            return;
        }

        Map<String, CommandBase> commands = getSubCommands();

        if (!commands.containsKey(args[0]))
        {
            sender.sendMessage(ChatColor.RED + "E: サブコマンドが見つかりませんでした:  " + args[0]);
            Bukkit.dispatchCommand(sender, "toilet help " + getName());
            return;
        }

        CommandBase commandBase = commands.get(args[0]);
        commandBase.onCommand(sender, removeFirst(args));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        List<String> completes = new ArrayList<>();
        Map<String, CommandBase> commands = getSubCommands();

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
