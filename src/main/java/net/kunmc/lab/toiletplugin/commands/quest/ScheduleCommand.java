package net.kunmc.lab.toiletplugin.commands.quest;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleCommand extends CommandBase
{
    private final GameMain game;

    public ScheduleCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 1, 2))
            return;

        Player player;
        if ((player = CommandFeedBackUtils.getPlayer(sender, args[0])) == null)
            return;

        if (args.length == 1)
        {
            Integer scheduleTime = game.getQuestManager().getScheduledTime(player);
            if (scheduleTime == null)
                checkQuesting(sender, player);
            else
                sender.sendMessage(ChatColor.GREEN + "S: " + player.getName() + "のクエストは" + scheduleTime + "秒後に開始されます。");

            return;
        }

        Integer scheduleTime;
        if ((scheduleTime = CommandFeedBackUtils.parseInteger(sender, args[1], -1)) == null)
            return;

        int result = game.getQuestManager().changeScheduledTime(player, scheduleTime);

        if (result != -1)
            sender.sendMessage(ChatColor.GREEN + "S: " + player.getName() + "のクエスト時間は" + result + "秒後に開始されます。");
        else
        {
            if (checkQuesting(sender, player))
                return;
            sender.sendMessage(ChatColor.RED + "E: " + player.getName() + "のクエストを開始できませんでした。");
        }
    }

    public boolean checkQuesting(CommandSender sender, Player player)
    {
        if (game.getQuestManager().isQuesting(player))
        {
            sender.sendMessage(ChatColor.RED + "E: " + player.getName() + "はクエスト中です。");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
            return game.getPlayerStateManager().getPlayers().stream().parallel()
                    .map(Player::getName).collect(Collectors.toList());
        if (args.length == 2)
            return Collections.singletonList("[time:int:0~]");
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プレイヤのクエストまでの時間を変更または確認します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("name", "player"),
                optional("time", "int")
        };
    }
}
