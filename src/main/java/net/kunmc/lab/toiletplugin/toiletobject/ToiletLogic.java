package net.kunmc.lab.toiletplugin.toiletobject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ToiletLogic implements Listener
{
    private final ToiletRegister register;

    public ToiletLogic(ToiletRegister register)
    {
        this.register = register;
    }

    private static TextComponent copy(String column, String text)
    {
        return Component.text(ChatColor.GREEN + column + ": " + ChatColor.WHITE + text)
                .clickEvent(ClickEvent.copyToClipboard(text))
                .hoverEvent(HoverEvent.showText(Component.text(ChatColor.GREEN + "クリックしてコピー")));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e)
    {
        if (e.getBlock().getType() != Material.CAULDRON && e.getBlock().getType() != Material.IRON_DOOR)
            return;
        Toilet toilet = register.getToilet(e.getBlock().getLocation());

        if (toilet == null)
            return;

        e.setCancelled(true);

        if (!e.getPlayer().hasPermission("toilet.admin"))
        {
            e.getPlayer().sendMessage(ChatColor.RED + "E: このブロックはトイレとして登録されているため破壊できません！");
            return;
        }

        Player player = e.getPlayer();

        Toilet.LocationPojo location = toilet.getArmorStandLocation();
        Toilet.LocationPojo door = toilet.getDoorLocation();
        Toilet.LocationPojo scythe = toilet.getScytheLocation();

        player.sendMessage(ChatColor.GOLD + "---=== トイレ情報 ===---");
        player.sendMessage(copy("名前", toilet.getName()));
        player.sendMessage(copy("中心位置", location.getX() + "," + location.getY() + "," + location.getZ()));
        player.sendMessage(copy("ドア位置", door.getX() + "," + door.getY() + "," + door.getZ()));
        player.sendMessage(copy("大釜位置", scythe.getX() + "," + scythe.getY() + "," + scythe.getZ()));
        player.sendMessage(Component.text("操作： ")
                .color(NamedTextColor.GREEN)
                .append(Component.text("[削除] ")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/toilet remove " + toilet.getName())))
                .hoverEvent(HoverEvent.showText(Component.text(ChatColor.RED + "トイレ「" + toilet.getName() + "」を削除します。"))));
        player.sendMessage(ChatColor.GOLD + "---=== トイレ情報 ===---");
    }
}
