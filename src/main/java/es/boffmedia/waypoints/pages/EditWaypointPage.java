package es.boffmedia.waypoints.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.worldmap.ContextMenuItem;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import es.boffmedia.waypoints.Icons;
import es.boffmedia.waypoints.config.WaypointsConfig;

import javax.annotation.Nonnull;

public class EditWaypointPage extends InteractiveCustomUIPage<EditWaypointPage.EditWaypointPageData> {

    private final MapMarker waypoint;
    private final Config<WaypointsConfig> config;
    private String selectedIcon;
    private String selectedIconDisplayName;
    private String savedName = null;
    private String savedX = null;
    private String savedY = null;
    private String savedZ = null;

    public static class EditWaypointPageData {
        public String action;
        public String name;
        public String x;
        public String y;
        public String z;

        public static final BuilderCodec<EditWaypointPageData> CODEC = ((BuilderCodec.Builder<EditWaypointPageData>) ((BuilderCodec.Builder<EditWaypointPageData>)
                ((BuilderCodec.Builder<EditWaypointPageData>) ((BuilderCodec.Builder<EditWaypointPageData>)
                        ((BuilderCodec.Builder<EditWaypointPageData>) ((BuilderCodec.Builder<EditWaypointPageData>)
                                BuilderCodec.builder(EditWaypointPageData.class, EditWaypointPageData::new))
                                .append(new KeyedCodec<>("Action", Codec.STRING), (EditWaypointPageData o, String v) -> o.action = v, (EditWaypointPageData o) -> o.action)
                                .add()
                                .append(new KeyedCodec<>("@Name", Codec.STRING), (EditWaypointPageData o, String v) -> o.name = v, (EditWaypointPageData o) -> o.name)
                                .add())
                        .append(new KeyedCodec<>("@X", Codec.STRING), (EditWaypointPageData o, String v) -> o.x = v, (EditWaypointPageData o) -> o.x)
                        .add())
                .append(new KeyedCodec<>("@Y", Codec.STRING), (EditWaypointPageData o, String v) -> o.y = v, (EditWaypointPageData o) -> o.y)
                .add())
                .append(new KeyedCodec<>("@Z", Codec.STRING), (EditWaypointPageData o, String v) -> o.z = v, (EditWaypointPageData o) -> o.z)
                .add()))
                .build();
    }

    public EditWaypointPage(@Nonnull PlayerRef playerRef, MapMarker waypoint, Config<WaypointsConfig> config) {
        super(playerRef, CustomPageLifetime.CanDismiss, EditWaypointPageData.CODEC);
        this.waypoint = waypoint;
        this.config = config;
        this.selectedIcon = waypoint.markerImage;
        // Find display name for current icon
        this.selectedIconDisplayName = "Unknown";
        for (Icons.Icon icon : Icons.getDefaultIcons()) {
            if (icon.getFileName().equals(waypoint.markerImage)) {
                this.selectedIconDisplayName = icon.getDisplayName();
                break;
            }
        }
    }

    public void setSelectedIcon(String iconFileName) {
        this.selectedIcon = iconFileName;
        // Update display name based on filename
        for (Icons.Icon icon : Icons.getDefaultIcons()) {
            if (icon.getFileName().equals(iconFileName)) {
                this.selectedIconDisplayName = icon.getDisplayName();
                break;
            }
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/EditWaypointPage.ui");

        // Pre-fill with current waypoint data or saved values
        Position position = waypoint.transform.position;

        uiCommandBuilder.set("#WaypointNameInput.Value", savedName != null ? savedName : waypoint.name);
        uiCommandBuilder.set("#XInput.Value", savedX != null ? savedX : String.format("%.2f", position.x));
        uiCommandBuilder.set("#YInput.Value", savedY != null ? savedY : String.format("%.2f", position.y));
        uiCommandBuilder.set("#ZInput.Value", savedZ != null ? savedZ : String.format("%.2f", position.z));

        // Set selected icon display name
        uiCommandBuilder.set("#SelectedIconLabel.Text", selectedIconDisplayName);

        // Add event binding for Choose Icon button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ChooseIconButton",
                new EventData()
                        .append("Action", "ChooseIcon")
                        .append("@Name", "#WaypointNameInput.Value")
                        .append("@X", "#XInput.Value")
                        .append("@Y", "#YInput.Value")
                        .append("@Z", "#ZInput.Value"),
                false
        );

        // Add event binding for Save button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                new EventData()
                        .append("Action", "Save")
                        .append("@Name", "#WaypointNameInput.Value")
                        .append("@X", "#XInput.Value")
                        .append("@Y", "#YInput.Value")
                        .append("@Z", "#ZInput.Value"),
                false
        );

