package net.kunmc.lab.toiletplugin.game.quest;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
}
