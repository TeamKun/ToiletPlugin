package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.SubCommandable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelpCommand extends CommandBase
{
    private final HashMap<String, CommandBase> commands;

    private final List<String> subCommands;

    public HelpCommand(HashMap<String, CommandBase> commands)
    {
        this.commands = commands;
        this.subCommands = commands.entrySet().stream().parallel()
                .filter(entry -> entry.getValue() instanceof SubCommandable)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    private static void send(CommandSender sender, TextComponent helpMessage, String commandName, int space_size, String subCommand)
    {
        sender.sendMessage(suggestCommand(
                of(ChatColor.AQUA + "/toilet " + (subCommand != null ? subCommand + " ": "")
                        + commandName + " " + StringUtils.repeat(" ", space_size - commandName.length()) + " - ")
                        .append(Component.text(ChatColor.DARK_AQUA.toString()))
                        .append(helpMessage.color(NamedTextColor.DARK_AQUA)),
                "/toilet " + (subCommand != null ? subCommand + " ": "") + commandName
        ));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        Map<String, CommandBase> commands = this.commands;
        boolean subCommand = false;

        if (args.length > 0 && commands.containsKey(args[0]))
        {
            CommandBase command = commands.get(args[0]);
            if (command instanceof SubCommandable)
            {
                SubCommandable subCommandable = (SubCommandable) command;
                commands = subCommandable.getSubCommands();
                subCommand = true;
            }
        }

        int page_max = commands.size() / 5 + 1;


        int page = 1;
        try
        {
            if (args.length > 1)
                page = Integer.parseInt(args[1]);
            else if (args.length > 0)
                if (args[0].matches("[0-9]+"))
                    page = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(ChatColor.RED + "ページ番号が不正です。");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "-----=====    ToiletPlugin (" + page + "/" + page_max + ")  =====-----");

        int start = (page - 1) * 5;
        int end = Math.min(start + 5, commands.size());

        int max_length = commands.keySet().stream().mapToInt(String::length).max().orElse(0);

        boolean finalSubCommand = subCommand;
        commands.entrySet().stream()
                .skip(start)
                .limit(end - start)
                .forEach(entry -> send(sender, entry.getValue().getHelpOneLine().append(
                                        of("\n    " + String.join(" ", entry.getValue().getArguments()))),
                                entry.getKey(), max_length, (finalSubCommand ? args[0]: null)
                        )
                );

        TextComponent footer = of(ChatColor.GOLD + "-----=====");

        if (page > 1)
            footer = footer.append(of(ChatColor.GOLD + " [" + ChatColor.RED + "<<" + ChatColor.GOLD + "]")
                    .clickEvent(ClickEvent.runCommand("/toilet help " + (subCommand ? args[0] + " ": "") + (page - 1)))
                    .hoverEvent(HoverEvent.showText(of(ChatColor.AQUA + "クリックして前のページに戻る"))));
        else
            footer = footer.append(of("     "));

        footer = footer.append(of(ChatColor.GOLD + " ToiletPlugin "));

        if (page < page_max)
            footer = footer.append(of(ChatColor.GOLD + "[" + ChatColor.GREEN + ">>" + ChatColor.GOLD + "] ")
                    .clickEvent(ClickEvent.runCommand("/toilet help " + (subCommand ? args[0] + " ": "") + (page + 1)))
                    .hoverEvent(HoverEvent.showText(of(ChatColor.AQUA + "クリックして次のページに進む"))));
        else
            footer = footer.append(of("    "));

        sender.sendMessage(footer.append(of(ChatColor.GOLD + "=====-----")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        List<String> result = new ArrayList<>(this.subCommands);

        if (args.length == 1)
        {
            result.add("[ページ番号|サブコマンド]");
            return result;
        }
        else if (args.length == 2)
        {
            if (!this.commands.containsKey(args[0]))
                return Collections.singletonList("E:存在しないサブコマンドです。ページ番号を指定している場合は第二引数は必要ありません。");

            return Collections.singletonList("[ページ番号]");
        }

        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("ヘルプを表示します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                "[ページ番号|サブコマンド]",
                "[ページ番号]"
        };
    }
}
