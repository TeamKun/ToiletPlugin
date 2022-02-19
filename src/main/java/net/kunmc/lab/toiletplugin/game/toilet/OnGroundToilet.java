package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.game.sound.SoundArea;
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

    private List<ArmorStand> displays;

    private GamePlayer toiletPlayer;

    private int timesElapsed;
    private int cooldownMax;
    private int cooldown;
    private int displayNonce;

    private ToiletState state;

    public void setToiletPlayer(Player player)
    {
        this.toiletPlayer = ToiletPlugin.getPlugin().getGame().getPlayerStateManager().getPlayer(player);
    }

    public OnGroundToilet(Toilet toilet)
    {
        super(toilet);
        this.state = ToiletState.OPEN;
        this.cooldownMax = 0;
        this.cooldown = 0;
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
            setDoorOpen(true);
    }

    public void purge()
    {
        this.state = ToiletState.OPEN;
        this.timesElapsed = 0;
        this.cooldownMax = 0;
        this.toiletPlayer = null;
        setDoorOpen(false);
    }

    public void setDoorOpen(boolean open)
    {
        Block doorBlock = this.getDoorLocation().toLocation().getBlock();

        if (!(doorBlock.getBlockData() instanceof Door))
            return;
        Door door = (Door) doorBlock.getBlockData();
        door.setOpen(open);
        doorBlock.setBlockData(door, true);
        if (!open)
            GameSound.IRON_DOOR_OPEN.play(doorBlock.getLocation(), SoundArea.NEAR_10);
        else
            GameSound.IRON_DOOR_CLOSE.play(doorBlock.getLocation(), SoundArea.NEAR_10);
    }

    public boolean isCooldown()
    {
        return this.state == ToiletState.TOILET_COOLDOWN && this.cooldown > 0;
    }

    public void setCooldown(ToiletState state, int cooldown)
    {
        setCooldown(state, cooldown, cooldown);
    }

    public void setCooldown(ToiletState state, int max, int now)
    {
        if (state != ToiletState.TOILET_COOLDOWN && state != ToiletState.PLAYER_COOLDOWN)
            throw new IllegalArgumentException("state must be TOILET_COOLDOWN or PLAYER_COOLDOWN");

        this.setState(state);
        this.setCooldownMax(max);
        this.setCooldown(now);

        if (state == ToiletState.TOILET_COOLDOWN)
        {
            this.setToiletPlayer(null);
            this.setDoorOpen(false);
        }
    }
}
