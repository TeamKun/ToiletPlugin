package net.kunmc.lab.toiletplugin.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
public class HelpCommand extends CommandBase
{
    private HashMap<String, CommandBase> commands;

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        int page_max = commands.size() / 5 + 1;

        Integer page = 1;
        if (args.length > 0 && (page = CommandFeedBackUtils.parseInteger(sender, args[0], 1, page_max)) == null)
            return;

        sender.sendMessage(ChatColor.GOLD + "-----=====    ToiletPlugin (" + page + "/" + page_max + ")  =====-----");

        int start = (page - 1) * 5;
        int end = Math.min(start + 5, commands.size());

        int max_length = commands.keySet().stream().mapToInt(String::length).max().orElse(0);

        commands.entrySet().stream()
                .skip(start)
                .limit(end - start)
                .forEach(entry -> send(sender, entry.getValue().getHelpOneLine(), entry.getKey(), max_length));

        TextComponent footer = of(ChatColor.GOLD + "-----=====");

        if (page > 1)
            footer = footer.append(of(ChatColor.GOLD + " [" + ChatColor.RED + "<<" + ChatColor.GOLD + "]")
                    .clickEvent(ClickEvent.runCommand("/toilet help " + (page - 1)))
                    .hoverEvent(HoverEvent.showText(of(ChatColor.AQUA + "クリックして前のページに戻る"))));
        else
            footer = footer.append(of("     "));

        footer = footer.append(of(ChatColor.GOLD + " ToiletPlugin "));

        if (page < page_max)
            footer = footer.append(of( ChatColor.GOLD + "[" + ChatColor.GREEN + ">>" + ChatColor.GOLD + "] ")
                    .clickEvent(ClickEvent.runCommand("/toilet help " + (page + 1)))
                    .hoverEvent(HoverEvent.showText(of(ChatColor.AQUA + "クリックして次のページに進む"))));
        else
            footer = footer.append(of("    "));

        sender.sendMessage(footer.append(of(ChatColor.GOLD + "=====-----")));
    }

    private static void send(CommandSender sender,  TextComponent helpMessage, String commandName, int space_size)
    {
        sender.sendMessage(suggestCommand(of(ChatColor.AQUA + "/toilet " + commandName + " " +
                        StringUtils.repeat(" ", space_size - commandName.length()) + " - ")
                .append(Component.text(ChatColor.DARK_AQUA.toString()))
                .append(helpMessage.color(NamedTextColor.DARK_AQUA)),
                "/toilet " + commandName));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        return Collections.singletonList("[ページ番号]");
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("ヘルプを表示します。");
    }
}
