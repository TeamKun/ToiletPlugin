package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    @Getter
    @Setter
    private OnGroundToilet toilet;

    public GamePlayer(Player player, GameMain game)
    {
        this.player = player;
        this.display = new PlayerHUD(this, game);
        this.questPhase = QuestPhase.NONE;
        this.maxTimeLimit = -1;
        this.time = -1;
        this.toilet = null;
    }

    public void setMaxTimeLimit(int maxTimeLimit)
    {
        this.maxTimeLimit = maxTimeLimit;
        this.time = maxTimeLimit;
    }

    public void setQuestPhase(QuestPhase state, int time)
    {
        setMaxTimeLimit(time);
        setQuestPhase(state);
    }

    public void setQuestPhase(QuestPhase state)
    {
        this.questPhase = state;

        if (this.questPhase == QuestPhase.NONE || this.questPhase == QuestPhase.SCHEDULED)
        {
            player.stopSound(GameSound.QUESTING_OPPRESSIVE.getName());
            return;
        }

        if (state == QuestPhase.STARTED)
            display.questStarted();
        else
        {
            display.showQuestTitle(state);
            if (state.isSubQuest())
                this.playSound(GameSound.QUEST_PHASE_COMPLETE);
        }
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

    public void playSound(GameSound sound)
    {
        sound.play(this.player);
    }

    public void playSound(GameSound sound, Location location)
    {
        sound.play(this.player, location);
    }
}
