package net.kunmc.lab.toiletplugin.game;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventListener implements Listener
{
    private final GameMain game;

    public GameEventListener(GameMain game)
    {
        this.game = game;
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e)
    {
        this.game.getPlayerStateManager().updatePlayer(e.getPlayer(), e.getNewGameMode());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        this.game.getPlayerStateManager().updatePlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        this.game.getPlayerStateManager().updatePlayer(e.getPlayer());
    }
}
