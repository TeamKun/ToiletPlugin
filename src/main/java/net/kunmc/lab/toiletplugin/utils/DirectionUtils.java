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
