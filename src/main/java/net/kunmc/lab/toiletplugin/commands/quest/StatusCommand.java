package net.kunmc.lab.toiletplugin.commands.quest;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StatusCommand extends CommandBase
{
    private final GameMain game;

    public StatusCommand(GameMain game)
    {
        this.game = game;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (CommandUtils.invalidLengthMessage(sender, args, 0, 1))
            return;

        List<Player> players = CommandUtils.getPlayer(sender, args.length == 0 ? "@a": args[0]);

        if (players == null)
            return;

        QuestManager questManager = game.getQuestManager();

        players.forEach(player -> {
            if (!game.getPlayerStateManager().isPlaying(player))
                sender.sendMessage(ChatColor.GREEN + player.getName() + "：参加していません。");
            else if (questManager.isQuesting(player))
                sender.sendMessage(ChatColor.GREEN + player.getName() + "：クエスト中 残り" +
                        questManager.getQuestTime(player) + "秒で死亡。");
            else if (questManager.isScheduled(player))
                sender.sendMessage(ChatColor.GREEN + player.getName() + "：待機中 残り" +
                        questManager.getScheduledTime(player) + "秒で開始。");
            else
                sender.sendMessage(ChatColor.GREEN + player.getName() + "：参加中 クエスト予約/開始なし。");
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
        return of("プレイヤのステータスを表示します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("name|selector", "player")
        };
    }
}
