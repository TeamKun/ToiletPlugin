package net.kunmc.lab.toiletplugin.game;

import com.google.gson.Gson;
import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import net.kunmc.lab.toiletplugin.game.player.PlayerManager;
import net.kunmc.lab.toiletplugin.game.player.ToiletMap;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.game.sound.DestinySoundPlayer;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GameMain extends BukkitRunnable
{
    @Getter
    private final GameConfig config;
    @Getter
    private final File configFile;

    @Getter
    private final ToiletPlugin plugin;

    @Getter
    private final ToiletManager toiletManager;
    @Getter
    private final QuestManager questManager;
    @Getter
    private final PlayerManager playerStateManager;
    @Getter
    private final DestinySoundPlayer destinySoundPlayer;

    @Getter
    private final ToiletMap toiletMap;

    private int tickCount;

    public GameMain(ToiletPlugin plugin)
    {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config_game.json");
        this.config = loadConfig(configFile);

        this.playerStateManager = new PlayerManager(this);
        this.questManager = new QuestManager(this);
        this.toiletManager = new ToiletManager(this, new File(plugin.getDataFolder(), "toilets.json"));
        this.destinySoundPlayer = new DestinySoundPlayer(plugin, this.playerStateManager);

        this.toiletMap = new ToiletMap(this);
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
            Files.write(configFile.toPath(), gson.toJson(config).getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setup()
    {
        this.playerStateManager.init();
        this.toiletManager.init();
        this.questManager.init();
        this.destinySoundPlayer.init();

        plugin.getServer().getOnlinePlayers()
                .forEach(player ->
                        Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, (Component) null)));

        this.runTaskTimer(plugin, 0L, 1L);
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
