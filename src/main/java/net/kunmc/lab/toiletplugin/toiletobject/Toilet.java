package net.kunmc.lab.toiletplugin.toiletobject;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.io.Serializable;

@Data
public class Toilet implements Serializable
{
    private final String name;

    private final BlockFace direction;

    private final LocationPojo armorStandLocation;
    private final LocationPojo scytheLocation;
    private final LocationPojo doorLocation;

    private final String armorStandUUID;
    private final String toiletInfoBaseArmorStandUUID;

    public Toilet(String name, BlockFace direction, LocationPojo armorStandLocation, LocationPojo scytheLocation, LocationPojo doorLocation, String armorStandUUID, String toiletInfoBaseArmorStandUUID)
    {
        this.name = name;
        this.direction = direction;
        this.armorStandLocation = armorStandLocation;
        this.scytheLocation = scytheLocation;
        this.doorLocation = doorLocation;
        this.armorStandUUID = armorStandUUID;
        this.toiletInfoBaseArmorStandUUID = toiletInfoBaseArmorStandUUID;
    }

    public Toilet(Toilet toilet)
    {
        this.name = toilet.getName();
        this.direction = toilet.getDirection();
        this.armorStandLocation = toilet.getArmorStandLocation();
        this.scytheLocation = toilet.getScytheLocation();
        this.doorLocation = toilet.getDoorLocation();
        this.armorStandUUID = toilet.getArmorStandUUID();
        this.toiletInfoBaseArmorStandUUID = toilet.getToiletInfoBaseArmorStandUUID();
    }

    @Data
    public static class LocationPojo
    {
        private final String worldName;
        private final int x;
        private final int y;
        private final int z;

        public static LocationPojo fromLocation(Location loc)
        {
            return new LocationPojo(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        public Location toLocation()
        {
            return new Location(Bukkit.getWorld(worldName), x, y, z);
        }
    }
}
