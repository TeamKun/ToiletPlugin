package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameConfig;
import net.kunmc.lab.toiletplugin.game.config.Config;
import net.kunmc.lab.toiletplugin.game.config.ConfigManager;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kunmc.lab.toiletplugin.utils.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.naming.SizeLimitExceededException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ConfigCommand extends CommandBase
{
    private final ConfigManager config;

    public ConfigCommand(ConfigManager config)
    {
        this.config = config;
    }

    private static String getArgument(String configName, ConfigManager.GeneratedConfig config)
    {
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
            sb.append(":").append(a).append("~");
        }
        if (max != -1)
        {
            String a;
            if (max % 1 == 0)
                a = String.valueOf((int) max);
            else
                a = String.valueOf(max);
            if (min == -1)
                sb.append(":");
            sb.append(a);
        }
        sb.append(">");
        return sb.toString();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            showPagedHelp(sender, 1);
            return;
        }

        if (CommandUtils.invalidLengthMessage(sender, args, 1, 2))
            return;

        String configName = args[0];

        if (configName.equals("help"))
        {
            Integer page;
            if (args.length == 2 && (page = CommandUtils.parseInteger(sender, args[1], 1)) != null)
            {
                showPagedHelp(sender, page);
                return;
            }
            showPagedHelp(sender, 1);
            return;
        }

        if (!this.config.isConfigExist(configName))
        {
            sender.sendMessage(ChatColor.RED + "E: 存在しないコンフィグ名です");
            return;
        }

        ConfigManager.GeneratedConfig config = this.config.getConfigAllowRanged(configName, false);
        ConfigManager.GeneratedConfig configMax = this.config.getConfigAllowRanged(configName, true);

        Config define = config.getDefine();
        Config defineMax = config.getDefine();

        if (define.toggle())
        {
            config.setValue(!config.booleanValue());
            sender.sendMessage(ChatColor.GREEN + "S: " + configName + "を" + config.booleanValue() + "に設定しました。");
            return;
        }

        if (args.length == 1)
        {
            if (!define.ranged())
            {
                sender.sendMessage(ChatColor.GREEN + "S: " + configName + "は" + config.getValue() + "です。");
                return;
            }

            sender.sendMessage(ChatColor.GREEN + "S: " + configName + "は" + config.getValue() + "~" + configMax.getValue() + "です。");
            return;
        }

        String value = args[1];

        Object currentValueMin = config.getValue();
        Object currentValueMax = configMax.getValue();

        try
        {
            if (define.ranged())
            {
                Pair<String, String> range = parsePair(sender, value, defineMax.min(), defineMax.max(), defineMax.min(), defineMax.max());

                if (range == null)
                    return;

                if (range.getLeft() != null)
                    this.config.setValue(config.getField().getName(), range.getLeft());
                if (range.getRight() != null)
                    this.config.setValue(configMax.getField().getName(), range.getRight());
            }
            else
                this.config.setValue(configName, value);

            sender.sendMessage(ChatColor.GREEN + "S: " + configName + "を" + value + "に設定しました。");

            List<Pair<String, String>> errors = ((GameConfig) this.config.getConfig()).checkConfig();
            if (errors.size() > 0)
            {
                sender.sendMessage(ChatColor.RED + "E: 設定にエラーを検出しました。");
                boolean rollback = false;
                for (Pair<String, String> error : errors)
                {
                    sender.sendMessage(ChatColor.RED + "E: " + error.getLeft() + ": " + error.getRight());
                    if (error.getLeft().equals(configName))
                        rollback = true;
                    else if (error.getLeft().equals("min" + configName.substring(0, 1).toUpperCase() + configName.substring(1)))
                        rollback = true;
                }

                if (rollback && define.ranged())
                {
                    config.setValue(currentValueMin);
                    configMax.setValue(currentValueMax);
                }
                else if (rollback)
                    config.setValue(currentValueMin);

                sender.sendMessage(ChatColor.GREEN + "S: 設定をロールバックしました。");
            }
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
        catch (IllegalAccessException e)
        {
            sender.sendMessage(ChatColor.RED + "E: " + value + " は有効な真偽値ではありません。");
        }
        catch (ClassNotFoundException e)
        {
            sender.sendMessage(ChatColor.RED + "E: " + value + " は有効な値ではありません。使用可: " +
                    String.join(", ", config.getDefine().enums()));
        }
    }

    private Pair<String, String> parsePair(CommandSender sender, String arg, double minMin, double minMax, double maxMin, double maxMax)
    {
        if (!arg.contains("~"))
        {
            sender.sendMessage(ChatColor.RED + "E: 範囲値には ~ を含み、最小または最大 もしくは両方を指定する必要があります。");
            return null;
        }

        String minSpec = null;
        String maxSpec = null;

        // 10~20
        if (arg.startsWith("~"))
            maxSpec = arg.substring(1);
        else if (arg.endsWith("~"))
            minSpec = arg.substring(0, arg.length() - 1);
        else
        {
            String[] split = StringUtils.split(arg, '~');
            if (split.length != 2)
            {
                sender.sendMessage(ChatColor.RED + "E: 範囲値には ~ を含み、最小または最大 もしくは両方を指定する必要があります。");
                return null;
            }

            minSpec = split[0];
            maxSpec = split[1];
        }

        if (minSpec == null && maxSpec == null)
        {
            sender.sendMessage(ChatColor.RED + "E: 範囲値には ~ を含み、最小または最大 もしくは両方を指定する必要があります。");
            return null;
        }

        Double min = null;
        Double max = null;

        if (minSpec != null && (min = CommandUtils.parseDouble(sender, minSpec, minMin, minMax)) == null)
            return null;
        if (maxSpec != null && (max = CommandUtils.parseDouble(sender, maxSpec, maxMin, maxMax)) == null)
            return null;

        if (CommandUtils.checkValidNumber(sender, min, max))
            return null;

        return new Pair<>(minSpec, maxSpec);
    }

    private void showPagedHelp(CommandSender sender, int page)
    {
        AtomicInteger maxLength = new AtomicInteger(0);
        HashMap<String, ConfigManager.GeneratedConfig> helps = config.getMap();

        int maxPage = helps.size() / 10 + 1;

        if (page > maxPage)
        {
            sender.sendMessage(ChatColor.RED + "E: 存在しないページです。");
            return;
        }

        helps.forEach((s, s2) -> maxLength.set(Math.max(maxLength.get(), s.length())));

        sender.sendMessage(ChatColor.GOLD + "-----=====     ToiletPlugin (" + page + "/" + maxPage + ")  =====-----");

        helps.entrySet().stream()
                .skip((page - 1) * 10L)
                .limit(10)
                .map(entry -> ChatColor.AQUA + entry.getKey() +
                        StringUtils.repeat(" ", maxLength.get() - entry.getKey().length()) +
                        "  -  " +
                        ChatColor.DARK_AQUA + entry.getValue().getDefine().helpMessage() + "\n" +
                        "    " + ChatColor.AQUA + getArgument(entry.getKey(), entry.getValue()))
                .map(Component::text)
                .map(textComponent -> textComponent
                        .clickEvent(ClickEvent.suggestCommand("/toilet config " +
                                textComponent.content().substring(2, textComponent.content().indexOf(" "))
                                + " "))
                        .hoverEvent(HoverEvent.showText(Component.text(ChatColor.AQUA + "クリックして設定を使用します。")))
                )
                .forEach(sender::sendMessage);

        TextComponent footer = of(ChatColor.GOLD + "-----=====");

        if (page > 1)
            footer = footer.append(of(ChatColor.GOLD + " [" + ChatColor.RED + "<<" + ChatColor.GOLD + "]")
                    .clickEvent(ClickEvent.runCommand("/toilet config help " + (page - 1)))
                    .hoverEvent(HoverEvent.showText(of(ChatColor.AQUA + "クリックして前のページに戻る"))));
        else
            footer = footer.append(of("     "));

        footer = footer.append(of(ChatColor.GOLD + " ToiletPlugin "));

        if (page < maxPage)
            footer = footer.append(of(ChatColor.GOLD + "[" + ChatColor.GREEN + ">>" + ChatColor.GOLD + "] ")
                    .clickEvent(ClickEvent.runCommand("/toilet config help " + (page + 1)))
                    .hoverEvent(HoverEvent.showText(of(ChatColor.AQUA + "クリックして次のページに進む"))));
        else
            footer = footer.append(of("    "));

        sender.sendMessage(footer.append(of(ChatColor.GOLD + "=====-----")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return this.config.getConfigs().stream().parallel()
                        .filter(generatedConfig -> {
                            Config define = generatedConfig.getDefine();
                            if (!define.ranged())
                                return true;
                            return generatedConfig.getField().getName().startsWith("min");
                        })
                        .map(generatedConfig -> {
                            String name = generatedConfig.getField().getName();
                            if (name.startsWith("min"))
                                name = name.substring(3, 4).toLowerCase() + name.substring(4);
                            return name;
                        })
                        .collect(Collectors.toList());
            case 2:
                String configName = args[0];
                if (!this.config.isConfigExist(configName))
                    return Collections.singletonList("存在しないコンフィグ名です。");

                ConfigManager.GeneratedConfig config = this.config.getConfigAllowRanged(configName, false);

                List<String> result = new ArrayList<>();
                if (config.getDefine().ranged())
                {
                    String arg = getArgument(configName, config) + "~";
                    ConfigManager.GeneratedConfig maxConfig = this.config.getConfigAllowRanged(configName, true);
                    if (maxConfig != null)
                        arg += getArgument(configName, maxConfig);
                    result.add(arg);
                }
                else
                    result.add(getArgument(configName, config));
                result.addAll(Arrays.asList(config.getDefine().enums()));
                if (config.getField().getType() == boolean.class || config.getField().getType() == Boolean.class)
                    result.addAll(Arrays.asList("true", "false"));
                return result;
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
