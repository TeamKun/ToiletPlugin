package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.events.PlayerToiletJoinEvent;
import net.kunmc.lab.toiletplugin.events.PlayerToiletQuitEvent;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.player.PlayerManager;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.game.sound.SoundArea;
import net.kunmc.lab.toiletplugin.utils.DirectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

public class ToiletLogic implements Listener
{
    private final GameMain game;
    @Getter
    private final InformationDisplay toiletInformationDisplay;
    private final PlayerManager playerManager;
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
        this.playerManager.getGamePlayers()
                .values()
                .stream()
                .filter(GamePlayer::isQuesting)
                .filter(gamePlayer -> gamePlayer.getQuestPhase() != QuestPhase.PLAYER_COOLDOWN)
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

            int cooldown = this.game.getConfig().generateToiletCooldownTime();
            toilet.setCooldown(ToiletState.TOILET_COOLDOWN, cooldown);
        }
        else if (toilet.getState() == ToiletState.TOILET_COOLDOWN)
        {
            toilet.setState(ToiletState.OPEN);
        }
    }

    public void checkPlayerInToilet(Player player)
    {
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
            this.playerJoinToilet(player, toilet, door, toiletBlock, armorStand);
        });
    }

    public void playerJoinToilet(Player player, OnGroundToilet toilet, Door door, Block doorBlock, ArmorStand informationDisplay)
    {
        GameSound.IRON_DOOR_CLOSE.play(player, SoundArea.NEAR_10);
        door.setOpen(true);
        doorBlock.setBlockData(door, true);

        this.toiletInformationDisplay.playerJoinToilet(player, toilet.getName());

        this.playerManager.getPlayer(player).setToilet(toilet);

        Bukkit.getPluginManager().callEvent(
                new PlayerToiletJoinEvent(this.game.getPlayerStateManager().getPlayer(player), toilet));
    }

    public void playerLeftToilet(Player player, OnGroundToilet toilet)
    {
        this.playerManager.getPlayer(player).setToilet(null);
        Bukkit.getPluginManager().callEvent(
                new PlayerToiletQuitEvent(this.game.getPlayerStateManager().getPlayer(player), toilet));
    }

    @EventHandler
    public void onEntityBreakBlock(EntityExplodeEvent e)
    {
        List<Block> blocks = e.blockList();

        this.toiletManager.getToiletLocationsStream()
                .parallel()
                .forEach(location -> {
                    try
                    {
                        List<Block> removeBlocks = blocks.stream()
                                .filter(block -> {
                                    if (block == null)
                                        return false;

                                    Location loc = block.getLocation();

                                    int distanceX = Math.abs(loc.getBlockX() - location.getBlockX());
                                    int distanceY = Math.abs(loc.getBlockY() - location.getBlockY());
                                    int distanceZ = Math.abs(loc.getBlockZ() - location.getBlockZ());

                                    return distanceX < 7 && distanceY < 7 && distanceZ < 7;
                                }).collect(Collectors.toList());

                        e.blockList().removeAll(removeBlocks);
                    }
                    catch (Exception ignored)
                    {
                    }
                });

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(PlayerQuitEvent event)
    {
        GamePlayer gamePlayer = this.playerManager.getPlayer(event.getPlayer());

        if (gamePlayer == null)
            return;

        if (gamePlayer.getToilet() != null)
            return;

        this.playerLeftToilet(gamePlayer.getPlayer(), gamePlayer.getToilet());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e)
    {
        GamePlayer gamePlayer = this.playerManager.getPlayer(e.getPlayer());

        if (gamePlayer.getToilet() == null)
            return;

        if (e.getFrom().getWorld().equals(e.getTo().getWorld()))
            this.playerLeftToilet(gamePlayer.getPlayer(), gamePlayer.getToilet());
        else if (e.getFrom().distance(e.getTo()) >= 5)
            this.playerLeftToilet(gamePlayer.getPlayer(), gamePlayer.getToilet());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent e)
    {
        GamePlayer gamePlayer = this.playerManager.getPlayer(e.getEntity());

        if (gamePlayer.getToilet() == null)
            return;

        this.playerLeftToilet(gamePlayer.getPlayer(), gamePlayer.getToilet());
    }

    @EventHandler
    public void onPlayerQuitToilet(PlayerToiletQuitEvent event)
    {
        OnGroundToilet toilet = event.getToilet();

        if (!game.getConfig().isToiletCooldownEnable())
        {
            toilet.purge();
            return;
        }


        if (toilet.isCooldown())
            return;

        int cooldown = game.getConfig().generateToiletCooldownTime();
        toilet.setCooldown(ToiletState.TOILET_COOLDOWN, cooldown);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBreakBlock(BlockBreakEvent e)
    {
        if (!this.playerManager.getPlayer(e.getPlayer()).isPlaying())
            return;

        Location location = e.getBlock().getLocation();

        boolean b = this.toiletManager.getToiletLocationsStream()
                .parallel()
                .anyMatch(loc -> {
                    int distanceX = Math.abs(loc.getBlockX() - location.getBlockX());
                    int distanceY = Math.abs(loc.getBlockY() - location.getBlockY());
                    int distanceZ = Math.abs(loc.getBlockZ() - location.getBlockZ());

                    return distanceX < 7 && distanceY < 7 && distanceZ < 7;
                });

        if (!b)
            return;

        e.getPlayer().sendMessage(ChatColor.RED + "トイレの近くのブロックは破壊できません！");
        e.setCancelled(true);
    }
}
