package net.kunmc.lab.toiletplugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandUtils
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
            sender.sendMessage(ChatColor.RED + "E: 引数の数が不正です: 必要: " +
                    (max == -1 ? min: min + "〜" + max) +
                    " 提供: " + args.length);
            return true;
        }

        return false;
    }

    public static List<Player> getPlayer(CommandSender sender, String query)
    {
        List<Player> players = new ArrayList<>();
        Player argPlayer = Bukkit.getPlayer(query);
        if (argPlayer == null)
        {
            List<Entity> entities;
            try
            {

                entities = Bukkit.selectEntities(sender, query);
            }
            catch (IllegalArgumentException e)
            {
                sender.sendMessage(ChatColor.RED + "E: 無効なセレクタを使用しました。");
                return null;
            }

            entities.stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .forEach(players::add);

            if (players.isEmpty())
            {
                sender.sendMessage(ChatColor.RED + "E: プレイヤーが見つかりませんでした。");
                return null;
            }
        }
        else
            players.add(argPlayer);
        return players;
    }

    public static boolean checkPlayer(@NotNull CommandSender sender)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "E: このコマンドはプレイヤーからのみ実行できます！");
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
                sender.sendMessage(ChatColor.RED + "E: 引数の値が不正です: 必要: " +
                        (max == -1 ? min: min + "〜" + max) +
                        " 提供: " + num);
                return null;
            }
            return num;
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(ChatColor.RED + "引数が数値ではありません: 必要: " +
                    (max == -1 ? min: min + "〜" + max) +
                    " 提供: " + arg);
            return null;
        }
    }

    public static @Nullable Integer parseInteger(@NotNull CommandSender sender, @NotNull String arg, int min)
    {
        return parseInteger(sender, arg, min, -1);
    }

}
