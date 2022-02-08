package net.kunmc.lab.toiletplugin.commands.quest;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CancelCommand extends CommandBase
{
    private final GameMain game;

    public CancelCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 1))
            return;

        List<Player> players = CommandUtils.getPlayer(sender, args.length == 0 ? "@a": args[0]);

        if (players == null)
            return;

        boolean noReschedule = args.length <= 1 || Boolean.parseBoolean(args[1]);

        players.forEach(player -> {

            int result = game.getQuestManager().cancel(player, noReschedule);

            if (result != -1)
                if (result == 0)
                    sender.sendMessage(ChatColor.GREEN + "I: " + player.getName() + "のクエストを停止しました。");
                else
                    sender.sendMessage(ChatColor.GREEN + "I: " + player.getName() + "のクエストを停止し, " + result + " 秒後に再スケジュールしました。");
            else
                sender.sendMessage(ChatColor.RED + "E: " + player.getName() + "のクエストを停止できませんでした。");
        });

        sender.sendMessage(ChatColor.GREEN + "S: " + players.size() + "人に対して操作を実行しました。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length != 1)
            return null;

        List<String> playerNames = game.getPlayerStateManager().getPlayers().stream().parallel()
                .map(Player::getName).collect(Collectors.toList());

        playerNames.addAll(Arrays.asList("@a", "@p", "@r", "@s"));

        return playerNames;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プレイヤのクエストを強制停止します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("name|selector", "player"),
                optional("noReschedule", "boolean", "true")
        };
    }
}
