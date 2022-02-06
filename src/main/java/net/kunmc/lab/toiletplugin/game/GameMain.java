package net.kunmc.lab.toiletplugin.game;

import com.google.gson.Gson;
import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GameMain extends BukkitRunnable
{
    @Getter
    private final List<Player> players;
    @Getter
    private final List<Player> spectators;

    @Getter
    private final GameConfig gameConfig;
    @Getter
    private final File configFile;

    private final ToiletPlugin plugin;

    @Getter
    private final ToiletManager toiletManager;
    @Getter
    private final QuestManager questManager;

    private int tickCount;

    public GameMain(ToiletPlugin plugin)
    {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.configFile = new File(plugin.getDataFolder(), "config_game.json");
        this.gameConfig = loadConfig(configFile);

        this.questManager = new QuestManager(this);
        this.toiletManager = new ToiletManager(this, new File(plugin.getDataFolder(), "toilets.json"));
    }

    private static GameConfig loadConfig(File file)
    {
        if (!file.exists())
            return new GameConfig();

        Gson gson = new Gson();

        try
        {
            return gson.fromJson(new String(Files.readAllBytes(file.toPath())), GameConfig.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return new GameConfig();
    }

    public void saveConfig()
    {
        Gson gson = new Gson();
        try
        {
            Files.write(configFile.toPath(), gson.toJson(gameConfig).getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setup()
    {
        Bukkit.getPluginManager().registerEvents(new GameEventListener(this), this.plugin);
        this.toiletManager.init();
        this.questManager.init();

        plugin.getServer().getOnlinePlayers().stream().parallel()
                .forEach(this::updatePlayer);

        this.runTaskTimer(plugin, 0L, 1L);
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
        this.questManager.cancel(player, true);
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

    @Override
    public void run()
    {
        tickCount++;

        if (tickCount % 20 == 0)
        {
            toiletManager.getLogic().onSecond();
            tickCount = 0;
        }
        else if (tickCount % 2 == 0)
            toiletManager.getLogic().onTwoTick(2);
    }
}
