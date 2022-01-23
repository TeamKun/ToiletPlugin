package net.kunmc.lab.toiletplugin.toilet;

import lombok.Data;

import java.io.Serializable;

@Data
public class Toilet implements Serializable
{
    private final LocationPojo armorStandLocation;
    private final LocationPojo scytheLocation;
    private final LocationPojo doorLocation;

    @Data
    public static class LocationPojo
    {
        private final String worldName;
        private final int x;
        private final int y;
        private final int z;
    }
}
