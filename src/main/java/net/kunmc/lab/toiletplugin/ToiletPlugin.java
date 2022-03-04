package net.kunmc.lab.toiletplugin;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.toiletobject.generate.ModelManager;
import net.kunmc.lab.toiletplugin.toiletobject.generate.ToiletGenerator;
import net.kunmc.lab.toiletplugin.toiletobject.generate.ToolManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ToiletPlugin extends JavaPlugin
{
    private static Logger LOGGER;

    @Getter
    private static ToiletPlugin plugin;

    @Getter
    private final ModelManager modelManager;
    @Getter
    private final GameMain game;

    @Getter
    private Scoreboard pluginScoreboard;

    public ToiletPlugin() throws IOException
    {
        LOGGER = getLogger();

        modelManager = new ModelManager();

        game = new GameMain(this);

        plugin = this;
    }


    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable()
    {
        LOGGER.info("ToiletPlugin has enabled!");

        copyFilesFromJar();

        modelManager.scan(new File(getDataFolder(), "assets/toilet_models"));

        Bukkit.getWorlds().stream().parallel()
                .forEach(world -> modelManager.scan(new File(world.getWorldFolder(), "generated")));

        Bukkit.getPluginManager().registerEvents(new ToolManager(), this);
        Bukkit.getPluginManager().registerEvents(new ToiletGenerator(this.game.getToiletManager()), this);

        pluginScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        game.setup();

        getCommand("toilet").setExecutor(new CommandHandler(this.game));
        getCommand("toilet").setTabCompleter(new CommandHandler(this.game));
    }

    public static void copyFilesFromJar()
    {
        File dataDir = plugin.getDataFolder();

        CodeSource codeSource = ToiletPlugin.class.getProtectionDomain().getCodeSource();
        if (codeSource == null)
        {
            LOGGER.info("Failed to get jar file.");
            return;
        }

        URL jar = codeSource.getLocation();
        try(ZipFile zip = new ZipFile(jar.getPath()))
        {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (true)
            {
                if (!entries.hasMoreElements())
                    break;

                ZipEntry entry = entries.nextElement();
                if (entry == null)
                    break;

                String name = entry.getName();
                if (name.startsWith("assets") && !entry.isDirectory())
                {
                    File file = new File(dataDir, name);

                    if (!file.exists() || !checkDigest(zip.getInputStream(entry), new FileInputStream(file)))
                        actualCopy(zip.getInputStream(entry), file);
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LOGGER.info("Failed to copy assets from jar.");
        }
    }

    private static boolean checkDigest(InputStream one, InputStream two) throws IOException
    {
        return Arrays.equals(DigestUtils.sha1(one), DigestUtils.sha1(two));
    }

    private static void actualCopy(InputStream inputStream,  File file) throws IOException
    {
        LOGGER.info("Copying " + file.getName());
        if (file.exists())
            file.delete();
        file.getParentFile().mkdirs();
        try(FileOutputStream out = new FileOutputStream(file))
        {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0)
                out.write(buffer, 0, len);
        }
    }

    @Override
    public void onDisable()
    {
        try
        {
            this.game.getToiletManager().save();
            this.game.saveConfig();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        LOGGER.info("Unregistering scoreboard/team/objectives...");

        pluginScoreboard.getTeams().stream()
                .filter(team -> team.getName().startsWith("toilet_"))
                .forEach(Team::unregister);

        pluginScoreboard.getObjectives().stream()
                .filter(objective -> objective.getName().startsWith("toilet_"))
                .filter(objective -> objective.getCriteria().equalsIgnoreCase("dummy"))
                .forEach(Objective::unregister);

        Bukkit.getOnlinePlayers()
                .forEach(player -> player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()));

        LOGGER.info("ToiletPlugin has disabled!");
    }
}
