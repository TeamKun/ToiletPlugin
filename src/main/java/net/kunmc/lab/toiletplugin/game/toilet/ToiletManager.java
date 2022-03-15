package net.kunmc.lab.toiletplugin.game.toilet;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ToiletManager
{
    private static final int RADIUS = 5;

    private final GameMain game;
    @Getter
    private final ToiletLogic logic;

    @Getter
    private final HashMap<String, OnGroundToilet> toilets;
    @Getter
    private final File file;

    public ToiletManager(GameMain game, File file)
    {
        this.game = game;
        this.file = file;
        this.toilets = new HashMap<>();
        this.logic = new ToiletLogic(game, this);
    }

    public void init()
    {
        try
        {
            this.loadFromFile(this.file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to load toilet data.", e);
        }

        Bukkit.getPluginManager().registerEvents(this.logic, ToiletPlugin.getPlugin());
        this.logic.init();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                new HashMap<>(toilets).values().forEach(toilet -> {
                    toilet.getArmorStandLocation().toLocation().getChunk().load();
                    toilet.getDoorLocation().toLocation().getChunk().load();
                    toilet.getScytheLocation().toLocation().getChunk().load();
                    toilet = new OnGroundToilet(toilet);
                    toilet.purge();
                    toilets.put(toilet.getName(), toilet);
                });

                ToiletManager.this.game.getToiletMap().updateToilets();
            }
        }.runTaskLater(ToiletPlugin.getPlugin(), 0);

    }

    public Toilet detectToilet(ArmorStand stand, BlockFace direction)
    {
        Location armorStandLoc = stand.getLocation();
        Location ironDoorLoc = null;
        Location scytheLoc = null;
        World world = stand.getWorld();

        for (int x = -RADIUS; x <= RADIUS; x++)
            for (int y = -RADIUS; y <= RADIUS; y++)
                for (int z = -RADIUS; z <= RADIUS; z++)
                {
                    Location location = stand.getLocation().clone().add(x, y, z);
                    if (location.getBlock().getType() == Material.IRON_DOOR)
                    {
                        if (getToilet(location) != null)
                            continue;
                        if (world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()).getType() == Material.IRON_DOOR)
                            if (getToilet(location.clone().add(0, -1, 0)) != null)
                                continue;
                        if (world.getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()).getType() == Material.IRON_DOOR)
                            if (getToilet(location.clone().add(0, 1, 0)) != null)
                                continue;
                        ironDoorLoc = location;
                    }
                    if (location.getBlock().getType() == Material.CAULDRON)
                    {
                        if (getToilet(location) == null)
                            scytheLoc = location;
                    }
                    if (ironDoorLoc != null && scytheLoc != null)
                    {
                        return new Toilet(
                                stand.getUniqueId().toString().substring(0, 8),
                                direction,
                                new Toilet.LocationPojo(armorStandLoc.getWorld().getName(), armorStandLoc.getBlockX(), armorStandLoc.getBlockY(), armorStandLoc.getBlockZ()),
                                new Toilet.LocationPojo(scytheLoc.getWorld().getName(), scytheLoc.getBlockX(), scytheLoc.getBlockY(), scytheLoc.getBlockZ()),
                                new Toilet.LocationPojo(ironDoorLoc.getWorld().getName(), ironDoorLoc.getBlockX(), ironDoorLoc.getBlockY(), ironDoorLoc.getBlockZ()),
                                stand.getUniqueId().toString(),
                                null
                        );
                    }
                }
        return null;
    }

    public OnGroundToilet getToilet(String name)
    {
        return toilets.get(name);
    }

    public void registerToilet(String name, OnGroundToilet toilet)
    {
        toilets.put(name, toilet);
        game.getToiletMap().updateToilets();
    }

    public void save() throws IOException
    {
        this.saveToFile(file);
    }

    public OnGroundToilet unregisterToilet(String name)
    {
        OnGroundToilet toilet = toilets.remove(name);
        toilet.killEntities();

        game.getToiletMap().updateToilets();
        return toilet;
    }

    public List<Toilet> getToiletList()
    {
        return new ArrayList<>(toilets.values());
    }

    public Location[] getToiletLocations()
    {
        return this.toilets.values().stream()
                .map(Toilet::getArmorStandLocation)
                .map(locationPojo -> new Location(Bukkit.getWorld(locationPojo.getWorldName()), locationPojo.getX(), locationPojo.getY(), locationPojo.getZ()))
                .toArray(Location[]::new);
    }

    public List<String> getToiletNames()
    {
        return new ArrayList<>(this.toilets.keySet());
    }

    public boolean containsToilet(String name)
    {
        return this.toilets.containsKey(name);
    }

    public OnGroundToilet getToilet(Location anyLoc)
    {
        return this.toilets.values().stream()
                .filter(toilet -> {
                    Toilet.LocationPojo doorLocation = toilet.getDoorLocation();
                    return
                            toilet.getArmorStandLocation().equals(Toilet.LocationPojo.fromLocation(anyLoc))
                                    || ((
                                    doorLocation.getX() == anyLoc.getBlockX()
                                            && doorLocation.getZ() == anyLoc.getBlockZ()
                            ) && (
                                    doorLocation.getY() == anyLoc.getBlockY()
                                            || doorLocation.getY() == anyLoc.getBlockY() - 1
                                            || doorLocation.getY() == anyLoc.getBlockY() - 2
                                            || doorLocation.getY() == anyLoc.getBlockY() + 1
                            )) || toilet.getScytheLocation().equals(Toilet.LocationPojo.fromLocation(anyLoc));
                })
                .findFirst()
                .orElse(null);
    }

    public OnGroundToilet getToilet(Entity entity)
    {
        return this.toilets.values().stream()
                .filter(toilet -> {
                    String toiletName = toilet.getName();
                    return entity.getScoreboardTags().contains("toilet_" + toiletName);
                })
                .findFirst()
                .orElse(null);
    }

    public void saveToFile(File file) throws IOException
    {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        try (FileOutputStream fos = new FileOutputStream(file))
        {

            fos.write(gson.toJson(toilets).getBytes());
        }
    }

    public void loadFromFile(File file) throws IOException
    {
        if (!file.exists())
            return;

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        toilets.putAll(gson.fromJson(new String(Files.readAllBytes(file.toPath())), new TypeToken<HashMap<String, Toilet>>()
        {
        }.getType()));
    }
}
