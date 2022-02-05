package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ChangeToiletCooldownChangeCommand extends CommandBase
{
    private final GameMain game;

    public ChangeToiletCooldownChangeCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 2, 3))
            return;

        String toiletName = args[0];
        Integer max;
        if ((max = CommandFeedBackUtils.parseInteger(sender, args[1], 1)) == null)
            return;

        Integer seconds = null;
        if (args.length == 3 && (seconds = CommandFeedBackUtils.parseInteger(sender, args[2], 1, max)) == null)
            return;

        OnGroundToilet toilet = game.getToiletManager().getToilet(toiletName);

        toilet.setCooldownMax(max);
        sender.sendMessage("Cooldown max is now " + seconds + " sec.");
        if (seconds != null)
        {
            toilet.setCooldown(seconds);
            sender.sendMessage("Cooldown is now " + seconds + " sec.");
        }
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
        return of("Change max cooldown of toilet.");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("toiletName", "toilet"),
                required("maxSeconds", "int:seconds"),
                required("remainingTime", "int:seconds"),
        };
    }
}
