package net.kunmc.lab.toiletplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class CommandBase
{
    public abstract void onCommand(CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    public abstract TextComponent getHelpOneLine();

    public abstract String[] getArguments();

    protected static String required(String argName, String typeName)
    {
        return ChatColor.AQUA + "<" + argName + ": " + typeName + "> " + ChatColor.RESET;
    }

    protected static String required(String argName, int min, int max)
    {
        return ChatColor.AQUA + "<" + argName + ":int:" + min + "～" + max + "> " + ChatColor.RESET;
    }

    protected static String optional(String argName, String typeName)
    {
        return ChatColor.DARK_AQUA + "[" + argName + ": " + typeName + "] " + ChatColor.RESET;
    }

    protected static String optional(String argName, int min, int max)
    {
        return ChatColor.DARK_AQUA + "[" + argName + ":int:" + min + "～" + max + "] " + ChatColor.RESET;
    }

    protected static TextComponent suggestCommand(String text, String command)
    {
        return Component.text(text)
                .clickEvent(ClickEvent.suggestCommand(command + " "))
                .hoverEvent(Component.text(ChatColor.AQUA + "クリックして補完" + command));
    }

    protected static TextComponent suggestCommand(TextComponent text, String command)
    {
        return text.clickEvent(ClickEvent.suggestCommand(command + " "))
                .hoverEvent(Component.text(ChatColor.AQUA + "クリックして補完" + command));
    }

    protected static TextComponent of(String text)
    {
        return Component.text(text);
    }
}
