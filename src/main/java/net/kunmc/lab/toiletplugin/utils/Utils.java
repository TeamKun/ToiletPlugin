package net.kunmc.lab.toiletplugin.utils;

import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import org.bukkit.entity.Entity;

import java.util.List;

public class Utils
{
    public static String convertExplict(GameConfig config, String from, String alternative)
    {
        return config.isEnableExplictExpression() ? from: alternative;
    }

    public static String convertExplict(GameMain game, String from, String alternative)
    {
        return convertExplict(game.getConfig(), from, alternative);
    }

    public static String replaceExplict(GameConfig config, String str, String from, String to)
    {
        return config.isEnableExplictExpression() ? str.replace(from, to): str;
    }

    public static String replaceExplict(GameMain game, String str, String from, String to)
    {
        return replaceExplict(game.getConfig(), str, from, to);
    }

    public static void killPassenger(List<Entity> passengers)
    {
        passengers.forEach(entity -> killPassenger(entity.getPassengers()));

        passengers.forEach(Entity::remove);
    }
}
