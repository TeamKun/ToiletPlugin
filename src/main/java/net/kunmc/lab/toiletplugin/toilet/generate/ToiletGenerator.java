package net.kunmc.lab.toiletplugin.toilet.generate;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class ToiletGenerator
{
    public static void generateToilet(String name, Location location, BlockFace direction)
    {
        if (!ToiletPlugin.getPlugin().getModelManager().contains(name))
            return;

        StructureBlockLibApi.INSTANCE
                .loadStructure(ToiletPlugin.getPlugin())
                .at(location)
                .includeEntities(true)
                .rotation(convertBlockFaceToStructureRotation(direction))
                .loadFromFile(ToiletPlugin.getPlugin().getModelManager().fromName(name))
                .onException(throwable -> {
                    throw new RuntimeException(throwable);
                });

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
