package es.boffmedia.waypoints.commands.waypoints;

import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;

import javax.annotation.Nonnull;

public class ListWaypointsCommand extends AbstractPlayerCommand {

    public ListWaypointsCommand() {
        super("listmarkers", "List all your map markers");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player sender = commandContext.senderAs(Player.class);
        PlayerWorldData perWorldData = sender.getPlayerConfigData().getPerWorldData(world.getName());
        MapMarker[] markers = perWorldData.getWorldMapMarkers();



        if (markers == null || markers.length == 0) {
            sender.sendMessage(Message.raw("You have no map markers."));
            return;
        }

        sender.sendMessage(Message.raw("Your map markers:"));
        for (MapMarker marker : markers) {
            String info = String.format("- %s (ID: %s) at [%.2f, %.2f, %.2f]",
                    marker.name,
                    marker.id,
                    marker.transform.position.x,
                    marker.transform.position.y,
                    marker.transform.position.z
            );
            sender.sendMessage(Message.raw(info));
        }
    }
}