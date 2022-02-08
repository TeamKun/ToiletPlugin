package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletState;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ChangeToiletStateCommand extends CommandBase
{
    private static final List<String> toiletStateNames = Stream.of(ToiletState.values())
            .map(ToiletState::name)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    private final GameMain game;

    public ChangeToiletStateCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 2, 2))
            return;

        String toiletName = args[0];
        String stateName = args[1];

        OnGroundToilet toilet = game.getToiletManager().getToilet(toiletName);

        if (toilet == null)
        {
            sender.sendMessage("Toilet not found.");
            return;
        }

        if (!toiletStateNames.contains(stateName))
        {
            sender.sendMessage("Invalid state name.");
            return;
        }

        ToiletState state = ToiletState.valueOf(stateName);

        toilet.setState(state);

        sender.sendMessage("State is now " + state.name());

        if (state == ToiletState.OPEN)
            return;

        toilet.setToiletPlayer((Player) sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
            return game.getToiletManager().getToiletNames();
        else if (args.length == 2)
            return toiletStateNames;

        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Change state of toilet.");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("toiletName", "toilet"),
                required("state", "ToiletState@String")
        };
    }
}
