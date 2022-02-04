package net.kunmc.lab.toiletplugin.game.toilet;

import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ConcurrentModificationException;
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

    public void playerQuitToilet(String toiletName)
    {
        if (!toilets.containsKey(toiletName))
            return;

        OnGroundToilet display = toilets.get(toiletName);
        display.setToiletPlayer(null);
        display.setTimesElapsed(0);
        display.setState(ToiletState.OPEN);
    }

    public void update()
    {
        try
        {
            this.toilets.forEach((key, value) -> {
                this.updateToilet(value);
            });
        }
        catch (ConcurrentModificationException ignored)
        {
        }
    }

    public void updateToilet(OnGroundToilet toilet)
    {
        if (toilet.getInformationArmorStand() == null || toilet.getInformationArmorStand().isDead())
        {
            this.removeToilet(toilet);
            return;
        }

        if (toilet.getToiletPlayer() != null)
            toilet.setTimesElapsed(toilet.getTimesElapsed() + 1);
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
        List<Entity> passengers = display.getInformationArmorStand().getPassengers();

        if (passengers.isEmpty())
            spawnPassengers(display.getInformationArmorStand(), display.getDisplays());

        batchPassenger(display.getInformationArmorStand(), display);
        batchPassenger(display.getInformationArmorStand().getPassengers().get(0), display);
    }

    private void batchPassenger(Entity ae, OnGroundToilet display)
    {
        ToiletState state = display.getState();

        if (!(ae instanceof ArmorStand))
            return;

        ae.getPassengers().forEach(entity -> {
            ArmorStand armorStand = (ArmorStand) entity;
            loop:
            for (String name : entity.getScoreboardTags())
                switch (name)
                {
                    case "info_player_name":
                        if (display.getToiletPlayer() != null)
                        {
                            entity.setCustomNameVisible(true);
                            entity.setCustomName(customName("使用者", display.getToiletPlayer().getName()));
                        }
                        else
                            entity.setCustomNameVisible(false);
                        break loop;
                    case "info_state":
                        entity.setCustomName(customName("状態", state.getDisplayName()));
                        break loop;
                    case "info_times_elapsed":
                        if (display.getTimesElapsed() == 0)
                        {
                            armorStand.setCustomNameVisible(false);
                            break loop;
                        }

                        armorStand.setCustomNameVisible(true);
                        entity.setCustomName(customName("経過時間", formatDateTime(display.getTimesElapsed())));
                        break loop;
                    case "info_remaining_time":
                        if (display.getCooldownMax() == 0)
                        {
                            armorStand.setCustomNameVisible(false);
                            break loop;
                        }

                        int remainingTime = display.getCooldownMax() - display.getTimesElapsed();

                        if (remainingTime <= 0)
                        {
                            armorStand.setCustomNameVisible(false);
                            break loop;
                        }

                        armorStand.setCustomNameVisible(true);
                        entity.setCustomName(customName("残り時間", formatDateTime(remainingTime)));
                        break loop;
                    case "info_time_display":
                        if (display.getCooldownMax() == 0)
                        {
                            armorStand.setCustomNameVisible(false);
                            break loop;
                        }

                        // Progress bar
                        int remainingTimeDisplay = display.getCooldownMax() - display.getTimesElapsed();
                        int maxTimeDisplay = display.getCooldownMax() / 10;
                        int progress = (int) (((double) remainingTimeDisplay / (double) maxTimeDisplay) * 10);

                        StringBuilder builder = new StringBuilder("[");
                        for (int i = 0; i < 10; i++)
                            if (i < progress)
                                builder.append(ChatColor.RED).append("|");
                            else
                                builder.append(ChatColor.GREEN).append("|");
                        builder.append(ChatColor.GREEN).append("]");
                        entity.setCustomName(customName("残り時間", builder.toString()));
                        break loop;
                }

            batchPassenger(entity, display);
        });
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

    private void spawnPassengers(ArmorStand infoStand, List<Entity> displays)
    {
        Entity nameTag = spawnPassenger(infoStand, "player_name");
        infoStand.addPassenger(nameTag);
        displays.add(nameTag);

        Entity stateTag = spawnPassenger(infoStand, "state");
        nameTag.addPassenger(stateTag);
        displays.add(stateTag);

        Entity timeElapsedTag = spawnPassenger(infoStand, "times_elapsed");
        stateTag.addPassenger(timeElapsedTag);
        displays.add(timeElapsedTag);

        Entity remainingTimeTag = spawnPassenger(infoStand, "remaining_time");
        timeElapsedTag.addPassenger(remainingTimeTag);
        displays.add(remainingTimeTag);

        Entity timeDisplayTag = spawnPassenger(infoStand, "time_display");
        remainingTimeTag.addPassenger(timeDisplayTag);
        displays.add(timeDisplayTag);
    }

    private ArmorStand spawnPassenger(ArmorStand infoStand, String name)
    {
        ArmorStand stand = infoStand.getWorld().spawn(infoStand.getLocation(), ArmorStand.class);
        stand.setCustomNameVisible(true);
        stand.addScoreboardTag("registered_toilet");
        stand.addScoreboardTag("info_" + name);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setSmall(true);
        return stand;
    }

}
