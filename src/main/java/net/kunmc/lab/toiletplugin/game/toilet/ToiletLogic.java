package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import net.kunmc.lab.toiletplugin.utils.DirectionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ToiletLogic extends BukkitRunnable implements Listener
{
    private final GameMain game;
    @Getter
    private final InformationDisplay toiletInformationDisplay;
    private final QuestManager questManager;
    private final ToiletManager toiletManager;
    private int count = 0;

    public ToiletLogic(GameMain game, ToiletManager toiletManager)
    {
        this.game = game;
        this.questManager = this.game.getQuestManager();
        this.toiletManager = toiletManager;
        this.toiletInformationDisplay = new InformationDisplay(game);
    }

    public void init()
    {
        this.toiletInformationDisplay.init();
    }

    @Override
    public void run()
    {
        count += 2;
        // Detect player join to toilet.
        this.questManager.getQuestingPlayer().forEach(this::checkCollision);
        if (count < 20)
            return;
        count = 0;

        // Onsec
        this.toiletInformationDisplay.update();
    }

    public void checkCollision(Player player)
    {
        player.getNearbyEntities(3.0D, 1.0D, 3.0D).forEach(entity -> {
            if (!(entity instanceof ArmorStand))
                return;
            ArmorStand armorStand = (ArmorStand) entity;
            if (!armorStand.getScoreboardTags().contains("info_toilet"))
                return;

            Toilet toilet = this.game.getToiletManager().getToilet(armorStand);

            if (toilet == null)
                return;

            Location backDoorLock = toilet.getDoorLocation().toLocation();
            Block toiletBlock = toilet.getDoorLocation().toLocation().getBlock();
            if (toiletBlock.getType() != Material.IRON_DOOR)
                return;
            Door door = (Door) toiletBlock.getBlockData();

            if (door.isOpen())
                return;

            backDoorLock.setWorld(player.getWorld());
            backDoorLock = DirectionUtils.getDirLoc(backDoorLock, 1,
                    DirectionUtils.reverseDirection(toilet.getDirection())
            );

            if (door.getHalf() == Bisected.Half.TOP)
                backDoorLock.add(0, -1, 0);

            if (player.getLocation().getBlockX() != backDoorLock.getBlockX()
                    || player.getLocation().getBlockY() != backDoorLock.getBlockY()
                    || player.getLocation().getBlockZ() != backDoorLock.getBlockZ())
                return;

            this.playerJoinToilet(player, toilet, door, toiletBlock, armorStand);
        });
    }

    public void playerJoinToilet(Player player, Toilet toilet, Door door, Block doorBlock, ArmorStand informationDisplay)
    {
        player.getWorld().playSound(player.getLocation(), "block.iron_door.close", 1.0F, 1.0F);
        door.setOpen(true);
        doorBlock.setBlockData(door, true);

        this.toiletInformationDisplay.playerJoinToilet(player, toilet.getName());

        this.toiletManager.playerJoinToilet(player, toilet, informationDisplay);
    }
}
