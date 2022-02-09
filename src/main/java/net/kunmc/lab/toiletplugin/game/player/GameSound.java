package net.kunmc.lab.toiletplugin.game.player;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

public enum GameSound
{
    TOILET_GENERATE("block.anvil.destroy", SoundCategory.BLOCKS, 1.0F, 1.0F),
    IRON_DOOR_OPEN("block.door_open", SoundCategory.BLOCKS, 1.0F, 1.0F),
    IRON_DOOR_CLOSE("block.door_close", SoundCategory.BLOCKS, 1.0F, 1.0F),
    QUEST_START("entity.player.levelup", SoundCategory.MASTER, 1.0F, 0.3F),
    QUEST_COMPLETE("block.end_portal.spawn", SoundCategory.MASTER, 0.6F, 0.6F),
    QUEST_FAILURE("entity.player.hurt_on_fire", SoundCategory.MASTER, 1.0F, 1.0F),
    QUEST_CANCEL("entity.player.levelup", SoundCategory.MASTER, 1.0F, 2.0F),

    ;
    @Getter
    private final String name;
    @Getter
    private final SoundCategory category;
    @Getter
    private final float volume;
    @Getter
    private final float pitch;

    GameSound(String name, SoundCategory category, float volume, float pitch)
    {
        this.name = "minecraft:" + name;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void play(Player player)
    {
        player.playSound(player.getLocation(), this.name, this.volume, this.pitch);
    }

    public void play(Player player, float volume, float pitch)
    {
        player.playSound(player.getLocation(), this.name, volume, pitch);
    }

    public void play(GamePlayer player)
    {
        player.getPlayer().playSound(player.getPlayer().getLocation(), this.name, this.volume, this.pitch);
    }

    public void play(GamePlayer player, float volume, float pitch)
    {
        player.getPlayer().playSound(player.getPlayer().getLocation(), this.name, volume, pitch);
    }

    public void play(Player player, Location location)
    {
        player.playSound(location, this.name, this.volume, this.pitch);
    }

    public void play(Player player, Location location, float volume, float pitch)
    {
        player.playSound(location, this.name, volume, pitch);
    }

    public void play(GamePlayer player, Location location)
    {
        player.getPlayer().playSound(location, this.name, this.volume, this.pitch);
    }

    public void play(GamePlayer player, Location location, float volume, float pitch)
    {
        player.getPlayer().playSound(location, this.name, volume, pitch);
    }

    public void play(World world, Location location)
    {
        world.playSound(location, this.name, this.volume, this.pitch);
    }

    public void play(World world, Location location, float volume, float pitch)
    {
        world.playSound(location, this.name, volume, pitch);
    }

    public void play(Location location)
    {
        location.getWorld().playSound(location, this.name, this.volume, this.pitch);
    }

    public void play(Location location, float volume, float pitch)
    {
        location.getWorld().playSound(location, this.name, volume, pitch);
    }
}
