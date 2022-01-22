package net.kunmc.lab.toiletplugin;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.toilet.generate.ModelManager;
import net.kunmc.lab.toiletplugin.toilet.generate.ToolManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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

    public ToiletPlugin()
    {
        plugin = this;
        LOGGER = getLogger();

        modelManager = new ModelManager();

    }



    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable()
    {
        LOGGER.info("ToiletPlugin has enabled!");

        getCommand("toilet").setExecutor(new CommandHandler());
        getCommand("toilet").setTabCompleter(new CommandHandler());

        copyFilesFromJar();

        modelManager.scan(new File(getDataFolder(), "assets/toilet_models"));

        Bukkit.getPluginManager().registerEvents(new ToolManager(), this);
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
        LOGGER.info("ToiletPlugin has enabled!");
    }
}
