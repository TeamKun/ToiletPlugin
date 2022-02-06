package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.SubCommandable;
import net.kunmc.lab.toiletplugin.commands.debug.ChangeToiletCooldownChangeCommand;
import net.kunmc.lab.toiletplugin.commands.debug.ChangeToiletStateCommand;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kyori.adventure.text.TextComponent;

import java.util.HashMap;
import java.util.Map;

public class DebugCommand extends SubCommandable
{
    private final GameMain game;

    public DebugCommand(GameMain game)
    {
        super();
        this.game = game;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("デバッグ用のコマンドです。");
    }

    @Override
    public String getName()
    {
        return "debug";
    }

    @Override
    public Map<String, CommandBase> getSubCommands()
    {
        HashMap<String, CommandBase> map = new HashMap<>();
        map.put("changeToiletState", new ChangeToiletStateCommand(game));
        map.put("changeToiletCooldown", new ChangeToiletCooldownChangeCommand(game));
        return map;
    }
}
