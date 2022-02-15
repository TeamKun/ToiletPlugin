package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.time.Duration;

public class PlayerHUD
{
    @Getter
    private final GamePlayer player;
    @Getter
    private final GameMain gameMain;
    @Getter
    private final PlayerManager playerStateManager;
    @Getter
    private final QuestManager questManager;

    private final BossBar timeBossBar;
    private final BossBar powerBossbar;

    private boolean questRun;

    public PlayerHUD(GamePlayer player, GameMain gameMain)
    {
        this.player = player;
        this.gameMain = gameMain;
        this.playerStateManager = gameMain.getPlayerStateManager();
        this.questManager = gameMain.getQuestManager();

        this.timeBossBar = this.createBossBar("残り時間");
        this.powerBossbar = this.createBossBar("パワー");

        this.questRun = false;
    }

    private BossBar createBossBar(String title)
    {
        BossBar bar = Bukkit.createBossBar(title, BarColor.GREEN, BarStyle.SOLID);
        bar.addPlayer(this.player.getPlayer());
        bar.setVisible(false);
        bar.setProgress(1.0);
        return bar;
    }

    public void questStarted()
    {
        showQuestTitle(this.player.getQuestPhase());
        this.timeBossBar.setVisible(true);
    }

    public void showQuestTitle(QuestPhase quest)
    {
        String titleStr = quest.getTitle() == null ? "": ChatColor.GREEN + "クエスト発生：" + quest.getTitle();
        if (quest.getTitle() != null && quest.isEmergency())
            titleStr = ChatColor.DARK_RED + "緊急クエスト発生: " + ChatColor.RED + titleStr;

        Title title = Title.title(
                Component.text(titleStr),
                Component.text(quest.getSubTitle() == null ? "": ChatColor.YELLOW + quest.getSubTitle()),
                Title.Times.of(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(3),
                        Duration.ofMillis(500)
                )
        );

        player.getPlayer().showTitle(title);
    }

    public void updateScreen()
    {
        if (this.player.isQuesting())
        {
            this.updateActionBar();
            this.updateTimeBossBar();
            this.questRun = true;
            if (this.player.getQuestPhase() == QuestPhase.TOILET_JOINED)
            {
                this.powerBossbar.setVisible(true);
                this.updatePowerBossBar();
            }
        }
        else if (this.questRun)
        {
            this.questRun = false;
            this.clearBossBar();
        }

    }

    private void updatePowerBossBar()
    {
        GameConfig config = this.getGameMain().getConfig();
        int max = 100;
        int min = config.getMinDefecationAcceptPower();
        int now = this.player.getNowPower();

        double progress = (double) now / max;

        this.powerBossbar.setProgress(Math.min(progress, 1.0));
        if (now >= min)
        {
            this.powerBossbar.setColor(BarColor.GREEN);
            this.powerBossbar.setTitle(ChatColor.GREEN + "パワー");
        }
        else if (now >= min / 2)
        {
            this.powerBossbar.setColor(BarColor.YELLOW);
            this.powerBossbar.setTitle(ChatColor.YELLOW + "パワー");
        }
        else
        {
            this.powerBossbar.setColor(BarColor.RED);
            this.powerBossbar.setTitle(ChatColor.RED + "パワー");
        }

    }

    private void clearBossBar()
    {
        this.timeBossBar.setVisible(false);
        this.timeBossBar.setProgress(1.0);

        this.powerBossbar.setVisible(false);
        this.powerBossbar.setProgress(1.0);
    }

    private void updateTimeBossBar()
    {
        int max = this.player.getMaxTimeLimit();
        int time = this.player.getTime();

        boolean whiteFlag = time % 2 == 0;

        if (max == 0 || time == 0)
        {
            this.timeBossBar.setTitle(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "残り時間: " +
                    ChatColor.WHITE + ChatColor.BOLD + "0 秒");
            return;
        }

        double progress = (double) time / (double) max;

        this.timeBossBar.setProgress(progress);

        if (whiteFlag)
            this.timeBossBar.setTitle(ChatColor.WHITE + ChatColor.BOLD.toString() + "残り時間: " + time + " 秒");
        else if (progress < 0.15)
        {
            this.timeBossBar.setTitle(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "残り時間: " +
                    ChatColor.WHITE + ChatColor.BOLD + time + "秒");
        }
        else if (progress < 0.3)
        {
            this.timeBossBar.setTitle(ChatColor.RED + ChatColor.BOLD.toString() + "残り時間: " +
                    ChatColor.WHITE + ChatColor.BOLD + time + " 秒");
            this.timeBossBar.setColor(BarColor.RED);
        }
        else if (progress < 0.6)
        {
            this.timeBossBar.setTitle(ChatColor.YELLOW + ChatColor.BOLD.toString() + "残り時間: " +
                    ChatColor.WHITE + ChatColor.BOLD + time + " 秒");
            this.timeBossBar.setColor(BarColor.YELLOW);
        }
        else
        {
            this.timeBossBar.setTitle(ChatColor.GREEN + ChatColor.BOLD.toString() + "残り時間: " +
                    ChatColor.WHITE + ChatColor.BOLD + time + " 秒");
            this.timeBossBar.setColor(BarColor.GREEN);
        }
    }

    private void updateActionBar()
    {
        QuestPhase quest = this.getPlayer().getQuestPhase();

        boolean whiteFlag = this.player.getTime() % 2 == 0;

        if (quest.getSubTitle() == null && quest.getTitle() == null)
            return;

        StringBuilder actionBar = new StringBuilder();
        if (quest.isEmergency())
            actionBar.append(whiteFlag ? ChatColor.WHITE: ChatColor.DARK_RED).append(ChatColor.BOLD).append("緊急クエスト: ");
        else if (quest.isSubQuest())
            actionBar.append(whiteFlag ? ChatColor.WHITE: ChatColor.GOLD).append(ChatColor.BOLD).append("サブクエスト: ");
        else
            actionBar.append(whiteFlag ? ChatColor.WHITE: ChatColor.GREEN).append(ChatColor.BOLD).append("クエスト: ");

        if (quest.getTitle() != null)
            if (whiteFlag)
                actionBar.append(ChatColor.WHITE).append(ChatColor.BOLD).append(quest.getTitle());
            else
                actionBar.append(ChatColor.RED).append(ChatColor.BOLD).append(quest.getTitle());
        if (quest.getSubTitle() != null)
            if (whiteFlag)
                actionBar.append(ChatColor.WHITE).append(ChatColor.BOLD).append(" - ").append(quest.getSubTitle());
            else
                actionBar.append(ChatColor.YELLOW).append(ChatColor.BOLD).append(" - ").append(quest.getSubTitle());

        this.player.getPlayer().sendActionBar(Component.text(actionBar.toString()));
    }

    public void onQuestCancelled()
    {
        this.questRun = false;
        this.clearBossBar();

        this.player.getPlayer().sendActionBar(
                Component.text(ChatColor.GREEN + ChatColor.BOLD.toString() + "クエストがキャンセルされました"));

        this.player.getPlayer().sendTitle(
                ChatColor.GREEN + "クエストがキャンセルされました。",
                "",
                0, 20, 10
        );
    }
}
