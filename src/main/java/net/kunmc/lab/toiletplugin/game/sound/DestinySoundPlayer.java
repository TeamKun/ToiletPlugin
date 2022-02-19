package net.kunmc.lab.toiletplugin.game.sound;

import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.game.player.PlayerManager;
import net.kunmc.lab.toiletplugin.game.quest.QuestPhase;
import net.kunmc.lab.toiletplugin.utils.DirectionUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DestinySoundPlayer extends BukkitRunnable
{
    private final PlayerManager playerManager;
    private final ToiletPlugin plugin;

    private int tick;

    public DestinySoundPlayer(ToiletPlugin plugin, PlayerManager playerManager)
    {
        this.playerManager = playerManager;
        this.plugin = plugin;
        this.tick = 0;
    }

    public void init()
    {
        this.runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void run()
    {
        if (!plugin.getGame().getConfig().isQuestingOppressiveSoundEnable())
            return;
        if (tick > 100)
            tick = 0;
        tick += 10;
        playerManager.getGamePlayers().values().stream()
                .filter(GamePlayer::isQuesting)
                .filter(p -> p.getQuestPhase() != QuestPhase.PLAYER_COOLDOWN)
                .forEach(this::playSound);
    }

    private void playSound(GamePlayer gamePlayer)
    {
        double time = (double) gamePlayer.getTime() / (double) gamePlayer.getMaxTimeLimit();

        Location location = calculatePlayLocation(gamePlayer.getPlayer());

        if ((time < 0.3 && (tick == 20 || tick == 60 || tick == 80)) ||
                (time < 0.5 && (tick == 10 || tick == 60) ||
                        (time < 1.0 && tick == 10)))
        {
            GameSound.QUESTING_OPPRESSIVE.play(gamePlayer.getPlayer(), location, 0.6F, 0.1F);
            GameSound.QUESTING_OPPRESSIVE.play(gamePlayer.getPlayer(), location, 0.6F, 2.0F);
        }

        GameSound.QUESTING_OPPRESSIVE.play(gamePlayer.getPlayer(), location, 0.6F, 0.1F);
    }

    public Location calculatePlayLocation(Player player)
    {
        Location location = player.getLocation();
        double speed = player.getVelocity().length();
        if (speed < 0.1)
            return location;

        BlockFace blockFace = DirectionUtils.getDirection(location.getYaw());

        return DirectionUtils.getDirLoc(location, 10, blockFace);
    }
}
