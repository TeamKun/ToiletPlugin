package net.kunmc.lab.toiletplugin.events;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerRespawnEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final GamePlayer who;

    public PlayerRespawnEvent(GamePlayer who)
    {
        super(who.getPlayer());
        this.who = who;
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
