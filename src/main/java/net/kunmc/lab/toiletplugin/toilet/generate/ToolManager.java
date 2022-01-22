package net.kunmc.lab.toiletplugin.toilet.generate;

import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import tokyo.peya.lib.bukkit.ItemUtils;

public class ToolManager implements Listener
{
    private static final NamespacedKey nsKey = new NamespacedKey(ToiletPlugin.getPlugin(), "popUpToilet");

    public static ItemStack getTool(String name)
    {
        ItemStack stack = new ItemStack(Material.CHEST);

        ItemMeta meta = stack.getItemMeta();

        stack = ItemUtils.quickLore(stack, ChatColor.GOLD + "トイレを設置するには平地で右クリックをしてください。");


        meta.displayName(Component.text(ChatColor.GREEN + "コンパクトトイレ「" + name + "」"));

        meta.getPersistentDataContainer().set(nsKey,
                PersistentDataType.STRING, name
        );

        stack.setItemMeta(meta);

        return stack;
    }

    private static boolean hasName(Chest chest)
    {
        ItemStack stack = chest.getBlockInventory().getItem(0);

        if (stack == null)
            return false;

        return stack.getItemMeta().getPersistentDataContainer().has(nsKey, PersistentDataType.STRING);
    }

    private static String getName(Chest chest)
    {
        ItemStack stack = chest.getBlockInventory().getItem(0);

        if (stack == null)
            return null;

        if (!stack.getItemMeta().getPersistentDataContainer().has(nsKey, PersistentDataType.STRING))
            return null;

        return stack.getItemMeta().getPersistentDataContainer().get(nsKey, PersistentDataType.STRING);


    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent e)
    {
        if (e.getBlock().getType() != Material.CHEST)
            return;

        Chest chest = (Chest) e.getBlock().getState();

        if (hasName(chest))
            e.setDropItems(false);
    }

    @EventHandler
    public void onChestPlace(BlockPlaceEvent e)
    {
        if (e.getBlock().getType() != Material.CHEST)
            return;

        Chest chest = (Chest) e.getBlock().getState();
        ItemStack stack = e.getItemInHand();

        if (stack.getItemMeta().getPersistentDataContainer().has(nsKey, PersistentDataType.STRING))
            chest.getBlockInventory().setItem(0, stack);
    }

    @EventHandler
    public void onHopper(InventoryMoveItemEvent e)
    {
        if (e.getSource().getHolder() == null)
            return;

        if (e.getSource().getHolder() instanceof Chest)
        {
            Chest chest = (Chest) e.getSource().getHolder();
            if (hasName(chest))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent e)
    {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (e.getClickedBlock() == null)
            return;

        if (e.getClickedBlock().getType() != Material.CHEST)
            return;

        Chest chest = (Chest) e.getClickedBlock().getState();

        String name = getName(chest);

        e.setCancelled(true);

        System.out.println(name);
    }
}
