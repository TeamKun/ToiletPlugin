package net.kunmc.lab.toiletplugin.game.player;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class PlayerManager extends BukkitRunnable implements Listener
{
    @Getter
    private final HashMap<Player, GamePlayer> gamePlayers;

    private final GameMain game;

    public PlayerManager(GameMain game)
    {
        this.gamePlayers = new HashMap<>();
        this.game = game;
    }

    public List<GamePlayer> getPlayers()
    {
        return new ArrayList<>(this.gamePlayers.values());
    }

    public GamePlayer getPlayer(Player player)
    {
        return this.gamePlayers.get(player);
    }

    private NamespacedKey KEY;

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        this.gamePlayers.put(e.getPlayer(), new GamePlayer(e.getPlayer(), game));

        this.updatePlayer(e.getPlayer());
    }

    public void init()
    {
        Bukkit.getPluginManager().registerEvents(this, ToiletPlugin.getPlugin());
        this.runTaskTimerAsynchronously(ToiletPlugin.getPlugin(), 0, 10);

        this.KEY = new NamespacedKey(ToiletPlugin.getPlugin(), "hud_entity");
        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new HUDPacketFilter());
    }
    private final Class<?>[] validEntities = new Class<?>[]{
            Slime.class,
            ArmorStand.class,
    };

    @EventHandler
    public void onDismount(EntityDismountEvent e)
    {
        passDismount(e, e.getEntity(), e.getDismounted());
    }

    public void passDismount(Cancellable event, Entity vehicle, Entity passenger)
    {
        if (Arrays.stream(validEntities)
                .parallel()
                .noneMatch(((Predicate<Class<?>>) aClass -> aClass.isInstance(vehicle))
                        .or(aClass -> aClass.isInstance(passenger))))
            return;
        if (passenger.getPersistentDataContainer().has(KEY, PersistentDataType.STRING)
                || vehicle.getPersistentDataContainer().has(KEY, PersistentDataType.STRING))
            event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        this.gamePlayers.remove(e.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e)
    {
        this.updatePlayer(e.getPlayer(), e.getNewGameMode());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickUpPoop(PlayerAttemptPickupItemEvent e)
    {
        if (e.getItem().getPersistentDataContainer().has(
                new NamespacedKey(ToiletPlugin.getPlugin(), "poop_item"),
                PersistentDataType.STRING
        ))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e)
    {
        getPlayer(e.getEntity()).onDeath();
    }

    public boolean isPlaying(Player player)
    {
        return getPlayer(player).isPlaying();
    }

    public boolean isSpectating(Player player)
    {
        return getPlayer(player).isSpectating();
    }

    public void updatePlayer(Player player)
    {
        this.updatePlayer(player, player.getGameMode());
    }

    public void updatePlayer(Player player, GameMode mode)
    {
        GamePlayer gamePlayer = this.gamePlayers.get(player);

        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE)
        {
            gamePlayer.setPlayState(PlayState.PLAYING);

            if (this.game.getConfig().isAutoScheduleOnJoin())
                gamePlayer.setQuestPhase(QuestPhase.SCHEDULED, this.game.getConfig().generateScheduleTime());
        }
        else
        {
            if (gamePlayer.isQuesting())
                this.game.getQuestManager().cancel(player, true);
            else if (gamePlayer.isScheduled())
                this.game.getQuestManager().unSchedule(player);


            gamePlayer.purge();
            gamePlayer.setPlayState(PlayState.SPECTATING);
        }
    }

    @Override
    public void run()
    {
        this.gamePlayers.forEach((player, gamePlayer) -> gamePlayer.getDisplay().updateScreen());
    }
}
