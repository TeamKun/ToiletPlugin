package net.kunmc.lab.toiletplugin.game.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.kunmc.lab.toiletplugin.ToiletPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class HUDPacketFilter extends PacketAdapter
{
    public HUDPacketFilter()
    {
        super(ToiletPlugin.getPlugin(), PacketType.Play.Server.ENTITY_METADATA);
    }

    @Override
    public void onPacketSending(PacketEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
            return;

        Player player = event.getPlayer();

        Entity entity = event.getPacket().getEntityModifier(player.getWorld()).read(0);

        if (entity == null)
            return;

        if (!entity.getPersistentDataContainer().has(
                new NamespacedKey(ToiletPlugin.getPlugin(), "hud_entity"),
                PersistentDataType.STRING
        ))
            return;

        String uuidString = entity.getPersistentDataContainer().get(
                new NamespacedKey(ToiletPlugin.getPlugin(), "hud_entity"),
                PersistentDataType.STRING
        );
        if (uuidString == null)
            return;

        UUID uuid = UUID.fromString(uuidString);

        if (player.getUniqueId().equals(uuid))
        {
            PacketContainer container = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            container.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
            try
            {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, container);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
