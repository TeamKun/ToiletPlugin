package net.kunmc.lab.toiletplugin.game.quest;

import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.events.PlayerToiletJoinEvent;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestLogic implements Listener
{
    private final GameMain game;
    private final QuestManager questManager;

    public QuestLogic(GameMain game, QuestManager questManager)
    {
        this.game = game;
        this.questManager = questManager;
    }

    public void init()
    {
        Bukkit.getPluginManager().registerEvents(this, ToiletPlugin.getPlugin());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        if (player.getKiller() != null)
            return;
        EntityDamageEvent dmgEvt = player.getLastDamageCause();
        if (dmgEvt == null)
            return;
        if (dmgEvt.getCause() != EntityDamageEvent.DamageCause.CUSTOM)
            return;
        if (dmgEvt.getDamage() != 0.11235)
            return;

        GamePlayer gamePlayer = this.game.getPlayerStateManager().getPlayer(player);
        gamePlayer.playSound(GameSound.QUEST_FAILURE);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.setGameMode(GameMode.SPECTATOR);
                player.spigot().respawn();
                gamePlayer.getDisplay().clearBossBar();

                if (!game.getConfig().isRespawnEnable())
                {
                    gamePlayer.setMaxTimeLimit(0);
                    return;
                }

                int respawn = game.getConfig().generateRespawnTime();
                gamePlayer.setMaxTimeLimit(respawn);
            }
        }.runTaskLater(ToiletPlugin.getPlugin(), 5L);
    }

    @EventHandler
    public void onPlayerJoinToilet(PlayerToiletJoinEvent event)
    {
        GamePlayer player = event.getGamePlayer();

        if (player.getQuestPhase() != QuestPhase.STARTED)
            return;

        player.setQuestPhase(
                QuestPhase.TOILET_JOINED,
                this.game.getConfig().getDefecationType().getMessage()
        );

        if (this.game.getConfig().isStopTimerOnJoinToilet())
        {
            player.setMaxTimeLimit(-1);
            player.setTime(-1);
            player.getDisplay().clearBossBar();
        }
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent e)
    {
        if (e.isSneaking() && questManager.getDefecationType() == DefecationType.SHIFT_MASH)
        {
            if (game.getConfig().isStrictDefecation() &&
                    e.getPlayer().getLocation().getBlock().getType() != Material.CAULDRON)
                return;

            GamePlayer gamePlayer = this.game.getPlayerStateManager().getPlayer(e.getPlayer());
            if (gamePlayer.getQuestPhase() != QuestPhase.TOILET_JOINED)
                return;
            gamePlayer.setNowPower(gamePlayer.getNowPower() + questManager.getGain());
            GameSound.TOILETPLAYER_POWER_CHANGE.play(gamePlayer, 0.5F,
                    (gamePlayer.getNowPower() / 100.0F) + 0.8F
            );
        }
    }

    private void onUnnecessaryAction(Player player)
    {
        int amount = this.game.getConfig().getPowerLossOnUnnecessaryActionAmount();
        GamePlayer gamePlayer = this.game.getPlayerStateManager().getPlayer(player);
        if (gamePlayer.getQuestPhase() != QuestPhase.TOILET_JOINED)
            return;
        int currentAmount = gamePlayer.getNowPower();
        if (amount == 0)
            return;
        gamePlayer.setNowPower(Math.max(0, currentAmount - amount));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        if (event.getFrom().getX() == event.getTo().getX() &&
                event.getFrom().getZ() == event.getTo().getZ() &&
                event.getFrom().getY() == event.getTo().getY())
            return;

        onUnnecessaryAction(event.getPlayer());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.PHYSICAL)
            return;

        onUnnecessaryAction(event.getPlayer());
    }

    @EventHandler
    public void onMapInit(MapInitializeEvent event)
    {
        event.getMap().addRenderer(game.getToiletMap());
    }
}
