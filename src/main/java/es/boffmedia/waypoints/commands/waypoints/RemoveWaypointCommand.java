package es.boffmedia.waypoints.commands.waypoints;

import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class RemoveWaypointCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> nameArg;

    public RemoveWaypointCommand() {
        super("remove", "Remove a map marker by name");
        this.nameArg = withRequiredArg("name", "The waypoint name to remove", (ArgumentType) ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player sender = commandContext.senderAs(Player.class);

        String markerName = commandContext.get(this.nameArg);
        if (markerName == null || markerName.trim().isEmpty()) {
            sender.sendMessage(Message.raw("Tienes que especificar el nombre del marcador a eliminar. Uso: /removemarker <nombre>"));
            return;
        }

        PlayerWorldData perWorldData = sender.getPlayerConfigData().getPerWorldData(world.getName());
        MapMarker[] markers = perWorldData.getWorldMapMarkers();

        if (markers == null || markers.length == 0) {
            sender.sendMessage(Message.raw("No tienes ningún marcador en este mundo."));
            return;
        }

        List<MapMarker> updatedMarkers = new ArrayList<>();
        boolean removed = false;

        for (MapMarker marker : markers) {
            if (!marker.name.equalsIgnoreCase(markerName)) {
                updatedMarkers.add(marker);
            } else {
                removed = true;
            }
        }

        if (!removed) {
            sender.sendMessage(Message.raw("No se encontró ningún marcador con ese nombre."));
            return;
        }

        perWorldData.setWorldMapMarkers(updatedMarkers.toArray(new MapMarker[0]));
        sender.sendMessage(Message.raw("Marcador eliminado correctamente."));
    }
}