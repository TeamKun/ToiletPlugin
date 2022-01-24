package net.kunmc.lab.toiletplugin.toilet.generate;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.toilet.Toilet;
import net.kunmc.lab.toiletplugin.toilet.ToiletRegister;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class ToiletGenerator
{
    public static void generateToilet(Player placer, String name, Location location, BlockFace direction)
    {
        if (!ToiletPlugin.getPlugin().getModelManager().contains(name))
            return;

        StructureRotation dir = convertBlockFaceToStructureRotation(direction);

        StructureBlockLibApi.INSTANCE
                .loadStructure(ToiletPlugin.getPlugin())
                .at(location)
                .includeEntities(true)
                .rotation(dir)
                .loadFromFile(ToiletPlugin.getPlugin().getModelManager().fromName(name))
                .onException(throwable -> {
                    throw new RuntimeException(throwable);
                })
                .onResult(a -> onComplete(placer, location, direction));
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

    private static void onComplete(Player placer, Location location, BlockFace direction)
    {
        location.getWorld().getNearbyEntitiesByType(ArmorStand.class, location, 5, armorStand -> {
                    Set<String> tags = armorStand.getScoreboardTags();
                    return tags.contains("toilet") && !tags.contains("registered_toilet");
                })
                .forEach(armorStand -> {
                    Toilet toilet = ToiletRegister.detect(armorStand);
                    if (toilet == null)
                    {
                        placer.sendMessage(ChatColor.RED + "E: トイレの検出に失敗しました。");
                        placer.sendMessage(ChatColor.BLUE + "I: 場所を変えて再度トイレを作成してください。");
                        return;
                    }

                    Toilet.LocationPojo doorLoc = toilet.getDoorLocation();
                    ArmorStand iDAS = (ArmorStand) location.getWorld().spawnEntity(
                            new Location(location.getWorld(), doorLoc.getX() + 0.5, doorLoc.getY(), doorLoc.getZ() + 0.5),
                            EntityType.ARMOR_STAND
                    );


                    toilet = new Toilet(toilet.getArmorStandLocation(), toilet.getScytheLocation(), toilet.getDoorLocation(),
                            toilet.getArmorStandUUID(), iDAS.getUniqueId().toString()
                    );

                    String name = UUID.randomUUID().toString().substring(0, 8);
                    ToiletPlugin.getPlugin().getToilets().registerToilet(name, toilet);

                    patchArmorStand(armorStand, name, direction);
                    patchArmorStand(iDAS, name, direction);

                    placer.sendMessage(ChatColor.GREEN + "S: トイレを「" + name + "」として作成しました。");
                });
    }

    private static void patchArmorStand(ArmorStand stand, String name, BlockFace direction)
    {
        stand.addScoreboardTag("registered_toilet");
        stand.addScoreboardTag("toilet_" + name);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomName(ChatColor.GREEN + "Toilet: " + name);

        if (direction == null)
            return;

        switch (direction)
        {
            case EAST:
                stand.setRotation(-90, 90);
                break;
            case SOUTH:
                stand.setRotation(0, 90);
                break;
            case NORTH:
                stand.setRotation(-180, 90);
                break;
            case WEST:
                stand.setRotation(90, 90);
        }
    }

}
