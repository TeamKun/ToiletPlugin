package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kunmc.lab.toiletplugin.utils.Utils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class KillPassengersCommand extends CommandBase
{
    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 0, 0) ||
                CommandUtils.checkPlayer(sender))
            return;

        Utils.killPassenger(((Player) sender).getPassengers());

        sender.sendMessage("All passengers were killed.");
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Kill all passengers.");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{};
    }
}
