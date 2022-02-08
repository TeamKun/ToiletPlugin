package net.kunmc.lab.toiletplugin.game.quest;

import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.player.PlayerStateManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestManager extends BukkitRunnable
{
    private final GameMain game;
    private final QuestLogic logic;
    private final PlayerStateManager stateManager;

    public QuestManager(GameMain game)
    {
        this.game = game;
        this.logic = new QuestLogic(game, this);

        this.stateManager = game.getPlayerStateManager();
    }

    public void init()
    {
        this.logic.init();
        this.runTaskTimer(ToiletPlugin.getPlugin(), 0, 20);
    }

    public int start(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isPlaying())
            return -1;

        if (info.isQuesting())
            return -1;

        int questTime = this.game.getConfig().generateQuestTime();

        info.setQuestProgress(QuestProgress.STARTED, questTime);

        player.sendMessage(ChatColor.DARK_RED + "あなたは便意を感じている... ");
        player.sendMessage(ChatColor.RED + "あなたは" + questTime + "秒以内に排便をしないと死んでしまう！");

        player.sendTitle(ChatColor.RED + "緊急クエスト発生：トイレに向かう",
                ChatColor.YELLOW + "使えるトイレを探して中に入ろう！", 5, 40, 5
        );

        return questTime;
    }

    public int cancel(Player player, boolean isNever)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isQuesting())
            return -1;

        info.setQuestProgress(QuestProgress.NONE, 0);

        player.sendMessage(ChatColor.GREEN + "あなたのクエストがキャンセルされました。");
        if (!isNever)
            return this.changeScheduledTime(player);
        return 0;
    }

    public boolean isQuesting(Player player)
    {
        return this.stateManager.getPlayer(player).isQuesting();
    }

    public boolean isScheduled(Player player)
    {
        return this.stateManager.getPlayer(player).getQuestProgress() == QuestProgress.SCHEDULED;
    }

    public int reSchedule(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (info.getQuestProgress() != QuestProgress.SCHEDULED)
            return -1;

        int scheduleTime = this.game.getConfig().generateScheduleTime();

        info.setTime(scheduleTime);
        return scheduleTime;
    }

    public boolean unSchedule(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);
        if (info.getQuestProgress() != QuestProgress.SCHEDULED)
            return false;
        info.setQuestProgress(QuestProgress.NONE, 0);
        return true;
    }

    public int changeScheduledTime(Player player, int time)
    {
        GamePlayer info = this.stateManager.getPlayer(player);
        if (!info.isPlaying())
            return -1;

        if (info.isQuesting())
            return -1;

        info.setQuestProgress(QuestProgress.SCHEDULED, time);
        return time;
    }

    public int changeScheduledTime(Player player)
    {
        return this.changeScheduledTime(player, this.game.getConfig().generateScheduleTime());
    }

    public Integer getScheduledTime(Player player)
    {
        return getTime(player);
    }

    public Integer getQuestTime(Player player)
    {
        return getTime(player);
    }

    private Integer getTime(Player player)
    {
        return this.stateManager.getPlayer(player).getTime();
    }

    public void onPlayerSuccessQuest(Player player)
    {
        if (this.game.getConfig().isAutoRescheduleOnSuccess())
            this.reSchedule(player);
    }

    public void onPlayerFailedQuest(GamePlayer gamePlayer)
    {
        Player player = gamePlayer.getPlayer();

        player.setKiller(null);
        player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, 0.11235));
        player.setHealth(0d);
        gamePlayer.setQuestProgress(QuestProgress.NONE, 0);
    }

    @Override
    public void run()
    {
        this.stateManager.getPlayers().forEach(info -> {
            Player player = info.getPlayer();

            if (!info.isPlaying())
                return;

            if (info.getQuestProgress() == QuestProgress.NONE)
                return;

            if (info.getTime() == 0)
                if (info.isQuesting())
                    this.onPlayerFailedQuest(info);
                else
                    this.start(player);

            info.setTime(info.getTime() - 1);
        });
    }
}
