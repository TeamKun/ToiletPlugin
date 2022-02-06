package net.kunmc.lab.toiletplugin.game.quest;

import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

public class QuestManager extends BukkitRunnable
{
    private final GameMain game;
    private final QuestLogic logic;

    private final HashMap<Player, Integer> questingPlayer;
    private final HashMap<Player, Integer> scheduledPlayer;

    public QuestManager(GameMain game)
    {
        this.game = game;
        this.questingPlayer = this.game.getPlayerStateManager().getQuestingPlayer();
        this.scheduledPlayer = this.game.getPlayerStateManager().getQuestScheduledPlayer();
        this.logic = new QuestLogic(game, this);
    }

    public void init()
    {
        this.logic.init();
        this.runTaskTimer(ToiletPlugin.getPlugin(), 0, 20);
    }

    public int start(Player player)
    {
        if (!this.game.getPlayerStateManager().isPlaying(player))
            return -1;

        if (this.questingPlayer.containsKey(player))
            return -1;

        this.scheduledPlayer.remove(player);
        this.questingPlayer.put(player, 30);

        player.sendTitle(ChatColor.RED + "緊急クエスト発生：トイレに向かう",
                ChatColor.YELLOW + "使えるトイレを探して中に入ろう！", 5, 40, 5
        );

        return 30;
    }

    public boolean cancel(Player player, boolean isNever)
    {
        if (!this.questingPlayer.containsKey(player))
            return false;

        this.questingPlayer.remove(player);
        player.sendMessage(ChatColor.GREEN + "あなたのクエストがキャンセルされました。");
        if (!isNever)
            this.changeScheduledTime(player);
        return true;
    }

    public boolean isQuesting(Player player)
    {
        return this.questingPlayer.containsKey(player);
    }

    public boolean isScheduled(Player player)
    {
        return this.scheduledPlayer.containsKey(player);
    }

    public int reSchedule(Player player)
    {
        this.questingPlayer.remove(player);
        this.scheduledPlayer.put(player, 30);
        return 30;
    }

    public int changeScheduledTime(Player player, int time)
    {
        if (!this.game.getPlayerStateManager().isPlaying(player))
            return -1;

        if (this.questingPlayer.containsKey(player))
            return -1;

        this.scheduledPlayer.put(player, time);
        return time;
    }

    public int changeScheduledTime(Player player)
    {
        return this.changeScheduledTime(player, new Random().nextInt(170) + 10);
    }

    public Integer getScheduledTime(Player player)
    {
        return this.scheduledPlayer.get(player);
    }

    public Integer getQuestTime(Player player)
    {
        return this.questingPlayer.get(player);
    }

    @Override
    public void run()
    {
        for (Player player : this.scheduledPlayer.keySet())
        {
            if (this.scheduledPlayer.get(player) == 0)
            {
                player.sendMessage(ChatColor.DARK_RED + "あなたは便意を感じている... ");
                player.sendMessage(ChatColor.RED + "あなたは" + start(player) + "秒以内に排便をしないと死んでしまう！");
            }
            else
                this.scheduledPlayer.put(player, this.scheduledPlayer.get(player) - 1);
        }

        for (Player player : this.questingPlayer.keySet())
        {
            if (this.questingPlayer.get(player) == 0)
            {
                player.setKiller(null);
                player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, 0.11235));
                player.setHealth(0d);
                this.questingPlayer.remove(player);
            }
            else
                this.questingPlayer.put(player, this.questingPlayer.get(player) - 1);
        }
    }
}
