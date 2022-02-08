package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public class PlayerHUD
{
    @Getter
    private final GamePlayer player;
    @Getter
    private final GameMain gameMain;
    @Getter
    private final PlayerStateManager playerStateManager;
    @Getter
    private final QuestManager questManager;

    private final BossBar bossBar;

    public PlayerHUD(GamePlayer player, GameMain gameMain)
    {
        this.player = player;
        this.gameMain = gameMain;
        this.playerStateManager = gameMain.getPlayerStateManager();
        this.questManager = gameMain.getQuestManager();

        this.bossBar = Bukkit.createBossBar("残り時間", BarColor.GREEN, BarStyle.SOLID);
        this.bossBar.addPlayer(player.getPlayer());
        this.bossBar.setVisible(false);
        this.bossBar.setProgress(1.0);
    }

    public void updateScreen()
    {
        if (this.player.isQuesting())
            this.updateQuestBossBar();
    }

    private void updateQuestBossBar()
    {

    }
}
