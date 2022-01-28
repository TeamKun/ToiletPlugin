package net.kunmc.lab.toiletplugin.toiletobject.generate;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import net.kunmc.lab.toiletplugin.toiletobject.ToiletRegister;
import net.kunmc.lab.toiletplugin.utils.DirectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Set;

public class ToiletGenerator implements Listener
{
    private final ToiletRegister register;

    public ToiletGenerator(ToiletRegister register)
    {
        this.register = register;
    }

    private static TextComponent copy(String column, String text)
    {
        return Component.text(ChatColor.GREEN + column + ": " + ChatColor.WHITE + text)
                .clickEvent(ClickEvent.copyToClipboard(text))
                .hoverEvent(HoverEvent.showText(Component.text(ChatColor.GREEN + "クリックしてコピー")));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e)
    {
        if (e.getBlock().getType() != Material.CAULDRON && e.getBlock().getType() != Material.IRON_DOOR)
            return;
        Toilet toilet = register.getToilet(e.getBlock().getLocation());

        if (toilet == null)
            return;

        e.setCancelled(true);

        if (!e.getPlayer().hasPermission("toilet.admin"))
        {
            e.getPlayer().sendMessage(ChatColor.RED + "E: このブロックはトイレとして登録されているため破壊できません！");
            return;
        }

        Player player = e.getPlayer();

        Toilet.LocationPojo location = toilet.getArmorStandLocation();
        Toilet.LocationPojo door = toilet.getDoorLocation();
        Toilet.LocationPojo scythe = toilet.getScytheLocation();

        player.sendMessage(ChatColor.GOLD + "---=== トイレ情報 ===---");
        player.sendMessage(copy("名前", toilet.getName()));
        player.sendMessage(copy("中心位置", location.getX() + "," + location.getY() + "," + location.getZ()));
        player.sendMessage(copy("ドア位置", door.getX() + "," + door.getY() + "," + door.getZ()));
        player.sendMessage(copy("大釜位置", scythe.getX() + "," + scythe.getY() + "," + scythe.getZ()));
        player.sendMessage(Component.text("操作： ")
                .color(NamedTextColor.GREEN)
                .append(Component.text("[削除] ")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/toilet remove " + toilet.getName())))
                .hoverEvent(HoverEvent.showText(Component.text(ChatColor.RED + "トイレ「" + toilet.getName() + "」を削除します。"))));
        player.sendMessage(ChatColor.GOLD + "---=== トイレ情報 ===---");
    }

    public static void generateToilet(Player placer, String name, Location location, BlockFace direction)
    {
        if (!ToiletPlugin.getPlugin().getModelManager().contains(name))
            return;

        StructureRotation dir = DirectionUtils.convertBlockFaceToStructureRotation(direction);

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

    private static void onComplete(Player placer, Location location, BlockFace direction)
    {
        location.getWorld().getNearbyEntitiesByType(ArmorStand.class, location, 5, armorStand -> {
                    Set<String> tags = armorStand.getScoreboardTags();
                    return tags.contains("toilet") && !tags.contains("registered_toilet");
                })
                .forEach(armorStand -> {
                    Toilet toilet = ToiletPlugin.getPlugin().getToilets().detect(armorStand, direction);
                    if (toilet == null)
                    {
                        placer.sendMessage(ChatColor.RED + "E: トイレの検出に失敗しました。");
                        placer.sendMessage(ChatColor.BLUE + "I: 場所を変えて再度トイレを作成してください。");
                        return;
                    }

                    Toilet.LocationPojo doorLoc = toilet.getDoorLocation();
                    int scanned_door_y = doorLoc.getY();

                    if (placer.getWorld().getBlockAt(doorLoc.getX(), scanned_door_y - 1, doorLoc.getZ()).getType() == Material.IRON_DOOR)
                        scanned_door_y -= 2;
                    else
                        scanned_door_y -= 1;

                    Location infoArmorStand = new Location(location.getWorld(), doorLoc.getX() + 0.5, scanned_door_y, doorLoc.getZ() + 0.5);
                    infoArmorStand = DirectionUtils.getDirLoc(infoArmorStand, 1, direction);


                    assert infoArmorStand != null;
                    ArmorStand infoArmorStandEntity = (ArmorStand) location.getWorld().spawnEntity(
                            infoArmorStand, EntityType.ARMOR_STAND);

                    infoArmorStandEntity.addScoreboardTag("info_toilet");
                    infoArmorStandEntity.setCustomName(ChatColor.GOLD + "TOILET!!");
                    infoArmorStandEntity.setCustomNameVisible(true);

                    Toilet.LocationPojo toiletLoc = new Toilet.LocationPojo(doorLoc.getWorldName(), doorLoc.getX(), doorLoc.getY(), doorLoc.getZ());

                    toilet = new Toilet(toilet.getName(), toilet.getDirection(),
                            toilet.getArmorStandLocation(), toilet.getScytheLocation(), toiletLoc,
                            toilet.getArmorStandUUID(), infoArmorStandEntity.getUniqueId().toString()
                    );

                    String name = toilet.getName();
                    ToiletPlugin.getPlugin().getToilets().registerToilet(name, toilet);
                    ToiletPlugin.getPlugin().getGame().getToiletManager().getLogic().getToiletInformationDisplay().addToilet(toilet);

                    patchArmorStand(armorStand, name, direction);
                    patchArmorStand(infoArmorStandEntity, name, direction);

                    placer.sendMessage(ChatColor.GREEN + "S: トイレを「" + name + "」として作成しました。");
                });
    }

    private static void patchArmorStand(ArmorStand stand, String name, BlockFace direction)
    {
        stand.addScoreboardTag("registered_toilet");
        stand.addScoreboardTag("toilet_" + name);
        stand.setVisible(false);
        stand.setGravity(false);

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
