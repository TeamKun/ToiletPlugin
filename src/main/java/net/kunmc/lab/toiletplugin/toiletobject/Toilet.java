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
