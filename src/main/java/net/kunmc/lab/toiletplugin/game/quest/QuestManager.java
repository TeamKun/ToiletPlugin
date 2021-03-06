package net.kunmc.lab.toiletplugin.game.quest;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.config.GameConfig;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.player.PlayerManager;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Random;

public class QuestManager extends BukkitRunnable
{
    private final GameMain game;
    private final QuestLogic logic;
    private final PlayerManager stateManager;

    private Team glowColorTeam;

    public QuestManager(GameMain game)
    {
        this.game = game;
        this.logic = new QuestLogic(game, this);

        this.stateManager = game.getPlayerStateManager();
    }

    private int secCount = 0;

    private int accept;
    private int maxCount;

    public boolean isQuesting(Player player)
    {
        return this.stateManager.getPlayer(player).isQuesting();
    }

    public boolean isScheduled(Player player)
    {
        return this.stateManager.getPlayer(player).getQuestPhase() == QuestPhase.SCHEDULED;
    }

    public int reSchedule(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (info.getQuestPhase() != QuestPhase.NONE)
            return -1;

        int scheduleTime = this.game.getConfig().generateScheduleTime();

        info.setQuestPhase(QuestPhase.SCHEDULED, scheduleTime);
        return scheduleTime;
    }

    public boolean unSchedule(Player player)
    {
        GamePlayer info = this.stateManager.getPlayer(player);
        if (info.getQuestPhase() != QuestPhase.SCHEDULED)
            return false;
        info.setQuestPhase(QuestPhase.NONE, 0);
        return true;
    }

    public int changeScheduledTime(Player player, int time)
    {
        GamePlayer info = this.stateManager.getPlayer(player);
        if (!info.isPlaying())
            return -1;

        if (info.isQuesting())
            return -1;

        info.setQuestPhase(QuestPhase.SCHEDULED, time);
        return time;
    }

    public int changeScheduledTime(Player player)
    {
        return this.changeScheduledTime(player, this.game.getConfig().generateScheduleTime());
    }

    public Integer getScheduledTime(Player player)
    {
        return getTime(player);
    }

    public Integer getQuestTime(Player player)
    {
        return getTime(player);
    }

    private Integer getTime(Player player)
    {
        return this.stateManager.getPlayer(player).getTime();
    }

