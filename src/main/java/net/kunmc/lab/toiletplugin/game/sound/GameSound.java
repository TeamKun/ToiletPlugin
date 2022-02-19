package net.kunmc.lab.toiletplugin.game.sound;

import lombok.Getter;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;

public enum GameSound
{
    TOILET_GENERATE("block.anvil.destroy", Sound.Source.BLOCK, 1.0F, 1.0F),

    IRON_DOOR_OPEN("block.iron_door.open", Sound.Source.BLOCK, 1.0F, 1.0F),
    IRON_DOOR_CLOSE("block.iron_door.close", Sound.Source.BLOCK, 1.0F, 1.0F),

    QUEST_START("block.end_portal.spawn", Sound.Source.MASTER, 0.1F, 0.7F),
    QUEST_PHASE_COMPLETE("entity.player.levelup", Sound.Source.MASTER, 0.3F, 2.0F),
    QUEST_COMPLETE("entity.player.levelup", Sound.Source.MASTER, 0.5F, 0.3F),
    QUEST_FAILURE("entity.player.hurt_on_fire", Sound.Source.MASTER, 1.0F, 1.0F),
    QUEST_CANCEL("entity.player.levelup", Sound.Source.MASTER, 0.5F, 2.0F),

    QUESTING_OPPRESSIVE("block.beacon.ambient", Sound.Source.MASTER, 0.5F, 1.0F),

    TOILETPLAYER_POWER_CHANGE("block.note_block.bass", Sound.Source.MASTER, 0.5F, 1.0F),

    POOP_THROW("entity.arrow.shoot", Sound.Source.MASTER, 0.5F, 0.6F),
    POOP_WATER_LAND("entity.generic.splash", Sound.Source.MASTER, 0.3F, 1.2F),
    ;
    @Getter
    private final String name;
    @Getter
    private final Sound.Source source;
    @Getter
    private final float volume;
    @Getter
    private final float pitch;

    GameSound(String name, Sound.Source source, float volume, float pitch)
    {
        this.name = "minecraft:" + name;
        this.source = source;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void play(Player player, Location location, SoundArea area, float volume, float pitch)
    {
        if (area == SoundArea.SELF)
        {
            player.playSound(location, this.name, volume, pitch);
            return;
        }

        this.play(location, area, volume, pitch);
    }

    public void play(Player player, SoundArea area, float volume, float pitch)
    {
        this.play(player, player.getLocation(), area, volume, pitch);
    }

    public void play(Player player, Location location, float volume, float pitch)
    {
        this.play(player, location, SoundArea.SELF, volume, pitch);
    }

    public void play(Location location, SoundArea area, float volume, float pitch)
    {
        switch (area)
        {
            case SERVER_ALL:
                this.playAll(location.getWorld(), volume, pitch);
            case NEAR_10:
                this.playSoundNearBy(location, 10, volume, pitch);
                break;
            case NEAR_5:
                this.playSoundNearBy(location, 5, volume, pitch);
                break;
            case NEAR_3:
                this.playSoundNearBy(location, 3, volume, pitch);
                break;
            case SELF:
                throw new RuntimeException(new OperationNotSupportedException("SoundArea.SELF is not supported"));
        }
    }

    private void playAll(World world, float volume, float pitch)
    {
        world.playSound(Sound.sound(Key.key(this.name), this.source, this.volume, this.pitch));
    }

    public void play(Player player)
    {
        this.play(player, SoundArea.SELF, this.volume, this.pitch);
    }

    public void play(GamePlayer player)
    {
        this.play(player.getPlayer(), SoundArea.SELF, this.volume, this.pitch);
    }

    public void play(Location location)
    {
        this.play(location, SoundArea.SERVER_ALL, this.volume, this.pitch);
    }

    public void play(Player player, SoundArea area)
    {
        this.play(player, area, this.volume, this.pitch);
    }

    public void play(GamePlayer player, SoundArea area)
    {
        this.play(player.getPlayer(), area, this.volume, this.pitch);
    }

    public void play(Location location, SoundArea area)
    {
        this.play(location, area, this.volume, this.pitch);
    }

    public void play(Player player, float volume, float pitch)
    {
        this.play(player, SoundArea.SELF, volume, pitch);
    }

    public void play(GamePlayer player, float volume, float pitch)
    {
        this.play(player.getPlayer(), volume, pitch);
    }

    public void play(Location location, float volume, float pitch)
    {
        this.play(location, SoundArea.SERVER_ALL, volume, pitch);
    }

    public void play(Player player, Location location)
    {
        this.play(player, location, SoundArea.SELF, this.volume, this.pitch);
    }

    public void play(GamePlayer player, Location location)
    {
        this.play(player.getPlayer(), location, SoundArea.SELF, this.volume, this.pitch);
    }

    public void play(World world, float volume, float pitch)
    {
        this.playAll(world, volume, pitch);
    }

    public void play(World world)
    {
        this.play(world, this.volume, this.pitch);
    }

    private void playSoundNearBy(Location location, int range, float volume, float pitch)
    {
        location.getWorld().getNearbyEntitiesByType(Player.class, location, range).forEach(player ->
                player.playSound(location, this.name, volume, pitch)
        );
    }

    public void stop(Player player)
    {
        player.stopSound(this.name);
    }
}
