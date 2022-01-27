package net.kunmc.lab.toiletplugin.game;

import com.google.gson.Gson;
import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletLogic;
import net.kunmc.lab.toiletplugin.toiletobject.ToiletRegister;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    @Getter
    private final File configFile;

    @Getter
    private final ToiletRegister register;

    private final ToiletPlugin plugin;

    private final ToiletLogic logic;
    @Getter
    private final QuestManager questManager;

    public GameMain(ToiletPlugin plugin)
    {
        this.plugin = plugin;
        this.register = plugin.getToilets();
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.configFile = new File(plugin.getDataFolder(), "config_game.json");
        this.gameConfig = loadConfig(configFile);

        this.questManager = new QuestManager(this);
        this.logic = new ToiletLogic(this);
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
        Bukkit.getPluginManager().registerEvents(this.logic, plugin);
        this.logic.runTaskTimer(plugin, 0, 2);

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
