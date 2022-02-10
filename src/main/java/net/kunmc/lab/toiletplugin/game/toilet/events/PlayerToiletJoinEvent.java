package net.kunmc.lab.toiletplugin.game.toilet.events;

import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerToiletJoinEvent extends ToiletEvent
{
    private static final HandlerList handlers = new HandlerList();

    public PlayerToiletJoinEvent(GamePlayer who, OnGroundToilet toilet)
    {
        super(who, toilet);
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }
}
