package net.kunmc.lab.toiletplugin.game.quest;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;

public class QuestManager
{
    @Getter
    private final HashMap<Player, Integer> questingPlayer;
    private final HashMap<Player, Integer> waitingPlayer;

    private final GameMain game;

    public QuestManager(GameMain game)
    {
        this.questingPlayer = new HashMap<>();
        this.waitingPlayer = new HashMap<>();
        this.game = game;
    }

    public int start(Player player)
    {
        if (!this.game.getPlayers().contains(player))
            return -1;

        if (this.questingPlayer.containsKey(player))
            return -1;

        this.waitingPlayer.remove(player);
        this.questingPlayer.put(player, 30);


        return 30;
    }

    public boolean cancel(Player player, boolean isNever)
    {
        if (!this.questingPlayer.containsKey(player))
            return false;

        this.questingPlayer.remove(player);
        player.sendMessage(ChatColor.GREEN + "あなたのクエストがキャンセルされました。");
        if (!isNever)
            this.changeWaitingTime(player);
        return true;
    }

    public boolean isQuesting(Player player)
    {
        return this.questingPlayer.containsKey(player);
    }

    public int changeWaitingTime(Player player, int time)
    {
        if (!this.game.getPlayers().contains(player))
            return -1;

        if (this.questingPlayer.containsKey(player))
            return -1;

        this.waitingPlayer.put(player, time);
        return time;
    }

    public int changeWaitingTime(Player player)
    {
        return this.changeWaitingTime(player, new Random().nextInt(170) + 10);
    }

    public Integer getWaitingTime(Player player)
    {
        return this.waitingPlayer.get(player);
    }

}
