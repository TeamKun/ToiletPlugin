package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.game.quest.QuestProgress;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GamePlayer
{
    @Getter
    private final Player player;

    @Getter
    @Setter
    private PlayState state;

    @Getter
    private int maxTimeLimit;
    @Getter
    @Setter
    private int time;
    @Getter
    private QuestProgress questProgress;

    public GamePlayer(Player player)
    {
        this.player = player;
        this.questProgress = QuestProgress.NONE;
        this.maxTimeLimit = -1;
        this.time = -1;
    }

    public void setMaxTimeLimit(int maxTimeLimit)
    {
        this.maxTimeLimit = maxTimeLimit;
        this.time = maxTimeLimit;
    }

    public void setQuestProgress(QuestProgress state, int time)
    {
        this.questProgress = state;
        setMaxTimeLimit(time);
    }

    public boolean isQuesting()
    {
        return this.questProgress != QuestProgress.NONE &&
                this.questProgress != QuestProgress.SCHEDULED;
    }

    public boolean isScheduled()
    {
        return this.questProgress != QuestProgress.SCHEDULED;
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
        this.questProgress = QuestProgress.NONE;
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

                this.questProgress = QuestProgress.NONE;
                break;
        }

        this.state = state;
    }
}
