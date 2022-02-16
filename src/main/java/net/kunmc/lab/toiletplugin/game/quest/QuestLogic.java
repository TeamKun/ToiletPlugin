package net.kunmc.lab.toiletplugin.game.quest;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.events.PlayerToiletJoinEvent;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

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

        game.getPlayerStateManager().getPlayer(player).playSound(GameSound.QUEST_FAILURE);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent event)
    {
        if (this.game.getConfig().isAutoRescheduleOnRespawn())
            this.questManager.reSchedule(event.getPlayer());
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
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent e)
    {
        if (e.isSneaking() && this.game.getConfig().getDefecationType() == DefecationType.SHIFT_MASH)
        {
            int gain = this.game.getConfig().getPowerGainAmount();
            GamePlayer gamePlayer = this.game.getPlayerStateManager().getPlayer(e.getPlayer());
            gamePlayer.setNowPower(gamePlayer.getNowPower() + gain);
            GameSound.TOILETPLAYER_POWER_CHANGE.play(gamePlayer, 0.5F,
                    (gamePlayer.getNowPower() / 100.0F) + 0.8F
            );
        }
    }
}