        // Add event binding for Cancel button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CancelButton",
                new EventData().append("Action", "Cancel"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EditWaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "ChooseIcon":
                // Save current form values before opening icon picker
                savedName = data.name;
                savedX = data.x;
                savedY = data.y;
                savedZ = data.z;
                
                // Open icon picker page
                IconPickerPage iconPickerPage = new IconPickerPage(playerRef, selectedIcon, this);
                player.getPageManager().openCustomPage(ref, store, iconPickerPage);
                break;

            case "Save":
                if (data.name == null || data.name.trim().isEmpty()) {
                    player.sendMessage(Message.raw("Error: Waypoint name cannot be empty."));
                    return;
                }

                try {
                    // Parse coordinates
                    float x = Float.parseFloat(data.x);
                    float y = Float.parseFloat(data.y);
                    float z = Float.parseFloat(data.z);

                    // Get per-world data
                    String worldName = player.getWorld().getName();
                    com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData perWorldData =
                            player.getPlayerConfigData().getPerWorldData(worldName);

                    MapMarker[] markers = perWorldData.getWorldMapMarkers();
                    if (markers == null || markers.length == 0) {
                        player.sendMessage(Message.raw("You don't have any waypoints in this world."));
                        break;
                    }

                    // Update the waypoint
                    java.util.List<MapMarker> updatedMarkers = new java.util.ArrayList<>();
                    boolean found = false;

                    for (MapMarker marker : markers) {
                        if (marker.id.equals(waypoint.id)) {
                            // Create updated waypoint
                            Position newPosition = new Position(x, y, z);
                            Direction orientation = new Direction(0.0F, 0.0F, 0.0F);
                            Transform newTransform = new Transform(newPosition, orientation);

                            ContextMenuItem[] items = new ContextMenuItem[]{
                                    new ContextMenuItem("Remove Waypoint", "waypoint remove " + data.name)
                            };

                            MapMarker updatedMarker = new MapMarker(
                                    waypoint.id, // Keep the same ID
                                    data.name,
                                    selectedIcon,
                                    newTransform,
                                    items
                            );

                            updatedMarkers.add(updatedMarker);
                            found = true;
                        } else {
                            updatedMarkers.add(marker);
                        }
                    }

                    if (!found) {
                        player.sendMessage(Message.raw("No waypoint was found with that ID."));
                        break;
                    }

                    perWorldData.setWorldMapMarkers(updatedMarkers.toArray(new MapMarker[0]));
                    player.sendMessage(Message.raw("Waypoint updated successfully: " + data.name));

                    // Return to waypoint list
                    player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, updatedMarkers.toArray(new MapMarker[0]), config));

                } catch (NumberFormatException e) {
                    player.sendMessage(Message.raw("Error: Invalid coordinates. Please enter valid numbers."));
                }
                break;

            case "Cancel":
                // Return to waypoint list
                String worldName = player.getWorld().getName();
                com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData perWorldData =
                        player.getPlayerConfigData().getPerWorldData(worldName);
                MapMarker[] markers = perWorldData.getWorldMapMarkers();
                
                player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, markers != null ? markers : new MapMarker[0], config));
                break;

            default:
                break;
        }
    }
}
