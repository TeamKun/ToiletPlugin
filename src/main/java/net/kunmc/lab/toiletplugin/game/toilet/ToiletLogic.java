package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.player.PlayerManager;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.game.sound.SoundArea;
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

import java.util.ConcurrentModificationException;
import java.util.HashMap;

public class ToiletLogic implements Listener
{
    private final GameMain game;
    @Getter
    private final InformationDisplay toiletInformationDisplay;
    private final PlayerManager playerManager;
    private final ToiletManager toiletManager;

    private HashMap<Player, OnGroundToilet> foundOnPreviousTick;

    public ToiletLogic(GameMain game, ToiletManager toiletManager)
    {
        this.game = game;
        this.playerManager = this.game.getPlayerStateManager();
        this.toiletManager = toiletManager;
        this.toiletInformationDisplay = new InformationDisplay(game, toiletManager);
        this.foundOnPreviousTick = new HashMap<>();
    }

    public void init()
    {
        this.toiletInformationDisplay.init();
    }

    public void onTwoTick(int tick)
    {
        this.playerManager.getGamePlayers()
                .values()
                .stream()
                .filter(GamePlayer::isQuesting)
                .map(GamePlayer::getPlayer)
                .forEach(this::checkPlayerInToilet);
    }

    public void onSecond()
    {
        try
        {
            this.toiletManager.getToilets().forEach((key, toilet) -> {
                if (toilet.getState() != ToiletState.OPEN)
                    toilet.setTimesElapsed(toilet.getTimesElapsed() + 1);
                if (toilet.getCooldownMax() > 0 && toilet.getCooldown() > 0)
                    toilet.setCooldown(toilet.getCooldown() - 1);

                if (toilet.getCooldown() == 0)
                    this.onCooldownFinished(toilet);

                this.toiletInformationDisplay.updateToilet(toilet);
            });
        }
        catch (ConcurrentModificationException ignored)
        {
        }
    }

    private void onCooldownFinished(OnGroundToilet toilet)
    {
        if (toilet.getState() == ToiletState.PLAYER_COOLDOWN)
        {
            if (!this.game.getConfig().isToiletCooldownEnable())
            {
                toilet.setState(ToiletState.OPEN);
                return;
            }

            toilet.setState(ToiletState.TOILET_COOLDOWN);
            toilet.setDoorOpen(false);
            int cooldown = this.game.getConfig().generateToiletCooldownTime();
            toilet.setCooldownMax(cooldown);
            toilet.setCooldown(cooldown);
            toilet.setToiletPlayer(null);
        }
        else if (toilet.getState() == ToiletState.TOILET_COOLDOWN)
        {
            toilet.setState(ToiletState.OPEN);
        }
    }

    public void checkPlayerInToilet(Player player)
    {
        HashMap<Player, OnGroundToilet> found = new HashMap<>();

        player.getNearbyEntities(3.0D, 1.0D, 3.0D).forEach(entity -> {
            if (!(entity instanceof ArmorStand))
                return;
            ArmorStand armorStand = (ArmorStand) entity;
            if (!armorStand.getScoreboardTags().contains("info_toilet"))
                return;

            OnGroundToilet toilet = this.game.getToiletManager().getToilet(armorStand);

            if (toilet == null)
                return;

            if (toilet.getState() != ToiletState.OPEN)
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

            found.put(player, toilet);
            this.playerJoinToilet(player, toilet, door, toiletBlock, armorStand);
        });

        if (this.foundOnPreviousTick.size() > found.size())
        {
            this.foundOnPreviousTick.entrySet().stream()
                    .filter(p -> !found.containsKey(p.getKey()))
                    .forEach(playerOnGroundToiletEntry -> {
                        this.playerLeftToilet(playerOnGroundToiletEntry.getKey(), playerOnGroundToiletEntry.getValue());
                    });
        }

        this.foundOnPreviousTick = found;
    }

    public void playerJoinToilet(Player player, OnGroundToilet toilet, Door door, Block doorBlock, ArmorStand informationDisplay)
    {
        GameSound.IRON_DOOR_CLOSE.play(player, SoundArea.NEAR_10);
        door.setOpen(true);
        doorBlock.setBlockData(door, true);

        this.toiletInformationDisplay.playerJoinToilet(player, toilet.getName());

        this.playerManager.getPlayer(player).setToilet(toilet);

        this.toiletManager.playerJoinToilet(player, toilet, informationDisplay);
    }

    public void playerLeftToilet(Player player, OnGroundToilet toilet)
    {
        this.toiletManager.playerLeftToilet(player, toilet);

        this.playerManager.getPlayer(player).setToilet(null);
    }

    @EventHandler
    public void onEntityBreakBlock(EntityExplodeEvent e)
    {
        e.blockList().removeIf(block -> this.toiletManager.getToilet(block.getLocation()) != null);
    }
}
