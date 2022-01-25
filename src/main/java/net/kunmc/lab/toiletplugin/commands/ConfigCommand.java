package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.config.ConfigManager;
import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.naming.SizeLimitExceededException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigCommand extends CommandBase
{
    private final ConfigManager config;

    public ConfigCommand(ConfigManager config)
    {
        this.config = config;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 1, 2))
            return;

        String configName = args[0];

        if (!this.config.isConfigExist(configName))
        {
            sender.sendMessage(ChatColor.RED + "E: 存在しないコンフィグ名です");
            return;
        }

        ConfigManager.GeneratedConfig config = this.config.getConfig(configName);

        if (config.getDefine().toggle())
        {
            config.setValue(!config.booleanValue());
            sender.sendMessage(ChatColor.GREEN + "S: " + configName + "を" + config.booleanValue() + "に設定しました。");
            return;
        }

        if (args.length == 1)
        {
            sender.sendMessage(ChatColor.GREEN + "S: " + configName + "は" + config.getValue() + "です。");
            return;
        }

        String value = args[1];
        try
        {
            this.config.setValue(configName, value);
            sender.sendMessage(ChatColor.GREEN + "S: " + configName + "を" + value + "に設定しました。");
        }
        catch (SizeLimitExceededException e)
        {
            sender.sendMessage(ChatColor.RED + "E: 値が大きすぎます。最大: " + (config.getDefine().max() % 1 == 0 ? (int) config.getDefine().max(): config.getDefine().max()));
        }
        catch (NegativeArraySizeException e)
        {
            sender.sendMessage(ChatColor.RED + "E: 値が小さすぎます。最小: " + (config.getDefine().min() % 1 == 0 ? (int) config.getDefine().min(): config.getDefine().min()));
        }
        catch (NoSuchFieldException e)
        {
            sender.sendMessage(ChatColor.RED + "E: 存在しないコンフィグです。");
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(ChatColor.RED + "E: " + value + " は有効な数値ではありません。");
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return this.config.getConfigs().stream().parallel()
                        .map(generatedConfig -> generatedConfig.getDefine().name().equals("")
                                ? generatedConfig.getField().getName(): generatedConfig.getDefine().name())
                        .collect(Collectors.toList());
            case 2:
                String configName = args[0];
                if (!this.config.isConfigExist(configName))
                    return Collections.singletonList("存在しないコンフィグ名です。");
                ConfigManager.GeneratedConfig config = this.config.getConfig(configName);

                if (config.getDefine().toggle())
                    return null;

                StringBuilder sb = new StringBuilder("<").append(configName).append(":").append(config.getField().getType().getSimpleName());

                double min = config.getDefine().min();
                double max = config.getDefine().max();
                if (min != -1)
                {
                    String a;
                    if (min % 1 == 0)
                        a = String.valueOf((int) min);
                    else
                        a = String.valueOf(min);
                    sb.append(a).append("~");
                }
                if (max != -1)
                {
                    String a;
                    if (max % 1 == 0)
                        a = String.valueOf((int) max);
                    else
                        a = String.valueOf(max);
                    sb.append(a);
                }
                sb.append(">");
                return Collections.singletonList(sb.toString());
            default:
                return null;
        }
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
