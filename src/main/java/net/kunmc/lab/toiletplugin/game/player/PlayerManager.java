package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.DefecationType;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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

    @EventHandler
    public void onShift(PlayerToggleSneakEvent e)
    {
        if (e.isSneaking() && this.game.getConfig().getDefecationType() == DefecationType.SHIFT_MASH)
        {
            int gain = this.game.getConfig().getPowerGainAmount();
            GamePlayer gamePlayer = this.gamePlayers.get(e.getPlayer());
            gamePlayer.setNowPower(gamePlayer.getNowPower() + gain);
            GameSound.TOILETPLAYER_POWER_CHANGE.play(gamePlayer, 0.5F,
                    (gamePlayer.getNowPower() / 100.0F) + 0.8F
            );
        }
    }

    @Override
    public void run()
    {
        int gain = Math.toIntExact(Math.round((double) this.game.getConfig().getPowerGainAmount() / 2.0d));
        int loss = Math.toIntExact(Math.round((double) this.game.getConfig().getPowerLossOnSecAmount() / 2.0d));

        DefecationType defecationType = this.game.getConfig().getDefecationType();

        boolean burst = this.game.getConfig().isBurstOnPowerOver100();

        this.gamePlayers.forEach((player, gamePlayer) -> {
            gamePlayer.getDisplay().updateScreen();
            if (gamePlayer.getQuestPhase() != QuestPhase.TOILET_JOINED)
                return;

            if (gamePlayer.getNowPower() >= 100 && burst)
            {
                onBurstAsSync(gamePlayer);
                return;
            }

            if (defecationType == DefecationType.SHIFT_HOLD && player.isSneaking())
            {
                gamePlayer.setNowPower(Math.min(gamePlayer.getNowPower() + gain, 100));
                GameSound.TOILETPLAYER_POWER_CHANGE.play(player, 0.5F,
                        (gamePlayer.getNowPower() / 100.0F) + 0.6F
                );
            }
            else if (gamePlayer.getNowPower() > 0)
                gamePlayer.setNowPower(Math.max(0, gamePlayer.getNowPower() - loss));
        });
    }

    private void onBurstAsSync(GamePlayer player)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                game.getQuestManager().onBurst(player);
            }
        }.runTask(ToiletPlugin.getPlugin());
    }
}
