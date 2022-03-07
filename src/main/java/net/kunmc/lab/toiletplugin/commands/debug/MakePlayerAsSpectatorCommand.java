package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MakePlayerAsSpectatorCommand extends CommandBase
{
    private final GameMain game;

    public MakePlayerAsSpectatorCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 1) || CommandUtils.checkPlayer(sender))
            return;

        Integer max;
        if ((max = CommandUtils.parseInteger(sender, args[0], 1)) == null)
            return;


        Player player = (Player) sender;

        player.setGameMode(GameMode.SPECTATOR);

        game.getPlayerStateManager().getPlayer(player).setMaxTimeLimit(max);

        player.sendMessage(of("You are now a spectator."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Make a player as a spectator and set respawn time.");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("maxSeconds", "int:seconds"),
        };
    }
}
