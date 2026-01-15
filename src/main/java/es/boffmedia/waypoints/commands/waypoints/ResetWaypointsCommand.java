package es.boffmedia.waypoints.commands.waypoints;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;

import javax.annotation.Nonnull;

public class ResetWaypointsCommand extends AbstractPlayerCommand {

    public ResetWaypointsCommand() {
        super("resetmarkers", "Reset all map markers");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player sender = commandContext.senderAs(Player.class);
        PlayerWorldData perWorldData = sender.getPlayerConfigData().getPerWorldData(world.getName());

        perWorldData.setWorldMapMarkers(new com.hypixel.hytale.protocol.packets.worldmap.MapMarker[0]);
        sender.sendMessage(Message.raw("All markers reset."));
    }
}