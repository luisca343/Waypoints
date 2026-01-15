package es.boffmedia.waypoints.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class WaypointPage extends InteractiveCustomUIPage<WaypointPage.WaypointPageData> {
    private final MapMarker[] waypoints;
    private final String WAYPOINTS_LIST_REF = "#WaypointsList";
    private final String WAYPOINT_ITEM_UI = "Pages/WaypointItem.ui";

    public static class WaypointPageData {
        public String action;
        public String waypointId;

        public static final BuilderCodec<WaypointPageData> CODEC = ((BuilderCodec.Builder<WaypointPageData>) ((BuilderCodec.Builder<WaypointPageData>)
                BuilderCodec.builder(WaypointPageData.class, WaypointPageData::new))
                .append(new KeyedCodec<>("Action", Codec.STRING), (WaypointPageData o, String v) -> o.action = v, (WaypointPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("WaypointId", Codec.STRING), (WaypointPageData o, String v) -> o.waypointId = v, (WaypointPageData o) -> o.waypointId)
                .add())
                .build();
    }

    public WaypointPage(@Nonnull PlayerRef playerRef, MapMarker[] waypoints) {
        super(playerRef, CustomPageLifetime.CanDismiss, WaypointPageData.CODEC);
        this.waypoints = waypoints;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/WaypointPage.ui");
        uiCommandBuilder.clear(WAYPOINTS_LIST_REF);

        if(waypoints.length == 0){
            uiCommandBuilder.appendInline(WAYPOINTS_LIST_REF, "Label { Text: \"No waypoints\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
        }

        int i = 0;

        for (MapMarker waypoint : waypoints) {
            String selector = "#WaypointsList[" + i + "]";
            uiCommandBuilder.append(WAYPOINTS_LIST_REF, WAYPOINT_ITEM_UI);

            String waypointName = waypoint.name;
            String waypointId = waypoint.id;

            uiCommandBuilder.set(selector + " #WaypointName.Text", waypointName);

            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #RemoveButton",
                    new EventData().append("Action", "Remove").append("WaypointId", waypointId),
                    false
            );
            i++;
        }

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CreateWaypointButton",
                new EventData().append("Action", "Create"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "Remove":
                if (data.waypointId != null && !data.waypointId.isEmpty()) {
                    // Get per-world data
                    String worldName = player.getWorld().getName();
                    com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData perWorldData =
                            player.getPlayerConfigData().getPerWorldData(worldName);

                    MapMarker[] markers = perWorldData.getWorldMapMarkers();
                    if (markers == null || markers.length == 0) {
                        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("You don't have any waypoints in this world."));
                        break;
                    }

                    java.util.List<MapMarker> updatedMarkers = new java.util.ArrayList<>();
                    boolean removed = false;

                    for (MapMarker marker : markers) {
                        if (!marker.id.equals(data.waypointId)) {
                            updatedMarkers.add(marker);
                        } else {
                            removed = true;
                        }
                    }

                    if (!removed) {
                        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("No waypoint was found with that ID."));
                        break;
                    }

                    perWorldData.setWorldMapMarkers(updatedMarkers.toArray(new MapMarker[0]));
                    player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Waypoint removed successfully."));
                    // Optionally refresh the page
                    player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, updatedMarkers.toArray(new MapMarker[0])));
                }
                break;
            case "Create":
                player.getPageManager().openCustomPage(ref, store, new AddWaypointPage(playerRef));
                break;
            default:
                break;
        }

    }
}