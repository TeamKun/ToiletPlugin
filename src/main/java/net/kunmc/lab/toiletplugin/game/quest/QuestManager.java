package net.kunmc.lab.toiletplugin.game.quest;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class QuestManager
{
    @Getter
    private final List<Player> questingPlayer;
    private final HashMap<Player, Integer> waitingPlayer;

    private final GameMain game;

    public QuestManager(GameMain game)
    {
        this.questingPlayer = new ArrayList<>();
        this.waitingPlayer = new HashMap<>();
        this.game = game;
    }

    public boolean start(Player player)
    {
        if (!this.game.getPlayers().contains(player))
            return false;

        if (this.questingPlayer.contains(player))
            return false;

        this.waitingPlayer.remove(player);
        this.questingPlayer.add(player);


        return true;
    }

    public boolean cancel(Player player, boolean isNever)
    {
        if (!this.questingPlayer.contains(player))
            return false;

        this.questingPlayer.remove(player);
        player.sendMessage(ChatColor.GREEN + "あなたのクエストがキャンセルされました。");
        if (!isNever)
            this.changeWaitingTime(player);
        return true;
    }

    public boolean isQuesting(Player player)
    {
        return this.questingPlayer.contains(player);
    }

    public boolean changeWaitingTime(Player player, int time)
    {
        if (!this.game.getPlayers().contains(player))
            return false;

        if (this.questingPlayer.contains(player))
            return false;

        this.waitingPlayer.put(player, time);
        return true;
    }

    public boolean changeWaitingTime(Player player)
    {
        return this.changeWaitingTime(player, new Random().nextInt(170) + 10);
    }

    public Integer getWaitingTime(Player player)
    {
        return this.waitingPlayer.get(player);
    }

}
