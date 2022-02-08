package net.kunmc.lab.toiletplugin.commands.quest;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class UnscheduleCommand extends CommandBase
{
    private final GameMain game;

    public UnscheduleCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 1, 1))
            return;

        List<Player> players = CommandUtils.getPlayer(sender, args[0]);

        if (players == null)
            return;

        players.removeIf(player -> !game.getPlayerStateManager().getPlayer(player).isScheduled());

        players.forEach(player -> {
            boolean result = game.getQuestManager().unSchedule(player);

            if (result)
                sender.sendMessage(ChatColor.GREEN + "I: " + player.getName() + "クエストのスケジュールを削除しました。");
            else
                sender.sendMessage(ChatColor.RED + "E: " + player.getName() + "のスケジュールを削除できませんでした。");
        });

        sender.sendMessage(ChatColor.GREEN + "S: " + players.size() + "人に対して操作を実行しました。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
            return game.getPlayerStateManager().getPlayers().stream().parallel()
                    .filter(GamePlayer::isScheduled)
                    .map(GamePlayer::getPlayer)
                    .map(Player::getName)
                    .collect(Collectors.toList());
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プレイヤのクエストのスケジュールを削除します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("name", "player")
        };
    }
}
