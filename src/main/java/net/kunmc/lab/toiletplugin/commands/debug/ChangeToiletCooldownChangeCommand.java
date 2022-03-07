package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletState;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        if (CommandUtils.invalidLengthMessage(sender, args, 2, 4))
            return;

        String toiletName = args[0];
        Integer max;
        if ((max = CommandUtils.parseInteger(sender, args[1], 1)) == null)
            return;

        Integer seconds = null;
        if (args.length >= 3 && (seconds = CommandUtils.parseInteger(sender, args[2], 1, max)) == null)
            return;

        ToiletState state = null;
        if (args.length >= 4)
        {
            try
            {
                state = ToiletState.valueOf(args[3].toUpperCase());
            }
            catch (Exception e)
            {
                sender.sendMessage("Invalid toilet state.");
                return;
            }
        }

        OnGroundToilet toilet = game.getToiletManager().getToilet(toiletName);
        if (toilet == null)
        {
            sender.sendMessage("Toilet not found.");
            return;
        }
        toilet.setCooldownMax(max);
        sender.sendMessage("Cooldown max is now " + seconds + " sec.");
        if (seconds != null)
        {
            toilet.setCooldown(seconds);
            sender.sendMessage("Cooldown is now " + seconds + " sec.");
        }

        if (state != null)
        {
            if (state != ToiletState.OPEN)
                toilet.setToiletPlayer((Player) sender);
            toilet.setState(state);
            sender.sendMessage("Toilet state is now " + state.name());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
            return game.getToiletManager().getToiletNames();
        if (args.length == 4)
            return Arrays.stream(ToiletState.values())
                    .map(ToiletState::name)
                    .filter(s -> s.startsWith(args[3])).collect(Collectors.toList());

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
                optional("remainingTime", "int:seconds"),
                optional("state", "state")
        };
    }
}
