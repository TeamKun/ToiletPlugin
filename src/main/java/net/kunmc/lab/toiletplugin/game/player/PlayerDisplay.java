package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.time.Duration;
import java.util.Optional;

public class PlayerDisplay
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
    private final BossBar powerBossBar;

    private ArmorStand hud;
    private ArmorStand hudBar;

    private boolean questRun;

    public PlayerDisplay(GamePlayer player, GameMain gameMain)
    {
        this.player = player;
        this.gameMain = gameMain;
        this.playerStateManager = gameMain.getPlayerStateManager();
        this.questManager = gameMain.getQuestManager();

        this.timeBossBar = this.createBossBar("残り時間");
        this.powerBossBar = this.createBossBar("パワー");

        this.questRun = false;
    }

    private void initHud(ArmorStand stand)
    {
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setCustomName("");
    }

    private ArmorStand getHud(Entity player)
    {
        Optional<Entity> passenger = player.getPassengers().stream().findFirst();
        if (passenger.isPresent())
            if (passenger.get() instanceof ArmorStand)
                return (ArmorStand) passenger.get();

        ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        player.addPassenger(stand);

        return stand;
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


        this.hud = getHud(player.getPlayer());
        this.hudBar = getHud(this.hud);

        initHud(this.hud);
        initHud(this.hudBar);
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

        titleStr = Utils.replaceExplict(gameMain, titleStr, "排便", "うんこ");

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
                this.clearHud();
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
            this.powerBossBar.setVisible(true);
            this.updatePowerBossBar();
        }

        if (this.player.getQuestPhase() != QuestPhase.PLAYER_COOLDOWN)
            this.updateHud();
        else
            this.clearHud();

        this.updateGeneralTitle();
    }

    public void clearHud()
    {
        this.hud.remove();
        this.hudBar.remove();
    }

    public void updateHud()
    {
        if (this.player.getTime() < 0)
        {
            this.clearHud();
            return;
        }

        this.hud.setCustomName(getTimeString(this.player.getTime(), this.player.getMaxTimeLimit()));
        this.hud.setCustomNameVisible(true);

        double progress = (double) this.player.getTime() / this.player.getMaxTimeLimit();

        StringBuilder progressBar = new StringBuilder("[");

        // Right to Left Progress Bar

        for (int i = 0; i < 10; i++)
        {
            if (i > progress * 10)
                progressBar.append(ChatColor.RED).append("░");
            else
                progressBar.append(ChatColor.GREEN).append("█");
        }

        progressBar.append(ChatColor.WHITE).append("]");

        hudBar.setCustomName(progressBar.toString());
        hudBar.setCustomNameVisible(true);
    }

    public void updateGeneralTitle()
    {
        int time = this.player.getTime();
        int max = this.player.getMaxTimeLimit();

        if (time < 0 || max < 0)
            return;

        ChatColor color = ChatColor.GREEN;
        boolean flag = true;
        if (time < 5)
            color = ChatColor.RED;
        else if (time % 10 == 0)
        {
            double progress = (double) time / max;
            if (progress < 0.15)
                color = ChatColor.DARK_RED;
            else if (progress < 0.3)
                color = ChatColor.RED;
            else if (progress < 0.6)
                color = ChatColor.YELLOW;
        }
        else
            flag = false;

        if (flag)
            this.player.getPlayer().showTitle(Title.title(
                    Component.text(ChatColor.YELLOW + "残り " + color + time + ChatColor.YELLOW + " 秒"),
                    Component.text(""),
                    Title.Times.of(
                            Duration.ofMillis(500),
                            Duration.ofSeconds(1),
                            Duration.ofMillis(500)
                    )
            ));
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
        int min = config.getDefecationNeedPower();
        int now = this.player.getNowPower();

        double progress = (double) now / max;

        String titlePrefix = "パワー";

        this.powerBossBar.setProgress(Math.min(progress, 1.0));

        if (now > 90)
        {
            this.powerBossBar.setColor(BarColor.GREEN);
            titlePrefix = ChatColor.DARK_RED + "⚠⚠⚠" + titlePrefix;
        }
        if (now >= min)
        {
            this.powerBossBar.setColor(BarColor.GREEN);
            titlePrefix = ChatColor.GREEN + titlePrefix;
        }
        else if (now >= min / 2)
        {
            this.powerBossBar.setColor(BarColor.YELLOW);
            titlePrefix = ChatColor.YELLOW + titlePrefix;
        }
        else
        {
            this.powerBossBar.setColor(BarColor.RED);
            titlePrefix = ChatColor.RED + titlePrefix;
        }

        this.powerBossBar.setTitle(titlePrefix + ": " + now + ChatColor.DARK_GREEN + "/" + max);
    }

    public void clearBossBar()
    {
        this.timeBossBar.setVisible(false);
        this.timeBossBar.setProgress(1.0);

        this.powerBossBar.setVisible(false);
        this.powerBossBar.setProgress(1.0);
    }

    private void updateTimeBossBar()
    {
        int max = this.player.getMaxTimeLimit();
        int time = this.player.getTime();

        if (max == 0 || time == 0)
        {
            this.timeBossBar.setTitle(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "残り時間: " +
                    ChatColor.WHITE + ChatColor.BOLD + "0 秒");
            return;
        }

        double progress = (double) time / (double) max;

        this.timeBossBar.setProgress(progress);

        this.timeBossBar.setTitle(getTimeString(time, max));
        this.timeBossBar.setColor(getBossBarColor(progress));
    }

    private BarColor getBossBarColor(double progress)
    {
        if (progress < 0.3)
            return BarColor.RED;
        else if (progress < 0.6)
            return BarColor.YELLOW;
        else
            return BarColor.GREEN;
    }

    private String getTimeString(int time, int max)
    {
        boolean whiteFlag = time % 2 == 0;

        if (max == 0 || time == 0)
            return ChatColor.DARK_RED + ChatColor.BOLD.toString() + "残り時間: " + ChatColor.WHITE + ChatColor.BOLD + "0 秒";

        double progress = (double) time / (double) max;

        if (whiteFlag)
            return ChatColor.WHITE + ChatColor.BOLD.toString() + "残り時間: " + time + " 秒";
        else if (progress < 0.15)
            return ChatColor.DARK_RED + ChatColor.BOLD.toString() + "残り時間: " + ChatColor.WHITE + ChatColor.BOLD + time + " 秒";
        else if (progress < 0.3)
            return ChatColor.RED + ChatColor.BOLD.toString() + "残り時間: " + ChatColor.WHITE + ChatColor.BOLD + time + " 秒";
        else if (progress < 0.6)
            return ChatColor.YELLOW + ChatColor.BOLD.toString() + "残り時間: " + ChatColor.WHITE + ChatColor.BOLD + time + " 秒";
        else
            return ChatColor.GREEN + ChatColor.BOLD.toString() + "残り時間: " + ChatColor.WHITE + ChatColor.BOLD + time + " 秒";
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
        this.powerBossBar.setVisible(false);
    }

    public void onDeath()
    {
        this.clearHud();
    }
}
