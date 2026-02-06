package es.boffmedia.waypoints.commands.waypoints;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import es.boffmedia.waypoints.Constants;
import es.boffmedia.waypoints.util.PermissionsUtil;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class WaypointTeleportCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> nameArg;

    public WaypointTeleportCommand() {
        super("teleport", "Teleport to a waypoint");
        this.nameArg = withRequiredArg("name", "The waypoint name", (ArgumentType) ArgTypes.STRING);
        // Permission check is done manually in execute() using PermissionsUtil
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String name = commandContext.get(this.nameArg);
        if (name == null || name.trim().isEmpty()) {
            commandContext.sendMessage(Message.raw("You must specify a waypoint name!"));
            return;
        }

        Ref<EntityStore> playerEntityRef = commandContext.senderAsPlayerRef();
        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            commandContext.sendMessage(Message.raw("Could not find player entity!"));
            return;
        }

        world.execute(() -> {
            if (!playerEntityRef.isValid()) {
                return;
            }

            Player player = store.getComponent(playerEntityRef, Player.getComponentType());
            if (player == null) {
                commandContext.sendMessage(Message.raw("Could not access player data!"));
                return;
            }

            // Check permission using the new utility
            if (!PermissionsUtil.canTeleport(player)) {
                commandContext.sendMessage(Message.raw("You do not have permission to teleport to waypoints."));
                return;
            }

            // Get per-world data
            String worldName = world.getName();
            com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData perWorldData =
                    player.getPlayerConfigData().getPerWorldData(worldName);

            MapMarker[] markers = perWorldData.getWorldMapMarkers();
            if (markers == null || markers.length == 0) {
                commandContext.sendMessage(Message.raw("You don't have any waypoints in this world."));
                return;
            }

            // Find waypoint by name (null-safe)
            MapMarker waypoint = null;
            for (MapMarker marker : markers) {
                String mName = marker.name != null ? marker.name : "";
                if (mName.equalsIgnoreCase(name)) {
                    waypoint = marker;
                    break;
                }
            }

            if (waypoint == null) {
                commandContext.sendMessage(Message.raw("Waypoint '" + name + "' not found!"));
                return;
            }

            // Teleport to waypoint
            Vector3d targetPos = new Vector3d(
                    waypoint.transform.position.x,
                    waypoint.transform.position.y,
                    waypoint.transform.position.z
            );
            Vector3f rotation = new Vector3f(0.0F, 0.0F, 0.0F);
            Teleport teleport = new Teleport(targetPos, rotation);
            store.addComponent(playerEntityRef, Teleport.getComponentType(), teleport);

            commandContext.sendMessage(Message.raw("Teleported to '" + (waypoint.name != null ? waypoint.name : name) + "'!"));
        });
    }
}
