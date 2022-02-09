package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GamePlayer
{
    @Getter
    private final Player player;
    @Getter
    private final PlayerHUD display;

    @Getter
    @Setter
    private PlayState state;

    @Getter
    private int maxTimeLimit;
    @Getter
    @Setter
    private int time;
    @Getter
    private QuestPhase questPhase;

    public GamePlayer(Player player, GameMain game)
    {
        this.player = player;
        this.display = new PlayerHUD(this, game);
        this.questPhase = QuestPhase.NONE;
        this.maxTimeLimit = -1;
        this.time = -1;
    }

    public void setMaxTimeLimit(int maxTimeLimit)
    {
        this.maxTimeLimit = maxTimeLimit;
        this.time = maxTimeLimit;
    }

    public void setQuestPhase(QuestPhase state, int time)
    {
        this.questPhase = state;
        setMaxTimeLimit(time);
    }

    public boolean isQuesting()
    {
        return this.questPhase != QuestPhase.NONE &&
                this.questPhase != QuestPhase.SCHEDULED;
    }

    public boolean isScheduled()
    {
        return this.questPhase != QuestPhase.SCHEDULED;
    }

    public boolean isPlaying()
    {
        return this.state == PlayState.PLAYING;
    }

    public boolean isSpectating()
    {
        return this.state == PlayState.SPECTATING;
    }

    public void purge()
    {
        this.maxTimeLimit = 0;
        this.time = 0;
        this.questPhase = QuestPhase.NONE;
    }

    // Logic

    public void setPlayState(PlayState state)
    {
        switch (state)
        {
            case PLAYING:
                this.player.sendMessage(ChatColor.GREEN + "ゲームに参加しました！");
                break;
            case SPECTATING:
                if (this.state == PlayState.PLAYING)
                    this.player.sendMessage(ChatColor.RED + "ゲームから退出しました！");
                this.player.sendMessage(ChatColor.GREEN + "観戦モードになりました！");

                this.questPhase = QuestPhase.NONE;
                break;
        }

        this.state = state;
    }
}
