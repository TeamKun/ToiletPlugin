package net.kunmc.lab.toiletplugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.security.CodeSource;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ToiletPlugin extends JavaPlugin
{
    private static Logger LOGGER;

    @Getter
    private static ToiletPlugin plugin;

    public ToiletPlugin()
    {
        plugin = this;
        LOGGER = getLogger();
    }



    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable()
    {
        LOGGER.info("ToiletPlugin has enabled!");

        getCommand("toilet").setExecutor(new CommandHandler());
        getCommand("toilet").setTabCompleter(new CommandHandler());

        copyFilesFromJar();
    }

    private static void copyFilesFromJar()
    {
        File dataDir = plugin.getDataFolder();

        CodeSource codeSource = ToiletPlugin.class.getProtectionDomain().getCodeSource();
        if (codeSource == null)
        {
            LOGGER.info("Failed to get jar file.");
            return;
        }

        URL jar = codeSource.getLocation();
        try(ZipInputStream zip = new ZipInputStream(jar.openStream()))
        {
            while (true)
            {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null)
                    break;

                String name = entry.getName();
                if (name.startsWith("assets") && !entry.isDirectory())
                {
                    actualCopy(zip, new File(dataDir, name));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LOGGER.info("Failed to copy assets from jar.");
        }
    }

    private static void actualCopy(InputStream inputStream,  File file) throws IOException
    {
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
