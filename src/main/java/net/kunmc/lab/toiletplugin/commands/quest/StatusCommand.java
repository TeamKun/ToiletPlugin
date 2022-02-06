package net.kunmc.lab.toiletplugin.commands.quest;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.quest.QuestManager;
import net.kunmc.lab.toiletplugin.utils.CommandFeedBackUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 0, 1))
            return;

        List<Player> players = new ArrayList<>();

        String arg = "@a";

        if (args.length != 0)
            arg = args[0];

        Player argPlayer = Bukkit.getPlayer(arg);
        if (argPlayer == null)
        {
            List<Entity> entities = Bukkit.selectEntities(sender, arg);

            entities.stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .forEach(players::add);

            if (players.isEmpty())
            {
                sender.sendMessage(ChatColor.RED + "E: プレイヤーが見つかりませんでした。");
                return;
            }
        }
        else
            players.add(argPlayer);

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
