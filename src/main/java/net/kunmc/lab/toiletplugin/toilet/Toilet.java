package net.kunmc.lab.toiletplugin.toilet;

import lombok.Data;
import org.bukkit.Location;

import java.io.Serializable;

@Data
public class Toilet implements Serializable
{
    private final String name;

    private final LocationPojo armorStandLocation;
    private final LocationPojo scytheLocation;
    private final LocationPojo doorLocation;

    private final String armorStandUUID;
    private final String ironDoorASUUID;

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
    }
}
