package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import org.bukkit.Bukkit;
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
    }

    public void killEntities()
    {
        displays.forEach(Entity::remove);
    }

}
