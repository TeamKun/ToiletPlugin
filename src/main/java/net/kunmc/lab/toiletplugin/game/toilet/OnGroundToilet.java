package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OnGroundToilet extends Toilet
{
    private final Toilet toilet;
    private final ArmorStand informationArmorStand;

    private List<Entity> displays;

    private Player toiletPlayer;

    private int timesElapsed;
    private int cooldownMax;

    private int displayNonce;

    private ToiletState state;

    public OnGroundToilet(Toilet toilet)
    {
        super(toilet);
        this.state = ToiletState.OPEN;
        this.cooldownMax = 0;
        this.timesElapsed = 0;
        this.toilet = toilet;
        this.informationArmorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(toilet.getToiletInfoBaseArmorStandUUID()));
        this.toiletPlayer = null;
        this.displays = new ArrayList<>();

        if (this.informationArmorStand == null)
            return;

        this.displays.addAll(recordPassengerDisplays(this.informationArmorStand.getPassengers()));
    }

    private static List<ArmorStand> recordPassengerDisplays(List<Entity> passengers)
    {
        List<ArmorStand> accumulationDisplays = new ArrayList<>();
        passengers.forEach(entity ->
        {
            if (!(entity instanceof ArmorStand))
                return;
            if (entity.getScoreboardTags().contains("registered_toilet"))
                accumulationDisplays.add((ArmorStand) entity);
            accumulationDisplays.addAll(recordPassengerDisplays(entity.getPassengers()));
        });

        return accumulationDisplays;
    }

    public void killEntities()
    {
        displays.forEach(Entity::remove);
    }

    public void setState(ToiletState state)
    {
        this.state = state;
        if (state == ToiletState.OPEN)
            this.purge();
        else
            setDoor(true);
    }

    public void purge()
    {
        this.state = ToiletState.OPEN;
        this.timesElapsed = 0;
        this.cooldownMax = 0;
        this.toiletPlayer = null;
        setDoor(false);
    }

    private void setDoor(boolean open)
    {
        Block doorBlock = this.getDoorLocation().toLocation().getBlock();

        if (!(doorBlock.getBlockData() instanceof Door))
            return;
        Door door = (Door) doorBlock.getBlockData();
        door.setOpen(open);
        doorBlock.setBlockData(door, true);
    }

}
