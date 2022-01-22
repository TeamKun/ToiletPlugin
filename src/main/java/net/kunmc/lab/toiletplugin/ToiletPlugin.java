package net.kunmc.lab.toiletplugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

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
    public void onEnable()
    {
        LOGGER.info("ToiletPlugin has enabled!");
    }

    @Override
    public void onDisable()
    {
        LOGGER.info("ToiletPlugin has enabled!");
    }
}
