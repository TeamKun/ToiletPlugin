package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GameSound;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlaySoundCommand extends CommandBase
{
    private final GameMain game;

    public PlaySoundCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 1, 3) ||
                CommandUtils.checkPlayer(sender))
            return;
        String soundName = args[0];

        GameSound sound;
        try
        {
            sound = GameSound.valueOf(soundName);
        }
        catch (IllegalArgumentException e)
        {
            sender.sendMessage("Invalid sound name.");
            return;
        }

        Float pitch = 1.0F;
        Float volume = 1.0F;

        if (args.length >= 2)
            if ((pitch = CommandUtils.parseFloat(sender, args[1], 1.0f, 2.0f)) == null)
                return;
        if (args.length >= 3)
            if ((volume = CommandUtils.parseFloat(sender, args[2], 0.0f, 1.0f)) == null)
                return;

        sound.play((Player) sender, pitch, volume);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
            return Arrays.stream(GameSound.values())
                    .map(GameSound::name)
                    .collect(Collectors.toList());
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Play a sound that registered in the GameSound enum.");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("sound", "GameSound"),
                optional("pitch", "float"),
                optional("volume", "float")
        };
    }
}
