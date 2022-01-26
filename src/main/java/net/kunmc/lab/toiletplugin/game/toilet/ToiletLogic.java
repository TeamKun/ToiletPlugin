package net.kunmc.lab.toiletplugin.game.toilet;

import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ToiletLogic extends BukkitRunnable implements Listener
{
    private final GameMain game;

    public ToiletLogic(GameMain game)
    {
        this.game = game;

    }

    @Override
    public void run()
    {
        this.game.getQuested().forEach(this::checkCollision);
    }

    public void checkCollision(Player player)
    {
        player.getNearbyEntities(1.0D, 1.0D, 1.0D).forEach(entity -> {
            if (!(entity instanceof ArmorStand))
                return;
            ArmorStand armorStand = (ArmorStand) entity;
            if (!armorStand.getScoreboardTags().contains("door_toilet"))
                return;

            Toilet toilet = this.game.getRegister().getToilet(armorStand.getLocation().toBlockLocation());
            if (toilet == null)
                return;

            Door door = (Door) toilet.getDoorLocation().toLocation().getBlock().getBlockData();

            door.setOpen(true);
        });
    }

}
