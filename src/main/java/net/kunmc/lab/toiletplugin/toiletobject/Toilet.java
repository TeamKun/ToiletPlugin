package net.kunmc.lab.toiletplugin.toiletobject;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.io.Serializable;

@Data
public class Toilet implements Serializable
{
    @Expose
    private final String name;

    @Expose
    private final BlockFace direction;

    @Expose
    private final LocationPojo armorStandLocation;
    @Expose
    private final LocationPojo scytheLocation;
    @Expose
    private final LocationPojo doorLocation;

    @Expose
    private final String armorStandUUID;
    @Expose
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
        @Expose
        private final String worldName;
        @Expose
        private final int x;
        @Expose
        private final int y;
        @Expose
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
