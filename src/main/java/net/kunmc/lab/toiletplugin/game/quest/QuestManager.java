package net.kunmc.lab.toiletplugin.game.quest;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.player.PlayerManager;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.utils.Utils;
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

    private int accept;
    private int maxCount;

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

    private int cooldown;

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
        Bukkit.broadcastMessage(ChatColor.RED + gamePlayer.getPlayer().getName() +
                Utils.convertExplict(gamePlayer.getGame(), " は便意に耐えられず死んでしまった！", " はうんこを漏らしてしまった！"));
    }

    @SuppressWarnings("deprecation")
    public void onBurst(GamePlayer player)
    {
        Player p = player.getPlayer();
        p.getWorld().createExplosion(p.getLocation(), 0F);


        this.questFailGeneral(player);
        Bukkit.broadcastMessage(ChatColor.RED + p.getName() + " は力を込めすぎて爆発してしまった！");
    }

    @Getter
    private int gain;
    private int loss;

    public int start(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isPlaying())
            return -1;

        if (info.isQuesting())
            return -1;

        info.resetPlayerForQuest();

        int questTime = this.game.getConfig().generateQuestTime();
        int maxPoop = this.game.getConfig().generatePoopAmount();

        info.playSound(GameSound.QUEST_START);
        info.setQuestPhase(QuestPhase.STARTED, questTime);
        info.setMaxPoop(maxPoop);

        player.sendMessage(ChatColor.DARK_RED + "あなたは便意を感じている... ");
        player.sendMessage(ChatColor.RED + "あなたは" + questTime + "秒以内に" +
                Utils.convertExplict(this.game, "うんこ", "排泄") + "をしないと死んでしまう！");


        return questTime;
    }

    public int cancel(Player player, boolean isNever)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isQuesting())
            return -1;

        if (info.getQuestPhase() != QuestPhase.STARTED)
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

    public void onPlayerQuestFinish(GamePlayer player)
    {
        player.purge();

        if (this.game.getConfig().isAutoRescheduleOnSuccess())
            this.reSchedule(player.getPlayer());

        player.getDisplay().onQuestFinished();
    }

    @Getter
    private DefecationType defecationType;
    private boolean burst;

    public void init()
    {
        this.logic.init();
        this.runTaskTimer(ToiletPlugin.getPlugin(), 0, 10);
        this.updateConfig();
    }

    private void updateConfig()
    {
        GameConfig config = this.game.getConfig();

        gain = Math.toIntExact(Math.round((double) config.getPowerGainAmount() / 2.0d));
        loss = Math.toIntExact(Math.round((double) config.getPowerLossOnSecAmount() / 2.0d));
        defecationType = config.getDefecationType();
        burst = config.isEnablePowerBurst();
        accept = config.getDefecationNeedPower();
        maxCount = config.getPowerKeepCountSeconds();
        cooldown = config.generatePlayerCooldownTime();
    }

    private void pendingQuestTick(Player player, GamePlayer gamePlayer)
    {
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

    }

    private boolean acceptQuestTick(GamePlayer player)
    {
        int nowCount = player.getNowCount();

        if (nowCount < maxCount)
            player.setNowCount(nowCount + 1);
        else
        {
            player.doDefecation();
            player.setNowPoop(player.getNowPoop() + 1);
            player.setNowCount(0);
            player.setNowPower(player.getNowPower() - accept);
            if (player.getNowPoop() >= player.getMaxPoop())
            {
                player.getDisplay().clearPowerBossBar();
                player.setCooldown(cooldown);
                return true;
            }
        }
        return false;
    }

    @Override
    public void run()
    {
        this.game.getPlayerStateManager().getGamePlayers().forEach(this::pendingQuestTick);

        if (++secCount != 2)
            return;

        this.stateManager.getPlayers().forEach(info -> {
            Player player = info.getPlayer();

            if (!info.isPlaying())
                return;

            if (info.getQuestPhase() == QuestPhase.NONE ||
                    info.getQuestPhase() == QuestPhase.SCHEDULED)
                return;

            if (info.getQuestPhase() != QuestPhase.PLAYER_COOLDOWN)
            {
                if (info.getNowPower() >= accept)
                {
                    if (acceptQuestTick(info))
                        return;
                }
                else
                    info.setNowCount(0);
            }

            if (info.getTime() == 0)
            {
                if (info.getQuestPhase() == QuestPhase.PLAYER_COOLDOWN)
                {
                    onPlayerQuestFinish(info);
                    return;
                }

                if (info.isQuesting())
                    this.onPlayerFailedQuest(info);
                else
                    this.start(player);
            }

            info.setTime(info.getTime() - 1);
        });

        updateConfig();
        secCount = 0;
    }
}
