package net.kunmc.lab.toiletplugin.game.toilet;

import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InformationDisplay
{
    private static final String[] displays = {
            ChatColor.YELLOW + ChatColor.BOLD.toString() + "TOILET!!",
            ChatColor.WHITE + ChatColor.BOLD.toString() + "TOILET!!"
    };
    private final GameMain game;
    private final HashMap<String, OnGroundToilet> toilets;

    private static final String[] availableDisplays = {
            "player_name",
            "state",
            "times_elapsed",
            "remaining_time",
            "time_display"
    };

    public InformationDisplay(GameMain game, ToiletManager toiletManager)
    {
        this.game = game;
        this.toilets = toiletManager.getToilets();
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void init()
    {
        game.getToiletManager().getToiletList().forEach(toilet -> this.toilets.put(toilet.getName(), new OnGroundToilet(toilet)));
    }

    public void removeToilet(OnGroundToilet toilet)
    {
        toilet.killEntities();
        this.toilets.remove(toilet.getToilet().getName());
    }

    public void playerJoinToilet(Player player, String toiletName)
    {
        if (!toilets.containsKey(toiletName))
            return;

        OnGroundToilet display = toilets.get(toiletName);
        display.setToiletPlayer(player);
        display.setState(ToiletState.PLAYER_USING);
    }


    public void updateToilet(OnGroundToilet toilet)
    {
        if (toilet.getInformationArmorStand() == null || toilet.getInformationArmorStand().isDead())
        {
            this.removeToilet(toilet);
            return;
        }

        ArmorStand infoStand = toilet.getInformationArmorStand();
        if (infoStand == null)
            return;

        if (toilet.getDisplayNonce() >= displays.length)
            toilet.setDisplayNonce(0);
        infoStand.setCustomName(displays[toilet.getDisplayNonce()]);
        toilet.setDisplayNonce(toilet.getDisplayNonce() + 1);

        writeToiletInfoPassengers(toilet);
    }

    private void writeToiletInfoPassengers(OnGroundToilet display)
    {
        List<ArmorStand> displays = display.getDisplays();

        if (displays.isEmpty() || displays.size() != availableDisplays.length)
        {
            displays.forEach(ArmorStand::remove);
            displays.clear();
            displays.addAll(spawnPassengers(display.getInformationArmorStand()));
        }

        batchPassenger(displays, display);
    }

    private void batchPassenger(List<ArmorStand> entities, OnGroundToilet display)
    {
        ToiletState state = display.getState();
        int displayCount = 0;

        entities.forEach(e -> e.setCustomNameVisible(false));

        for (String name : availableDisplays)
        {
            ArmorStand armorStand = entities.get(displayCount);

            switch (name)
            {
                case "player_name":
                    if (display.getToiletPlayer() != null)
                    {
                        armorStand.setCustomNameVisible(true);
                        armorStand.setCustomName(customName("使用者", display.getToiletPlayer().getPlayer().getName()));
                        displayCount++;
                    }
                    break;
                case "state":
                    armorStand.setCustomNameVisible(true);
                    armorStand.setCustomName(customName("状態", state.getDisplayName()));
                    displayCount++;
                    break;
                case "times_elapsed":
                    if (display.getTimesElapsed() == 0)
                        break;
                    armorStand.setCustomNameVisible(true);
                    armorStand.setCustomName(customName("経過時間", formatDateTime(display.getTimesElapsed())));
                    displayCount++;
                    break;
                case "remaining_time":
                    if (display.getCooldownMax() == 0 || display.getCooldown() == 0)
                        break;


                    armorStand.setCustomNameVisible(true);
                    armorStand.setCustomName(customName("残り時間", formatDateTime(display.getCooldown())));
                    displayCount++;
                    break;
                case "time_display":
                    if (display.getCooldownMax() == 0 || display.getCooldown() == 0)
                        break;
                    StringBuilder bar = new StringBuilder("[");

                    int progress = ((100 * display.getCooldown()) / display.getCooldownMax()) / 10;
                    for (int i = 10; i > 0; i--)
                    {
                        if (i <= progress)
                            bar.append(ChatColor.RED).append("░");
                        else
                            bar.append(ChatColor.GREEN).append("█");
                    }

                    bar.append(ChatColor.RESET).append("]");

                    armorStand.setCustomNameVisible(true);
                    armorStand.setCustomName(bar.toString());
                    displayCount++;
            }
        }

    }

    private String formatDateTime(int seconds)
    {
        return formatter.format(LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC));
    }

    private String customName(String key, String value)
    {
        return ChatColor.WHITE + key +
                ChatColor.GRAY + ": " +
                ChatColor.GREEN + value;
    }

    private List<ArmorStand> spawnPassengers(ArmorStand infoStand)
    {
        List<ArmorStand> displays = new ArrayList<>();

        ArmorStand nameTag = spawnPassenger(infoStand);
        infoStand.addPassenger(nameTag);
        displays.add(nameTag);

        ArmorStand stateTag = spawnPassenger(infoStand);
        nameTag.addPassenger(stateTag);
        displays.add(stateTag);

        ArmorStand timeElapsedTag = spawnPassenger(infoStand);
        stateTag.addPassenger(timeElapsedTag);
        displays.add(timeElapsedTag);

        ArmorStand remainingTimeTag = spawnPassenger(infoStand);
        timeElapsedTag.addPassenger(remainingTimeTag);
        displays.add(remainingTimeTag);

        ArmorStand timeDisplayTag = spawnPassenger(infoStand);
        remainingTimeTag.addPassenger(timeDisplayTag);
        displays.add(timeDisplayTag);

        return displays;
    }

    private ArmorStand spawnPassenger(ArmorStand infoStand)
    {
        ArmorStand stand = infoStand.getWorld().spawn(infoStand.getLocation(), ArmorStand.class);
        stand.setCustomNameVisible(false);
        stand.addScoreboardTag("registered_toilet");
        stand.addScoreboardTag("info_toilet");
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setSmall(true);
        return stand;
    }

}
