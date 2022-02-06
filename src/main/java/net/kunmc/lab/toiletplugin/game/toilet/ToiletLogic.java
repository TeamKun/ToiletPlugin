package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.PlayerStateManager;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import net.kunmc.lab.toiletplugin.utils.DirectionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ToiletLogic implements Listener
{
    private final GameMain game;
    @Getter
    private final InformationDisplay toiletInformationDisplay;
    private final PlayerStateManager playerManager;
    private final ToiletManager toiletManager;

    public ToiletLogic(GameMain game, ToiletManager toiletManager)
    {
        this.game = game;
        this.playerManager = this.game.getPlayerStateManager();
        this.toiletManager = toiletManager;
        this.toiletInformationDisplay = new InformationDisplay(game, toiletManager);
    }

    public void init()
    {
        this.toiletInformationDisplay.init();
    }

    public void onTwoTick(int tick)
    {
        this.playerManager.getQuestingPlayer().forEach((player, integer) -> this.checkPlayerInToilet(player));
    }

    public void onSecond()
    {
        this.toiletInformationDisplay.update();
    }

    public void checkPlayerInToilet(Player player)
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

    @EventHandler
    public void onEntityBreakBlock(EntityExplodeEvent e)
    {
        e.blockList().removeIf(block -> this.toiletManager.getToilet(block.getLocation()) != null);
    }
}
