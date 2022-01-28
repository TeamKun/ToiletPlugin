package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.toiletobject.Toilet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class InformationDisplay
{
    private static final String[] displays = {
            ChatColor.YELLOW + ChatColor.BOLD.toString() + "TOILET!!",
            ChatColor.WHITE + ChatColor.BOLD.toString() + "TOILET!!"
    };
    private final GameMain game;
    private final HashMap<String, ToiletDisplay> toilets;

    public InformationDisplay(GameMain game)
    {
        this.game = game;
        this.toilets = new HashMap<>();
    }

    public void init()
    {
        game.getRegister().getToiletList().forEach(toilet -> {
            this.toilets.put(toilet.getName(), new ToiletDisplay(toilet));
        });
    }

    public void addToilet(Toilet toilet)
    {
        this.toilets.put(toilet.getName(), new ToiletDisplay(toilet));
    }

    public void playerJoinToilet(Player player, String toiletName)
    {
        if (!toilets.containsKey(toiletName))
            return;

        ToiletDisplay display = toilets.get(toiletName);
        display.setInToiletPlayer(player);
    }

    public void playerQuitToilet(String toiletName)
    {
        if (!toilets.containsKey(toiletName))
            return;

        ToiletDisplay display = toilets.get(toiletName);
        display.setInToiletPlayer(null);
    }

    public void update()
    {
        this.toilets.forEach((key, value) -> {
            this.updateToilet(value);
        });
    }

    public void updateToilet(ToiletDisplay toilet)
    {
        if (toilet.getInToiletPlayer() != null)
            toilet.setTimesElapsed(toilet.getTimesElapsed() + 1);
        ArmorStand infoStand = toilet.getInformationArmorStand();
        if (infoStand == null)
            return;

        if (toilet.getDisplayNonce() >= displays.length)
            toilet.setDisplayNonce(0);
        infoStand.setCustomName(displays[toilet.getDisplayNonce()]);
        toilet.setDisplayNonce(toilet.getDisplayNonce() + 1);
    }

    @Getter
    @Setter
    private static class ToiletDisplay
    {
        @Getter
        private final Toilet toilet;
        @Getter
        private final ArmorStand informationArmorStand;

        private Player inToiletPlayer;
        private int timesElapsed;
        private int displayNonce;

        public ToiletDisplay(Toilet toilet)
        {
            this.toilet = toilet;
            this.informationArmorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(toilet.getToiletInfoBaseArmorStandUUID()));
            this.inToiletPlayer = null;
        }


    }
}
