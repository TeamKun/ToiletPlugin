package net.kunmc.lab.toiletplugin.game;

import com.google.gson.Gson;
import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.player.PlayerStateManager;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletManager;
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

    private final ToiletPlugin plugin;

    @Getter
    private final ToiletManager toiletManager;
    @Getter
    private final QuestManager questManager;
    @Getter
    private final PlayerStateManager playerStateManager;

    private int tickCount;

    public GameMain(ToiletPlugin plugin)
    {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config_game.json");
        this.config = loadConfig(configFile);

        this.playerStateManager = new PlayerStateManager(this);
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

        plugin.getServer().getOnlinePlayers().stream().parallel()
                .forEach(this.playerStateManager::updatePlayer);

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
