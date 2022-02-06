package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.toiletobject.generate.ToolManager;
import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class ToolCommand extends CommandBase
{

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 1) ||
                CommandFeedBackUtils.checkPlayer(sender))
            return;

        String modelName = args[0];

        if (!ToiletPlugin.getPlugin().getModelManager().contains(modelName))
        {
            sender.sendMessage(ChatColor.RED + "E: モデルが見つかりませんでした。");
            return;
        }

        Player player = (Player) sender;

        player.getInventory().addItem(ToolManager.getTool(modelName));

        sender.sendMessage(of(ChatColor.GREEN + "S: トイレ生成の箱を作成しました。"));

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length != 1)
            return null;
        File file = new File(ToiletPlugin.getPlugin().getDataFolder(), "assets/toilet_models");
        if (!file.exists())
            return null;
        return ToiletPlugin.getPlugin().getModelManager().getNames();
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("トイレを呼び出すための道具を生成します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("モデル名", "string")
        };
    }
}
