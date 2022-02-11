package net.kunmc.lab.toiletplugin.utils;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class DirectionUtils
{
    public static Location getDirLoc(Location loc, double distance, BlockFace face)
    {
        switch (face)
        {
            case NORTH:
                return loc.clone().add(0.0D, 0.0D, -distance);
            case SOUTH:
                return loc.clone().add(0.0D, 0.0D, distance);
            case EAST:
                return loc.clone().add(distance, 0.0D, 0.0D);
            case WEST:
                return loc.clone().add(-distance, 0.0D, 0.0D);
        }

        return loc;
    }

    public static BlockFace getDirection(double yaw)
    {
        if (yaw < 0.0D)
        {
            yaw += 360.0D;
        }


        return yaw >= 45.0D && yaw < 135.0D ? BlockFace.WEST:
                (yaw >= 135.0D && yaw < 225.0D ? BlockFace.NORTH:
                        (yaw >= 225.0D && yaw < 315.0D ? BlockFace.EAST:
                                BlockFace.SOUTH));
    }

    public static BlockFace reverseDirection(BlockFace face)
    {
        switch (face)
        {
            case NORTH:
                return BlockFace.SOUTH;
            case EAST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.EAST;
            default:
                return BlockFace.NORTH;
        }
    }

    public static StructureRotation convertBlockFaceToStructureRotation(BlockFace face)
    {
        switch (face)
        {
            case NORTH:
                return StructureRotation.NONE;
            case EAST:
                return StructureRotation.ROTATION_90;
            case WEST:
                return StructureRotation.ROTATION_270;
            case SOUTH:
            default:
                return StructureRotation.ROTATION_180;
        }
    }
}
