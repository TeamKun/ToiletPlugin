package net.kunmc.lab.toiletplugin.commands;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.SubCommandable;
import net.kunmc.lab.toiletplugin.commands.debug.ChangeToiletCooldownChangeCommand;
import net.kunmc.lab.toiletplugin.commands.debug.ChangeToiletStateCommand;
import net.kunmc.lab.toiletplugin.commands.debug.CreateHDCommand;
import net.kunmc.lab.toiletplugin.commands.debug.KillPassengersCommand;
import net.kunmc.lab.toiletplugin.commands.debug.PlaySoundCommand;
import net.kunmc.lab.toiletplugin.commands.debug.PurgeToiletLocalDataCommand;
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
        map.put("purgeToiletLocalData", new PurgeToiletLocalDataCommand(game));
        map.put("playSound", new PlaySoundCommand());
        map.put("killPassengers", new KillPassengersCommand());
        map.put("createHD", new CreateHDCommand());
        return map;
    }
}
