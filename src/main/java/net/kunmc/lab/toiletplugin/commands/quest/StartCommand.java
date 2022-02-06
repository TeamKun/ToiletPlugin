package net.kunmc.lab.toiletplugin.commands.quest;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.game.GameMain;
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
        if (CommandFeedBackUtils.invalidLengthMessage(sender, args, 1))
            return;

        List<Player> players = new ArrayList<>();

        Player argPlayer = Bukkit.getPlayer(args[0]);
        if (argPlayer == null)
        {
            List<Entity> entities = Bukkit.selectEntities(sender, args[0]);

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

        players.forEach(player -> {

            boolean result = game.getQuestManager().start(player);

            if (result)
                sender.sendMessage(ChatColor.GREEN + "S: " + player.getName() + "のクエストを開始しました。");
            else
                sender.sendMessage(ChatColor.RED + "E: " + player.getName() + "のクエストを開始できませんでした。");
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        if (args.length != 1)
            return null;

        List<String> playerNames = game.getPlayers().stream().parallel().map(Player::getName).collect(Collectors.toList());

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
