package net.kunmc.lab.toiletplugin.toilet;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class ToiletRegister
{
    private static final int radius = 5;
    private final HashMap<String, Toilet> toilets;
    private final File file;

    public ToiletRegister(File file) throws IOException
    {
        this.file = file;
        if (!file.exists())
        {
            toilets = new HashMap<>();
            return;
        }

        Gson gson = new Gson();

        toilets = gson.fromJson(new String(Files.readAllBytes(file.toPath())), new TypeToken<HashMap<String, Toilet>>()
        {
        }.getType());

    }

    public static Toilet detect(ArmorStand stand)
    {
        Location armorStandLoc = stand.getLocation();
        Location ironDoorLoc = null;
        Location scytheLoc = null;

        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++)
                {
                    Location location = stand.getLocation().clone().add(x, y, z);
                    if (location.getBlock().getType() == Material.IRON_DOOR)
                        ironDoorLoc = location;
                    if (location.getBlock().getType() == Material.CAULDRON)
                        scytheLoc = location;
                    if (ironDoorLoc != null && scytheLoc != null)
                        return new Toilet(
                                new Toilet.LocationPojo(armorStandLoc.getWorld().getName(), armorStandLoc.getBlockX(), armorStandLoc.getBlockY(), armorStandLoc.getBlockZ()),
                                new Toilet.LocationPojo(ironDoorLoc.getWorld().getName(), ironDoorLoc.getBlockX(), ironDoorLoc.getBlockY(), ironDoorLoc.getBlockZ()),
                                new Toilet.LocationPojo(scytheLoc.getWorld().getName(), scytheLoc.getBlockX(), scytheLoc.getBlockY(), scytheLoc.getBlockZ())
                        );

                }
        return null;
    }

    public Toilet getToilet(String name)
    {
        return toilets.get(name);
    }

    public void registerToilet(String name, Toilet toilet)
    {
        toilets.put(name, toilet);
    }

    public void save() throws IOException
    {
        this.saveToFile(file);
    }

    public void unregisterToilet(String name)
    {
        toilets.remove(name);
    }

    public Location[] getToilets()
    {
        return this.toilets.values().stream()
                .map(Toilet::getArmorStandLocation)
                .map(locationPojo -> new Location(Bukkit.getWorld(locationPojo.getWorldName()), locationPojo.getX(), locationPojo.getY(), locationPojo.getZ()))
                .toArray(Location[]::new);
    }

    public void saveToFile(File file) throws IOException
    {
        Gson gson = new Gson();
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            fos.write(gson.toJson(toilets).getBytes());
        }
    }
}
