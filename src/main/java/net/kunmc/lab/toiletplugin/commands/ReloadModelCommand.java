package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.toilet.generate.ModelManager;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

public class ReloadModelCommand extends CommandBase
{

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        ModelManager modelManager = ToiletPlugin.getPlugin().getModelManager();

        ToiletPlugin.getPlugin().getModelManager().clear();

        modelManager.scan(new File(ToiletPlugin.getPlugin().getDataFolder(), "assets/toilet_models"));

        Bukkit.getWorlds().stream().parallel()
                .forEach(world -> modelManager.scan(new File(world.getWorldFolder(), "generated")));


        sender.sendMessage(ChatColor.GREEN + "S: トイレモデルの情報を再読込しました。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("トイレモデルの情報を再読込します");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
