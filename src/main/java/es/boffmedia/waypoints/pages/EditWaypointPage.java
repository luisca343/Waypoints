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
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import es.boffmedia.waypoints.Constants;
import es.boffmedia.waypoints.Icons;

import javax.annotation.Nonnull;

public class EditWaypointPage extends InteractiveCustomUIPage<EditWaypointPage.EditWaypointPageData> {

    private final MapMarker waypoint;
    private String selectedIcon;

    public static class EditWaypointPageData {
        public String action;
        public String name;
        public String x;
        public String y;
        public String z;
        public String selectedEntry;

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
                .add())
                .append(new KeyedCodec<>("@SelectedEntry", Codec.STRING), (EditWaypointPageData o, String v) -> o.selectedEntry = v, (EditWaypointPageData o) -> o.selectedEntry)
                .add())
                .build();
    }

    public EditWaypointPage(@Nonnull PlayerRef playerRef, MapMarker waypoint) {
        super(playerRef, CustomPageLifetime.CanDismiss, EditWaypointPageData.CODEC);
        this.waypoint = waypoint;
        this.selectedIcon = waypoint.markerImage;
    }

    public void setSelectedIcon(String iconFileName) {
        this.selectedIcon = iconFileName;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/EditWaypointPage.ui");

        // Pre-fill with current waypoint data
        Position position = waypoint.transform.position;

        uiCommandBuilder.set("#WaypointNameInput.Value", waypoint.name);
        uiCommandBuilder.set("#XInput.Value", String.format("%.2f", position.x));
        uiCommandBuilder.set("#YInput.Value", String.format("%.2f", position.y));
        uiCommandBuilder.set("#ZInput.Value", String.format("%.2f", position.z));

        // Populate icon dropdown
        java.util.List<Icons.Icon> icons = Icons.getDefaultIcons();
        DropdownEntryInfo[] iconEntries = new DropdownEntryInfo[icons.size()];
        int selectedIndex = 0;
        for (int i = 0; i < icons.size(); i++) {
            Icons.Icon icon = icons.get(i);
            iconEntries[i] = new DropdownEntryInfo(LocalizableString.fromString(icon.getDisplayName()), Constants.ICON_PATH_PREFIX + icon.getFileName());
            if (icon.getFileName().equals(selectedIcon)) {
                selectedIndex = i;
            }
        }
        uiCommandBuilder.set("#IconDropdown.Entries", iconEntries);
        // Set initial dropdown value based on current waypoint icon
        uiCommandBuilder.set("#IconDropdown.Value", Constants.ICON_PATH_PREFIX + selectedIcon);

        // Add event binding for Save button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                new EventData()
                        .append("Action", "Save")
                        .append("@Name", "#WaypointNameInput.Value")
                        .append("@X", "#XInput.Value")
                        .append("@Y", "#YInput.Value")
                        .append("@Z", "#ZInput.Value")
                        .append("@SelectedEntry", "#IconDropdown.Value"),
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
            case "Save":
                // Extract icon from selectedEntry
                if (data.selectedEntry != null && !data.selectedEntry.isEmpty()) {
                    String iconPath = data.selectedEntry;
                    if (iconPath.startsWith(Constants.ICON_PATH_PREFIX)) {
                        selectedIcon = iconPath.substring(Constants.ICON_PATH_PREFIX.length());
                    } else {
                        selectedIcon = iconPath;
                    }
                }
                
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
                    player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, updatedMarkers.toArray(new MapMarker[0])));

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
                
                player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, markers != null ? markers : new MapMarker[0]));
                break;

            default:
                break;
        }
    }
}
