package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerStateManager implements Listener
{
    @Getter
    private final HashMap<Player, GamePlayer> gamePlayers;

    private final GameMain game;

    public PlayerStateManager(GameMain game)
    {
        this.gamePlayers = new HashMap<>();
        this.game = game;
    }

    public List<GamePlayer> getPlayers()
    {
        return new ArrayList<>(this.gamePlayers.values());
    }

    public GamePlayer getPlayer(Player player)
    {
        return this.gamePlayers.get(player);
    }

    public void init()
    {
        Bukkit.getPluginManager().registerEvents(this, ToiletPlugin.getPlugin());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        this.gamePlayers.put(e.getPlayer(), new GamePlayer(e.getPlayer()));

        this.updatePlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        this.gamePlayers.remove(e.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e)
    {
        this.updatePlayer(e.getPlayer(), e.getNewGameMode());
    }

    public boolean isPlaying(Player player)
    {
        return getPlayer(player).isPlaying();
    }

    public boolean isSpectating(Player player)
    {
        return getPlayer(player).isSpectating();
    }

    public void updatePlayer(Player player)
    {
        this.updatePlayer(player, player.getGameMode());
    }

    public void updatePlayer(Player player, GameMode mode)
    {
        GamePlayer gamePlayer = this.gamePlayers.get(player);

        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE)
        {
            gamePlayer.setPlayState(PlayState.PLAYING);

            if (this.game.getConfig().isAutoScheduleOnJoin())
                gamePlayer.setQuestProgress(QuestProgress.SCHEDULED, this.game.getConfig().generateScheduleTime());
        }
        else
        {
            if (gamePlayer.isQuesting())
                this.game.getQuestManager().cancel(player, true);
            else if (gamePlayer.isScheduled())
                this.game.getQuestManager().unSchedule(player);


            gamePlayer.purge();
            gamePlayer.setPlayState(PlayState.SPECTATING);
        }
    }
}
