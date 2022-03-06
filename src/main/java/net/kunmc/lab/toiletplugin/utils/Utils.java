package net.kunmc.lab.toiletplugin.utils;

import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class Utils
{
    public static String convertExplict(GameConfig config, String from, String alternative)
    {
        return config.isExplictExpressionEnable() ? from: alternative;
    }

    public static String convertExplict(GameMain game, String from, String alternative)
    {
        return convertExplict(game.getConfig(), from, alternative);
    }

    public static String replaceExplict(GameConfig config, String str, String from, String to)
    {
        return config.isExplictExpressionEnable() ? str.replace(from, to): str;
    }

    public static String replaceExplict(GameMain game, String str, String from, String to)
    {
        return replaceExplict(game.getConfig(), str, from, to);
    }

    public static void killPassenger(List<Entity> passengers, Class<?>... types)
    {
        List<Class<?>> typeList = Arrays.asList(types);

        passengers.stream()
                .filter(entity -> {
                    if (types.length == 0)
                        return true;
                    return typeList.stream().parallel()
                            .anyMatch(type -> type.isInstance(entity));
                })
                .forEach(entity -> {
                    killPassenger(entity.getPassengers(), types);
                    entity.remove();
                });
    }

    public static void setPoopVelocityRandom(Item item, int seed, double speed)
    {
        switch (seed)
        {
            case 0:
                item.setVelocity(new Vector(speed, 0, 0));
                break;
            case 1:
                item.setVelocity(new Vector(-speed, 0, 0));
                break;
            case 2:
                item.setVelocity(new Vector(0, 0, speed));
                break;
            case 3:
                item.setVelocity(new Vector(0, 0, -speed));
                break;
            case 4:
                item.setVelocity(new Vector(speed, speed, 0));
                break;
            case 5:
                item.setVelocity(new Vector(-speed, -speed, 0));
                break;
            case 6:
                item.setVelocity(new Vector(speed, -speed, 0));
                break;
            case 7:
                item.setVelocity(new Vector(-speed, speed, 0));
                break;
            case 8:
                item.setVelocity(new Vector(speed, 0, speed));
                break;
            case 9:
                item.setVelocity(new Vector(-speed, 0, -speed));
                break;
            case 10:
                item.setVelocity(new Vector(0, speed, speed));
                break;
            case 11:
                item.setVelocity(new Vector(0, -speed, -speed));
                break;
            case 12:
                item.setVelocity(new Vector(speed, speed, speed));
                break;
            case 13:
                item.setVelocity(new Vector(-speed, -speed, -speed));
                break;
            case 14:
                item.setVelocity(new Vector(speed, -speed, -speed));
                break;
            default:
                item.setVelocity(new Vector(-speed, speed, -speed));
                break;
        }
    }

}
