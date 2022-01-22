package net.kunmc.lab.toiletplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandFeedBackUtils
{
    public static boolean unPermMessage(@NotNull CommandSender sender, String perm)
    {
        if (sender.hasPermission(perm))
            return false;
        sender.sendMessage(ChatColor.RED + "E: このコマンドを使用するには権限が必要です！");
        return true;
    }

    public static boolean invalidLengthMessage(@NotNull CommandSender sender, String[] args, int min, int max)
    {
        if (min != -1 && args.length < min || (max != -1 && args.length > max))
        {
            sender.sendMessage(ChatColor.RED + "E: 引数の数が不正です:r:r 必要:r:r " +
                    (max == -1 ? min : min + "〜" + max) +
                    " 提供:r:r " + args.length);
            return true;
        }

        return false;
    }

    public static boolean invalidLengthMessage(@NotNull CommandSender sender, String[] args, int min)
    {
        return invalidLengthMessage(sender, args, min, -1);
    }

    public static @Nullable Integer parseInteger(@NotNull CommandSender sender, @NotNull String arg, int min, int max)
    {

        try
        {
            int num = Integer.parseInt(arg);
            if (num < min || (max != -1 && num > max))
            {
                sender.sendMessage(ChatColor.RED + "E: 引数の値が不正です:r:r 必要:r:r " +
                        (max == -1 ? min : min + "〜" + max) +
                        " 提供:r:r " + num);
                return null;
            }
            return num;
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(ChatColor.RED + "引数が数値ではありません:r:r 必要:r:r " +
                    (max == -1 ? min : min + "〜" + max) +
                    " 提供:r:r " + arg);
            return null;
        }
    }

    public static @Nullable Integer parseInteger(@NotNull CommandSender sender, @NotNull String arg, int min)
    {
        return parseInteger(sender, arg, min, -1);
    }

}
