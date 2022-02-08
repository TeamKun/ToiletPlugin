package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PurgeToiletLocalDataCommand extends CommandBase
{
    private final GameMain game;

    public PurgeToiletLocalDataCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 1, 1))
            return;

        String toiletName = args[0];
        OnGroundToilet toilet = game.getToiletManager().getToilet(toiletName);
        if (toilet == null)
        {
            sender.sendMessage("Toilet not found.");
            return;
        }

        toilet.purge();
        sender.sendMessage("A toilet local data has been purged.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
            return game.getToiletManager().getToiletNames();

        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Purge toilet local data.(Ex: BlockState, Cooldown, etc.)");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("toiletName", "toilet"),
        };
    }
}

