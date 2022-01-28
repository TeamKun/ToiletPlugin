package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class ToiletManager
{
    private final GameMain game;
    @Getter
    private final ToiletLogic logic;

    public ToiletManager(GameMain game)
    {
        this.game = game;
        this.logic = new ToiletLogic(game);
    }

    public void init()
    {
        Bukkit.getPluginManager().registerEvents(this.logic, ToiletPlugin.getPlugin());
        this.logic.init();
        this.logic.runTaskTimer(ToiletPlugin.getPlugin(), 0, 2);
    }

    public void playerJoinToilet(Player player, Toilet toilet, ArmorStand informationDisplay)
    {

    }
}
