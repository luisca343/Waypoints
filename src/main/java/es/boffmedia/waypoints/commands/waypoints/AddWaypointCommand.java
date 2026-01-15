package es.boffmedia.waypoints.commands.waypoints;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Transform;
import com.hypixel.hytale.protocol.packets.worldmap.ContextMenuItem;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;

import javax.annotation.Nonnull;

public class AddWaypointCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> nameArg;

    public AddWaypointCommand() {
        super("add", "Add a map marker with a name");
        this.nameArg = withRequiredArg("name", "The waypoint name", (ArgumentType) ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player sender = commandContext.senderAs(Player.class);

        String markerName = commandContext.get(this.nameArg);
        if (markerName == null || markerName.trim().isEmpty()) {
            sender.sendMessage(Message.raw("Tienes que especificar un nombre para el marcador. Uso: /marker add <nombre>"));
            return;
        }

        TransformComponent transformComponent = store.getComponent(sender.getReference(), TransformComponent.getComponentType());
        Direction orientation = new Direction(0.0F, 0.0F, 0.0F);
        Transform transform = new Transform(transformComponent.getSentTransform().position, orientation);

        ContextMenuItem[] items = new ContextMenuItem[]{
                new ContextMenuItem("Remove Waypoint", "waypoint remove " + markerName)
        };

        MapMarker playerMarker = new MapMarker(markerName.toLowerCase().replaceAll("\\s+", "_"), markerName, "Spawn.png", transform, items);

        WorldMapManager.PlayerMarkerReference playerMarkerReference = WorldMapManager.createPlayerMarker(sender.getReference(), playerMarker, store);

        sender.sendMessage(Message.raw("Marker created with ID: " + playerMarkerReference.getMarkerId()));
    }
}