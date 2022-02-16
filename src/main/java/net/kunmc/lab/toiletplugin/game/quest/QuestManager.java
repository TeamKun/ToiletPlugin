package net.kunmc.lab.toiletplugin.game.quest;

import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.player.PlayerManager;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestManager extends BukkitRunnable
{
    private final GameMain game;
    private final QuestLogic logic;
    private final PlayerManager stateManager;

    public QuestManager(GameMain game)
    {
        this.game = game;
        this.logic = new QuestLogic(game, this);

        this.stateManager = game.getPlayerStateManager();
    }

    private int secCount = 0;

    public int start(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isPlaying())
            return -1;

        if (info.isQuesting())
            return -1;

        info.resetPlayerForQuest();

        int questTime = this.game.getConfig().generateQuestTime();

        info.playSound(GameSound.QUEST_START);
        info.setQuestPhase(QuestPhase.STARTED, questTime);

        player.sendMessage(ChatColor.DARK_RED + "あなたは便意を感じている... ");
        player.sendMessage(ChatColor.RED + "あなたは" + questTime + "秒以内に排便をしないと死んでしまう！");


        return questTime;
    }

    public int cancel(Player player, boolean isNever)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isQuesting())
            return -1;

        if (info.getQuestPhase() == QuestPhase.TOILET_JOINED)
        {
            info.getToilet().purge();
            info.setToilet(null);
        }

        info.setQuestPhase(QuestPhase.NONE, 0);

        player.sendMessage(ChatColor.GREEN + "あなたのクエストがキャンセルされました。");
        if (!isNever)
            return this.changeScheduledTime(player);

        info.playSound(GameSound.QUEST_CANCEL);

        info.getDisplay().onQuestCancelled();
        return 0;
    }

    public boolean isQuesting(Player player)
    {
        return this.stateManager.getPlayer(player).isQuesting();
    }

    public boolean isScheduled(Player player)
    {
        return this.stateManager.getPlayer(player).getQuestPhase() == QuestPhase.SCHEDULED;
    }

    public int reSchedule(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (info.getQuestPhase() != QuestPhase.SCHEDULED)
            return -1;

        int scheduleTime = this.game.getConfig().generateScheduleTime();

        info.setTime(scheduleTime);
        return scheduleTime;
    }

    public boolean unSchedule(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);
        if (info.getQuestPhase() != QuestPhase.SCHEDULED)
            return false;
        info.setQuestPhase(QuestPhase.NONE, 0);
        return true;
    }

    public int changeScheduledTime(Player player, int time)
    {
        GamePlayer info = this.stateManager.getPlayer(player);
        if (!info.isPlaying())
            return -1;

        if (info.isQuesting())
            return -1;

        info.setQuestPhase(QuestPhase.SCHEDULED, time);
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

    private void questFailGeneral(GamePlayer gamePlayer)
    {
        Player player = gamePlayer.getPlayer();

        if (gamePlayer.getToilet() != null)  // TODO: Refactor: playerLeftToilet
            game.getToiletManager().getLogic().playerLeftToilet(gamePlayer.getPlayer(), gamePlayer.getToilet());

        player.setKiller(null);
        player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, 0.11235));
        player.setHealth(0d);
        gamePlayer.setQuestPhase(QuestPhase.NONE, 0);

        gamePlayer.resetPlayerForQuest();
    }

    @SuppressWarnings("deprecation")
    public void onPlayerFailedQuest(GamePlayer gamePlayer)
    {
        this.questFailGeneral(gamePlayer);
        Bukkit.broadcastMessage(ChatColor.RED + gamePlayer.getPlayer().getName() + " は便意に耐えられず死んでしまった！");
    }

    @SuppressWarnings("deprecation")
    public void onBurst(GamePlayer player)
    {
        Player p = player.getPlayer();
        p.getWorld().createExplosion(p.getLocation(), 0F);


        this.questFailGeneral(player);
        Bukkit.broadcastMessage(ChatColor.RED + p.getName() + " は力を込めすぎて爆発してしまった！");
    }

    private int gain;
    private int loss;
    private DefecationType defecationType;

    public void init()
    {
        this.logic.init();
        this.runTaskTimer(ToiletPlugin.getPlugin(), 0, 10);
        this.updateConfig();
    }

    private void updateConfig()
    {
        gain = Math.toIntExact(Math.round((double) this.game.getConfig().getPowerGainAmount() / 2.0d));
        loss = Math.toIntExact(Math.round((double) this.game.getConfig().getPowerLossOnSecAmount() / 2.0d));

        defecationType = this.game.getConfig().getDefecationType();
    }

    @Override
    public void run()
    {
        boolean burst = this.game.getConfig().isBurstOnPowerOver100();

        this.game.getPlayerStateManager().getGamePlayers().forEach((player, gamePlayer) -> {
            gamePlayer.getDisplay().updateScreen();
            if (gamePlayer.getQuestPhase() != QuestPhase.TOILET_JOINED)
                return;

            if (gamePlayer.getNowPower() >= 100 && burst)
            {
                game.getQuestManager().onBurst(gamePlayer);
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

        if (++secCount != 2)
            return;

        this.stateManager.getPlayers().forEach(info -> {
            Player player = info.getPlayer();

            if (!info.isPlaying())
                return;

            if (info.getQuestPhase() == QuestPhase.NONE)
                return;

            if (info.getTime() == 0)
                if (info.isQuesting())
                    this.onPlayerFailedQuest(info);
                else
                    this.start(player);

            info.setTime(info.getTime() - 1);
        });

        updateConfig();
        secCount = 0;
    }
}
