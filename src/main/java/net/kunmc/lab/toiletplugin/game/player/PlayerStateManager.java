package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerStateManager implements Listener
{
    @Getter
    private final List<Player> players;
    @Getter
    private final List<Player> spectators;
    @Getter
    private final HashMap<Player, Integer> questingPlayer;
    @Getter
    private final HashMap<Player, Integer> questScheduledPlayer;

    private final GameMain game;

    public PlayerStateManager(GameMain game)
    {
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.questingPlayer = new HashMap<>();
        this.questScheduledPlayer = new HashMap<>();

        this.game = game;
    }

    public void init()
    {
        Bukkit.getPluginManager().registerEvents(this, ToiletPlugin.getPlugin());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        this.updatePlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        this.updatePlayer(e.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e)
    {
        this.updatePlayer(e.getPlayer(), e.getNewGameMode());
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
        this.getQuestScheduledPlayer().put(player, 30);
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
        this.questScheduledPlayer.remove(player);
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
