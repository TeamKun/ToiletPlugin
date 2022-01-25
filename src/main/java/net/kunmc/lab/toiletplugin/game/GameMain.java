package net.kunmc.lab.toiletplugin.game;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameMain
{
    @Getter
    private final List<Player> players;
    @Getter
    private final List<Player> spectators;

    @Getter
    private final GameConfig gameConfig;

    private final ToiletPlugin plugin;

    public GameMain(ToiletPlugin plugin)
    {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.gameConfig = new GameConfig();
    }

    public void setup()
    {
        Bukkit.getPluginManager().registerEvents(new GameEventListener(this), this.plugin);

        plugin.getServer().getOnlinePlayers().stream().parallel()
                .forEach(this::updatePlayer);
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
