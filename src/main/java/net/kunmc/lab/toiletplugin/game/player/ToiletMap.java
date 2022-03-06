package net.kunmc.lab.toiletplugin.game.player;

import net.kunmc.lab.toiletplugin.game.GameMain;
import net.kunmc.lab.toiletplugin.game.toilet.OnGroundToilet;
import net.kunmc.lab.toiletplugin.game.toilet.ToiletManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ToiletMap extends MapRenderer
{
    private static final int MAP_SIZE = 128;
    private final GameMain gameMain;
    private final Map<Location, OnGroundToilet> toilets;

    public ToiletMap(GameMain gameMain)
    {
        super(false);
        this.gameMain = gameMain;
        this.toilets = new HashMap<>();
    }

    private static int getMultipleScale(MapView.Scale scale)
    {
        switch (scale)
        {
            case CLOSE:
                return 2;
            case NORMAL:
                return 4;
            case FAR:
                return 8;
            case FARTHEST:
                return 16;
            default:
                return 1;
        }
    }

    public void updateToilets()
    {
        ToiletManager toiletManager = gameMain.getToiletManager();

        Map<Location, OnGroundToilet> toiletMap = toiletManager.getToilets().values().stream().parallel()
                .collect(Collectors.toMap(toilet -> toilet.getScytheLocation().toLocation(), toilet -> toilet));

        toilets.putAll(toiletMap);
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player)
    {
        if (!gameMain.getConfig().isGiveToiletMap())
            return;
        int scale = getMultipleScale(map.getScale());

        while (canvas.getCursors().size() > 0)
            canvas.getCursors().removeCursor(canvas.getCursors().getCursor(0));

        toilets.forEach((location, toilet) -> {
            int x = ((location.getBlockX() - map.getCenterX()) / scale) * 2;
            int z = ((location.getBlockZ() - map.getCenterZ()) / scale) * 2;

            if (Math.abs(x) > 127)
                x = location.getBlockX() > map.getCenterZ() ? 127: -128;

            if (Math.abs(z) > 127)
                z = location.getBlockZ() > map.getCenterZ() ? 127: -128;

            MapCursor.Type type;
            switch (toilet.getState())
            {
                case OPEN:
                    type = MapCursor.Type.BANNER_PINK;
                    break;
                case PLAYER_USING:
                    type = MapCursor.Type.RED_X;
                    break;
                default:
                    type = MapCursor.Type.BANNER_MAGENTA;
                    break;
            }

            canvas.getCursors().addCursor(new MapCursor((byte) x, (byte) z, (byte) 0, type, true));
        });
    }
}
