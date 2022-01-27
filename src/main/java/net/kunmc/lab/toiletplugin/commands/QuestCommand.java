package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.SubCommandable;
import net.kunmc.lab.toiletplugin.commands.quest.StartCommand;
import net.kunmc.lab.toiletplugin.commands.quest.WaitCommand;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kyori.adventure.text.TextComponent;

import java.util.HashMap;
import java.util.Map;

public class QuestCommand extends SubCommandable
{
    private final GameMain game;

    public QuestCommand(GameMain game)
    {
        super();
        this.game = game;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("クエストを管理します。");
    }

    @Override
    public String getName()
    {
        return "quest";
    }

    @Override
    public Map<String, CommandBase> getSubCommands()
    {
        HashMap<String, CommandBase> subCommands = new HashMap<>();
        subCommands.put("start", new StartCommand(game));
        subCommands.put("wait", new WaitCommand(game));
        return subCommands;
    }
}
