package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
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
    private static final int TITLE_SHOWING_TIME = 4000;

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
        this.showQuestTitle(quest, "");
    }

    public void showQuestTitle(QuestPhase quest, String customStateMessage)
    {
        String titleStr = quest.getTitle() == null ? "": ChatColor.GREEN + "クエスト発生：" + quest.getTitle();
        if (quest.getTitle() != null && quest.isEmergency())
            titleStr = ChatColor.DARK_RED + "緊急クエスト発生: " + ChatColor.RED + titleStr;

        Title title = Title.title(
                Component.text(titleStr),
                Component.text(quest.getSubTitle() == null ? "": ChatColor.YELLOW + quest.getSubTitle()
                        .replace("%s", customStateMessage)),
                Title.Times.of(
                        Duration.ofMillis((long) (TITLE_SHOWING_TIME * 0.125)),
                        Duration.ofMillis((long) (TITLE_SHOWING_TIME * 0.75)),
                        Duration.ofMillis((long) (TITLE_SHOWING_TIME * 0.125))
                )
        );

        player.getPlayer().showTitle(title);
    }

    public void updateScreen()
    {
        if (!this.player.isQuesting())
        {
            if (this.questRun)
            {
                this.questRun = false;
                this.clearBossBar();
            }
            return;
        }

        this.questRun = true;

        if (this.player.getQuestPhase() == QuestPhase.TOILET_JOINED)
        {
            this.updateActionBar(this.gameMain.getConfig().getDefecationType().getMessage());
            this.updateDefecationTitle();
        }
        else
            this.updateActionBar("");

        this.updateTimeBossBar();

        if (this.player.getQuestPhase() == QuestPhase.TOILET_JOINED)
        {
            this.powerBossbar.setVisible(true);
            this.updatePowerBossBar();
        }

    }


    private void updateDefecationTitle()
    {
        if (this.player.getToiletJoinedIn() == -1)
            return;
        long diff = System.currentTimeMillis() - player.getToiletJoinedIn();
        if (diff < TITLE_SHOWING_TIME)
            return;

        if (this.player.getQuestPhase() != QuestPhase.TOILET_JOINED)
            return;

        String countTitle = String.valueOf(
                (char) (0x2460 + (this.gameMain.getConfig().getPowerKeepCountSeconds() - this.player.getNowCount()))
        );
        double count = this.player.getNowCount() / (double) this.gameMain.getConfig().getPowerKeepCountSeconds();

        if (count != 0.0)
            if (count < 0.5)
                countTitle = ChatColor.RED + countTitle;
            else if (count < 0.8)
                countTitle = ChatColor.YELLOW + countTitle;
            else
                countTitle = ChatColor.GREEN + countTitle;
        else
            countTitle = "";

        ChatColor poopTitleColor;

        double poop = this.player.getNowPoop() / (double) this.player.getMaxPoop();

        if (poop < 0.5)
            poopTitleColor = ChatColor.RED;
        else
            poopTitleColor = ChatColor.YELLOW;

        String poopTitle = ChatColor.YELLOW + "(" + poopTitleColor +
                this.player.getNowPoop() + ChatColor.YELLOW + "/" + this.player.getMaxPoop() + ")";

        this.player.getPlayer().showTitle(Title.title(
                Component.text(countTitle),
                Component.text(poopTitle),
                Title.Times.of(
                        Duration.ofMillis(0),
                        Duration.ofSeconds(1),
                        Duration.ofMillis(0)
                )
        ));
    }

    private void updatePowerBossBar()
    {
        GameConfig config = this.getGameMain().getConfig();
        int max = 100;
        int min = config.getMinDefecationAcceptPower();
        int now = this.player.getNowPower();

        double progress = (double) now / max;

        String titlePrefix = "パワー";

        this.powerBossbar.setProgress(Math.min(progress, 1.0));

        if (now > 90)
        {
            this.powerBossbar.setColor(BarColor.GREEN);
            titlePrefix = ChatColor.DARK_RED + "⚠⚠⚠" + titlePrefix;
        }
        if (now >= min)
        {
            this.powerBossbar.setColor(BarColor.GREEN);
            titlePrefix = ChatColor.GREEN + titlePrefix;
        }
        else if (now >= min / 2)
        {
            this.powerBossbar.setColor(BarColor.YELLOW);
            titlePrefix = ChatColor.YELLOW + titlePrefix;
        }
        else
        {
            this.powerBossbar.setColor(BarColor.RED);
            titlePrefix = ChatColor.RED + titlePrefix;
        }

        this.powerBossbar.setTitle(titlePrefix + ": " + now + ChatColor.DARK_GREEN + "/" + max);
    }

    public void clearBossBar()
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

    private void updateActionBar(String customQuestMessage)
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
                actionBar.append(ChatColor.WHITE).append(ChatColor.BOLD).append(" - ")
                        .append(quest.getSubTitle().replace("%s", customQuestMessage));
            else
                actionBar.append(ChatColor.YELLOW).append(ChatColor.BOLD).append(" - ")
                        .append(quest.getSubTitle().replace("%s", customQuestMessage));

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

    public void onQuestFinished()
    {
        this.questRun = false;
        this.clearBossBar();

        GameSound.QUEST_COMPLETE.play(this.player);

        this.player.getPlayer().sendTitle(
                ChatColor.GREEN + "クエスト成功！",
                "",
                0, 20, 0
        );
    }

    public void clearPowerBossBar()
    {
        this.powerBossbar.removeAll();
    }
}
