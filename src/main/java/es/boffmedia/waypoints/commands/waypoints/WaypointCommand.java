package es.boffmedia.waypoints.commands.waypoints;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.util.Config;
import es.boffmedia.waypoints.Constants;
import es.boffmedia.waypoints.util.PermissionsUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import es.boffmedia.waypoints.config.WaypointsConfig;
import es.boffmedia.waypoints.pages.WaypointPage;

import javax.annotation.Nonnull;

public class WaypointCommand extends AbstractPlayerCommand {

    private final Config<WaypointsConfig> config;

    public WaypointCommand(Config<WaypointsConfig> config) {
        super("waypoint", "Parent command for marker operations");
        this.config = config;
        addSubCommand(new AddWaypointCommand());
        addSubCommand(new RemoveWaypointCommand());
        addAliases("wp", "waypoints");
        setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player sender = commandContext.senderAs(Player.class);

        if (!PermissionsUtil.canOpenWaypointUI(sender)) {
            sender.sendMessage(Message.raw("You do not have permission to use this command."));
            return;
        }
        PlayerWorldData perWorldData = sender.getPlayerConfigData().getPerWorldData(world.getName());
        MapMarker[] waypoints = perWorldData.getWorldMapMarkers();

        WaypointPage page = new WaypointPage(playerRef, waypoints != null ? waypoints : new MapMarker[0], config);
        sender.getPageManager().openCustomPage(ref, store, page);
    }

    
}