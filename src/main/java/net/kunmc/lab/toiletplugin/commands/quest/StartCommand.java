package net.kunmc.lab.toiletplugin.commands.quest;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.player.GamePlayer;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StartCommand extends CommandBase
{
    private final GameMain game;

    public StartCommand(GameMain game)
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

        players.removeIf(player -> {
            GamePlayer gamePlayer = game.getPlayerStateManager().getPlayer(player);
            if (!gamePlayer.isPlaying())
                return true;
            return gamePlayer.isQuesting();
        });

        players.forEach(player -> {

            int result = game.getQuestManager().start(player);

            if (result != -1)
                sender.sendMessage(ChatColor.GREEN + "I: " + player.getName() + "のクエストを開始し、制限時間を" + result + "秒に設定しました。");
            else
                sender.sendMessage(ChatColor.RED + "E: " + player.getName() + "のクエストを開始できませんでした。");
        });

        sender.sendMessage(ChatColor.GREEN + "S: " + players.size() + "人に対して操作を実行しました。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length != 1)
            return null;

        List<String> playerNames = game.getPlayerStateManager().getPlayers().stream().parallel()
                .filter(GamePlayer::isPlaying)
                .filter(GamePlayer::isQuesting)
                .map(GamePlayer::getPlayer)
                .map(Player::getName)
                .collect(Collectors.toList());

        playerNames.addAll(Arrays.asList("@a", "@p", "@r", "@s"));

        return playerNames;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プレイヤのクエストを強制開始します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("name|selector", "player")
        };
    }
}
