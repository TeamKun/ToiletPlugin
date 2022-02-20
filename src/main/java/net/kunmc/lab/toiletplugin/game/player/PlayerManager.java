package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerManager extends BukkitRunnable implements Listener
{
    @Getter
    private final HashMap<Player, GamePlayer> gamePlayers;

    private final GameMain game;

    public PlayerManager(GameMain game)
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
        this.runTaskTimerAsynchronously(ToiletPlugin.getPlugin(), 0, 10);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        this.gamePlayers.put(e.getPlayer(), new GamePlayer(e.getPlayer(), game));

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickUpPoop(PlayerAttemptPickupItemEvent e)
    {
        if (e.getItem().getPersistentDataContainer().has(
                new NamespacedKey(ToiletPlugin.getPlugin(), "poop_item"),
                PersistentDataType.STRING
        ))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
        getPlayer(e.getEntity()).onDeath();
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
                gamePlayer.setQuestPhase(QuestPhase.SCHEDULED, this.game.getConfig().generateScheduleTime());
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

    @Override
    public void run()
    {
        this.gamePlayers.forEach((player, gamePlayer) -> gamePlayer.getDisplay().updateScreen());
    }
}
