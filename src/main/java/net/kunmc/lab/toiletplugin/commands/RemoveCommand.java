package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RemoveCommand extends CommandBase
{

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 1))
            return;

        String toiletName = args[0];

        if (!ToiletPlugin.getPlugin().getGame().getToiletManager().containsToilet(toiletName))
        {
            sender.sendMessage(ChatColor.RED + "E: トイレが見つかりませんでした。");
            return;
        }

        Toilet removedToilet = ToiletPlugin.getPlugin().getGame().getToiletManager().unregisterToilet(toiletName);

        try
        {
            Objects.requireNonNull(Bukkit.getEntity(UUID.fromString(removedToilet.getArmorStandUUID()))).remove();
            Objects.requireNonNull(Bukkit.getEntity(UUID.fromString(removedToilet.getToiletInfoBaseArmorStandUUID()))).remove();
        }
        catch (NullPointerException ignored)
        {
        }

        sender.sendMessage(ChatColor.GREEN + "S: トイレの登記を抹消し、トイレエンティティを削除しました。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length != 1)
            return null;
        return ToiletPlugin.getPlugin().getGame().getToiletManager().getToiletNames();
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("登録されたトイレの登記を抹消します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{required("トイレ名", "string")};
    }
}
