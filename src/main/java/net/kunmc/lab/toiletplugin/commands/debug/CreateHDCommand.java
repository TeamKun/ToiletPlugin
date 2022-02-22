package net.kunmc.lab.toiletplugin.commands.debug;

import net.kunmc.lab.toiletplugin.CommandBase;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import net.kunmc.lab.toiletplugin.utils.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CreateHDCommand extends CommandBase implements Listener
{

    @EventHandler
    public void onDamageEvent(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player))
            return;

        if (entityClass.isInstance(event.getEntity()))
        {
            event.getDamager().sendMessage(Component.text("HD ID: " + event.getEntity().getUniqueId())
                    .clickEvent(ClickEvent.suggestCommand("/toilet debug createHD " + event.getEntity().getUniqueId() + " "))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to suggest command."))));
        }
    }

    // Playground
    private static final Class<? extends Entity> entityClass = Item.class;

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("Create HD stack.");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("uuid", "uuid"),
                required("text", "string")
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if (HandlerList.getHandlerLists().stream()
                .map(HandlerList::getRegisteredListeners)
                .flatMap(Arrays::stream)
                .noneMatch(l -> l.getListener() == this)
        )
            Bukkit.getPluginManager().registerEvents(this, ToiletPlugin.getPlugin());

        if (CommandUtils.checkPlayer(sender) &&
                CommandUtils.invalidLengthMessage(sender, args, 1, 2))
            return;

        Player player = (Player) sender;

        Entity entity = null;

        if (args.length == 1)
            entity = player.getLocation().getWorld().spawn(player.getLocation(), entityClass);
        else
        {
            try
            {
                entity = Bukkit.getEntity(UUID.fromString(args[0]));
            }
            catch (IllegalArgumentException ignored)
            {
                sender.sendMessage("Invalid UUID.");
            }
        }

        if (entity == null)
        {
            sender.sendMessage("Entity not found.");
            return;
        }

        sender.sendMessage(Component.text("HD ID: " + entity.getUniqueId())
                .clickEvent(ClickEvent.suggestCommand("/toilet debug createHD " + entity.getUniqueId() + " "))
                .hoverEvent(HoverEvent.showText(Component.text("Click to suggest command."))));

        if (args.length == 1)
        {
            passEntity(entity);
            entity.setCustomName(args[0]);
            entity.setCustomNameVisible(true);
            sender.sendMessage("Created HD.");

            player.addPassenger(entity);

            return;
        }

        Entity passenger = player.getWorld().spawn(player.getLocation(), entityClass);

        passEntity(passenger);

        passenger.setCustomName(args[1]);
        passenger.setCustomNameVisible(true);
        entity.addPassenger(passenger);

        sender.sendMessage("Created HD.");
    }

    public void passEntity(Entity entity)
    {
        ((Item) entity).setItemStack(new ItemStack(Material.POTATO));
    }
}
