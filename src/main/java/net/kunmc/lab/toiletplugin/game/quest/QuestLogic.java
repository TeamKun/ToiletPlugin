package net.kunmc.lab.toiletplugin.game.quest;

import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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
    @SuppressWarnings("deprecation")
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

        Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " は便意に耐えられず死んでしまった！");
        this.questManager.reWait(player);
    }
}
