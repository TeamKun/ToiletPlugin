package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerStateManager
{
    @Getter
    private final List<Player> players;
    @Getter
    private final List<Player> spectators;

    private final GameMain game;

    public PlayerStateManager(GameMain game)
    {
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.game = game;
    }

    public boolean isPlaying(Player player)
    {
        return players.contains(player);
    }

    public boolean isSpectating(Player player)
    {
        return spectators.contains(player);
    }

    public void updatePlayer(Player player)
    {
        this.updatePlayer(player, player.getGameMode());
    }

    public void updatePlayer(Player player, GameMode mode)
    {
        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE)
        {
            this.removeSpectator(player);
            if (!players.contains(player))
                this.addPlayer(player);
        }
        else
        {
            this.removePlayer(player);
            if (!spectators.contains(player))
                this.addSpectator(player);
        }
    }

    public void addPlayer(Player player)
    {
        this.removeSpectator(player);
        players.add(player);
        player.sendMessage(ChatColor.GREEN + "ゲームに参加しました！");
    }

    public void addSpectator(Player player)
    {
        this.removePlayer(player);
        spectators.add(player);
        player.sendMessage(ChatColor.GOLD + "スペクテイターになりました！");
    }

    public void removePlayer(Player player)
    {
        this.game.getQuestManager().cancel(player, true);
        boolean removed = players.remove(player);

        if (!removed)
            return;
        player.sendMessage(ChatColor.RED + "ゲームから退出しました！");
    }

    public void removeSpectator(Player player)
    {
        boolean removed = spectators.remove(player);

        if (!removed)
            return;
        player.sendMessage(ChatColor.RED + "スペクテイターではなくなりました！");
    }
}
