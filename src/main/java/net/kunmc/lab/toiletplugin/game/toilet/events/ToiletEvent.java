package net.kunmc.lab.toiletplugin.game.toilet.events;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import org.bukkit.event.player.PlayerEvent;

public abstract class ToiletEvent extends PlayerEvent
{
    @Getter
    private final GamePlayer gamePlayer;
    @Getter
    private final OnGroundToilet toilet;

    public ToiletEvent(GamePlayer who, OnGroundToilet toilet)
    {
        super(who.getPlayer());
        this.gamePlayer = who;
        this.toilet = toilet;
    }
}