    private void questFailGeneral(GamePlayer gamePlayer)
    {
        Player player = gamePlayer.getPlayer();

        if (gamePlayer.getToilet() != null)  // TODO: Refactor: playerLeftToilet
            game.getToiletManager().getLogic().playerLeftToilet(gamePlayer.getPlayer(), gamePlayer.getToilet());

        if (game.getConfig().isPlaceLavaOnFail())
        {
            Block block = player.getLocation().getBlock();
            block.setType(Material.LAVA);
        }

        if (game.getConfig().isSpreadPoopOnFail())
            spreadPoops(gamePlayer);

        player.setKiller(null);
        player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, 0.11235));
        player.setHealth(0d);
        gamePlayer.setQuestPhase(QuestPhase.NONE, 0);

        player.removePotionEffect(PotionEffectType.GLOWING);
        glowColorTeam.removeEntry(player.getName());

        gamePlayer.resetPlayerForQuest();
    }

    private void spreadPoops(GamePlayer player)
    {
        Location location = player.getPlayer().getLocation();

        Random random = new Random();

        for (int i = 0; i < game.getConfig().getSpreadPoopAmount(); i++)
        {
            Item poop = player.spawnPoopItem(location);
            Utils.setPoopVelocityRandom(poop, random.nextInt(15), game.getConfig().getSpreadPoopDistanceMax());
        }
    }

    @SuppressWarnings("deprecation")
    public void onPlayerFailedQuest(GamePlayer gamePlayer)
    {
        this.questFailGeneral(gamePlayer);
        Bukkit.broadcastMessage(ChatColor.RED + gamePlayer.getPlayer().getName() +
                Utils.convertExplict(gamePlayer.getGame(), " ???????????????????????????????????????????????????", " ??????????????????????????????????????????"));
    }

    @SuppressWarnings("deprecation")
    public void onBurst(GamePlayer player)
    {
        Player p = player.getPlayer();
        p.getWorld().createExplosion(p.getLocation(), 0F);


        this.questFailGeneral(player);
        Bukkit.broadcastMessage(ChatColor.RED + p.getName() + " ???????????????????????????????????????????????????");
    }

    @Getter
    private int gain;
    private int loss;

    public int start(Player player)
    {
        return start(player, this.game.getConfig().generateQuestTime());
    }

    public int start(Player player, int questTime)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isPlaying())
            return -1;

        if (info.isQuesting())
            return -1;

        info.resetPlayerForQuest();

        int maxPoop = this.game.getConfig().generatePoopAmount();

        info.playSound(GameSound.QUEST_START);
        info.setQuestPhase(QuestPhase.STARTED, questTime);
        info.setMaxPoop(maxPoop);

        if (this.game.getConfig().isBroadcastOnQuestStart())
            Bukkit.getOnlinePlayers().stream().parallel()
                    .filter(p -> p.equals(player))
                    .forEach(p -> p.sendMessage(ChatColor.DARK_RED + player.getName() + "???????????????????????????..."));
        player.sendMessage(ChatColor.DARK_RED + "????????????????????????????????????... ");
        player.sendMessage(ChatColor.RED + "????????????" + questTime + "????????????" +
                Utils.convertExplict(this.game, "?????????", "??????") + "????????????????????????????????????");

        if (this.game.getConfig().isGlowingOnQuesting())
        {
            if (this.game.getConfig().isGlowingOnQuestingColorEnable())
                this.glowColorTeam.addEntry(player.getName());
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, true, false));
        }

        if (this.game.getConfig().isGiveToiletMap())
            player.getInventory().addItem(new ItemStack(Material.MAP));
        return questTime;
    }

    public int cancel(Player player, boolean isNever)
    {
        GamePlayer info = this.stateManager.getPlayer(player);

        if (!info.isQuesting())
            return -1;

        if (info.getQuestPhase() != QuestPhase.STARTED)
        {
            info.getToilet().purge();
            info.setToilet(null);
        }

        info.setQuestPhase(QuestPhase.NONE, 0);

        player.sendMessage(ChatColor.GREEN + "????????????????????????????????????????????????????????????");
        if (!isNever)
            return this.changeScheduledTime(player);

        info.playSound(GameSound.QUEST_CANCEL);

        info.getDisplay().onQuestCancelled();

        player.removePotionEffect(PotionEffectType.GLOWING);
        glowColorTeam.removeEntry(player.getName());

        return 0;
    }

    public void onPlayerQuestFinish(GamePlayer player)
    {
        player.purge();

        if (this.game.getConfig().isAutoRescheduleOnSuccess())
            this.reSchedule(player.getPlayer());

        player.getDisplay().onQuestFinished();
    }

    @Getter
    private DefecationType defecationType;
    private boolean burst;

    public void init()
    {
        this.logic.init();
        this.runTaskTimer(ToiletPlugin.getPlugin(), 0, 10);
        this.updateConfig();

        if ((this.glowColorTeam =
                game.getPlugin().getPluginScoreboard().getTeam("toilet_QstGlower")) == null)
            this.glowColorTeam = game.getPlugin().getPluginScoreboard().registerNewTeam("toilet_QstGlower");

        this.glowColorTeam.color(NamedTextColor.RED);
    }

    private void updateConfig()
    {
        GameConfig config = this.game.getConfig();

        gain = config.getPowerGainAmount();
        loss = config.getPowerLossOnSecAmount();
        defecationType = config.getDefecationType();
        burst = config.isEnablePowerBurst();
        accept = config.getDefecationNeedPower();
        maxCount = config.getPowerKeepCountSeconds();
    }

    private void pendingQuestTick(Player player, GamePlayer gamePlayer)
    {
        if (gamePlayer.getQuestPhase() != QuestPhase.TOILET_JOINED)
            return;

        if (gamePlayer.getNowPower() >= 100 && burst)
        {
            game.getQuestManager().onBurst(gamePlayer);
            return;
        }

        if (defecationType == DefecationType.SHIFT_HOLD && player.isSneaking())
        {
            if (game.getConfig().isStrictDefecation() &&
                    player.getLocation().getBlock().getType() != Material.CAULDRON)
                return;

            gamePlayer.setNowPower(Math.min(gamePlayer.getNowPower() + gain, 100));
            GameSound.TOILETPLAYER_POWER_CHANGE.play(player, 0.5F,
                    (gamePlayer.getNowPower() / 100.0F) + 0.6F
            );
        }
        else if (gamePlayer.getNowPower() > 0)
            gamePlayer.setNowPower(Math.max(0, gamePlayer.getNowPower() - loss));

    }

    private void questComplete(GamePlayer player)
    {
        player.getPlayer().getInventory().remove(Material.MAP);
        player.getPlayer().getInventory().remove(Material.FILLED_MAP);
        player.getDisplay().clearPowerBossBar();
        player.getDisplay().showTimeBossBar(); // Patch of stopTimerOnJoinToilet
        if (this.game.getConfig().isPlayerCooldownEnable())
            player.setCooldown(this.game.getConfig().generatePlayerCooldownTime());
        else
            player.setCooldown(1);
        player.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
        glowColorTeam.removeEntry(player.getPlayer().getName());
    }

    private boolean acceptQuestTick(GamePlayer player)
    {
        int nowCount = player.getNowCount();

        if (nowCount < maxCount)
            player.setNowCount(nowCount + 1);
        else
        {
            player.doDefecation();
            player.setNowPoop(player.getNowPoop() + 1);
            player.setNowCount(0);
            player.setNowPower(player.getNowPower() - accept);
            if (player.getNowPoop() >= player.getMaxPoop())
            {
                this.questComplete(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public void run()
    {
        this.game.getPlayerStateManager().getGamePlayers().forEach(this::pendingQuestTick);

        if (++secCount != 2)
            return;

        this.stateManager.getPlayers().forEach(info -> {
            Player player = info.getPlayer();

            if (!info.isPlaying())
                return;

            if (info.getQuestPhase() == QuestPhase.NONE)
                return;

            if (info.getQuestPhase() == QuestPhase.TOILET_JOINED)
            {
                if (info.getNowPower() >= accept)
                {
                    if (acceptQuestTick(info))
                        return;
                }
                else
                    info.setNowCount(0);
            }

            if (info.getTime() == 0)
            {
                if (info.getQuestPhase() == QuestPhase.PLAYER_COOLDOWN)
                {
                    onPlayerQuestFinish(info);
                    return;
                }

                if (info.isQuesting())
                    this.onPlayerFailedQuest(info);
                else
                    this.start(player);
            }

            info.setTime(info.getTime() - 1);
        });

        updateConfig();
        secCount = 0;
    }
}
