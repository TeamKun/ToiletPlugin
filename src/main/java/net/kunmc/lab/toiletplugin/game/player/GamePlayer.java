package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kunmc.lab.toiletplugin.game.sound.GameSound;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletState;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class GamePlayer
{
    @Getter
    private final Player player;
    @Getter
    private final PlayerDisplay display;

    @Getter
    @Setter
    private PlayState state;

    @Getter
    private int maxTimeLimit;
    @Getter
    @Setter
    private int time;

    @Getter
    private QuestPhase questPhase;

    @Getter
    @Setter
    private int nowPower;
    @Getter
    @Setter
    private int maxPoop;
    @Getter
    @Setter
    private int nowPoop;
    @Getter
    @Setter
    private int nowCount;

    @Getter
    private OnGroundToilet toilet;

    @Getter
    @Setter
    private long toiletJoinedIn;

    public GamePlayer(Player player, GameMain game)
    {
        this.player = player;
        this.display = new PlayerDisplay(this, game);
        this.questPhase = QuestPhase.NONE;
        this.maxTimeLimit = -1;
        this.time = -1;
        this.toiletJoinedIn = -1;
        this.toilet = null;
        this.nowPoop = 0;
        this.nowPower = 0;
        this.maxPoop = 0;
        this.nowCount = 0;
    }

    public void setToilet(OnGroundToilet toilet)
    {
        if (toilet != null)
            this.toiletJoinedIn = System.currentTimeMillis();
        this.toilet = toilet;
    }

    public void setMaxTimeLimit(int maxTimeLimit)
    {
        this.maxTimeLimit = maxTimeLimit;
        this.time = maxTimeLimit;
    }

    public void setQuestPhase(QuestPhase state, int time)
    {
        setMaxTimeLimit(time);
        setQuestPhase(state);
    }

    public void setQuestPhase(QuestPhase state)
    {
        this.setQuestPhase(state, "");
    }

    public void setQuestPhase(QuestPhase state, String customStateMessage)
    {
        this.questPhase = state;

        if (this.questPhase == QuestPhase.NONE || this.questPhase == QuestPhase.SCHEDULED)
        {
            player.stopSound(GameSound.QUESTING_OPPRESSIVE.getName());
            return;
        }

        if (state == QuestPhase.STARTED)
            display.questStarted();
        else
        {
            display.showQuestTitle(state, customStateMessage);
            if (state.isSubQuest())
                this.playSound(GameSound.QUEST_PHASE_COMPLETE);
        }
    }

    public boolean isQuesting()
    {
        return this.questPhase != QuestPhase.NONE &&
                this.questPhase != QuestPhase.SCHEDULED;
    }

    public boolean isScheduled()
    {
        return this.questPhase != QuestPhase.SCHEDULED;
    }

    public boolean isPlaying()
    {
        return this.state == PlayState.PLAYING;
    }

    public boolean isSpectating()
    {
        return this.state == PlayState.SPECTATING;
    }

    public void purge()
    {
        this.maxTimeLimit = 0;
        this.time = 0;
        this.questPhase = QuestPhase.NONE;
    }

    public void resetPlayerForQuest()
    {
        this.nowPower = 0;
        this.nowPoop = 0;
        this.maxPoop = 0;
        this.nowCount = 0;
    }

    // Logic

    public void setPlayState(PlayState state)
    {
        switch (state)
        {
            case PLAYING:
                this.player.sendMessage(ChatColor.GREEN + "ゲームに参加しました！");
                break;
            case SPECTATING:
                if (this.state == PlayState.PLAYING)
                    this.player.sendMessage(ChatColor.RED + "ゲームから退出しました！");
                this.player.sendMessage(ChatColor.GREEN + "観戦モードになりました！");

                this.questPhase = QuestPhase.NONE;
                break;
        }

        this.state = state;
    }

    public void playSound(GameSound sound)
    {
        sound.play(this.player);
    }

    public void playSound(GameSound sound, Location location)
    {
        sound.play(this.player, location);
    }

    public void stopSound(GameSound sound)
    {
        sound.stop(this.player);
    }

    public void doDefecation()
    {
        Location soundLoc = this.player.getLocation().clone();
        soundLoc.setX(soundLoc.getX() - 10);

        playSound(GameSound.POOP_THROW, soundLoc);

        ItemStack stack = new ItemStack(Material.COCOA_BEANS);

        Location poopLoc = this.player.getLocation().clone();
        poopLoc.setY(poopLoc.getY() + 0.3);

        Item itemEntity = this.player.getWorld().dropItem(poopLoc, stack);

        itemEntity.setCustomNameVisible(true);
        itemEntity.customName(Component.text(this.player.getName() + "のうんこ"));
        itemEntity.getPersistentDataContainer().set(new NamespacedKey(ToiletPlugin.getPlugin(), "poop_item"), PersistentDataType.STRING, "poop");


        Location fireWorksLoc = this.player.getLocation().clone();
        fireWorksLoc.setY(fireWorksLoc.getY() + 7);
        Firework fw = (Firework) this.player.getWorld().spawnEntity(fireWorksLoc, EntityType.FIREWORK);
        FireworkMeta fm = fw.getFireworkMeta();

        fm.setPower(2);
        fm.addEffect(FireworkEffect.builder()
                .withColor(Color.AQUA)
                .with(FireworkEffect.Type.BALL_LARGE)
                .flicker(true).build());

        fw.setFireworkMeta(fm);

        new BukkitRunnable()
        {
            private int count = 0;

            @Override
            public void run()
            {
                fw.detonate();
                if (++count >= 2)
                {
                    stopSound(GameSound.POOP_WATER_LAND);
                    this.cancel();
                    return;
                }
                playSound(GameSound.POOP_WATER_LAND, soundLoc);

            }
        }.runTaskTimer(ToiletPlugin.getPlugin(), 0, 20);
    }

    public void setCooldown(int cooldown)
    {
        this.maxTimeLimit = cooldown;
        this.time = cooldown;

        this.setQuestPhase(QuestPhase.PLAYER_COOLDOWN);

        if (this.toilet != null)
            this.toilet.setCooldown(ToiletState.PLAYER_COOLDOWN, cooldown);
    }
}
