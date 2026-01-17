package es.boffmedia.waypoints.pages;

import com.hypixel.hytale.builtin.buildertools.tooloperations.transform.Translate;
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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import es.boffmedia.waypoints.Constants;
import es.boffmedia.waypoints.Icons;

import javax.annotation.Nonnull;

public class AddWaypointPage extends InteractiveCustomUIPage<AddWaypointPage.AddWaypointPageData> {

    private String selectedIcon = "Coordinate.png";

    public static class AddWaypointPageData {
        public String action;
        public String name;
        public String x;
        public String y;
        public String z;
        public String selectedEntry;

        public static final BuilderCodec<AddWaypointPageData> CODEC = ((BuilderCodec.Builder<AddWaypointPageData>) ((BuilderCodec.Builder<AddWaypointPageData>)
                ((BuilderCodec.Builder<AddWaypointPageData>) ((BuilderCodec.Builder<AddWaypointPageData>)
                        ((BuilderCodec.Builder<AddWaypointPageData>) ((BuilderCodec.Builder<AddWaypointPageData>)
                                BuilderCodec.builder(AddWaypointPageData.class, AddWaypointPageData::new))
                                .append(new KeyedCodec<>("Action", Codec.STRING), (AddWaypointPageData o, String v) -> o.action = v, (AddWaypointPageData o) -> o.action)
                                .add()
                                .append(new KeyedCodec<>("@Name", Codec.STRING), (AddWaypointPageData o, String v) -> o.name = v, (AddWaypointPageData o) -> o.name)
                                .add())
                        .append(new KeyedCodec<>("@X", Codec.STRING), (AddWaypointPageData o, String v) -> o.x = v, (AddWaypointPageData o) -> o.x)
                        .add())
                .append(new KeyedCodec<>("@Y", Codec.STRING), (AddWaypointPageData o, String v) -> o.y = v, (AddWaypointPageData o) -> o.y)
                .add())
                .append(new KeyedCodec<>("@Z", Codec.STRING), (AddWaypointPageData o, String v) -> o.z = v, (AddWaypointPageData o) -> o.z)
                .add())
                .append(new KeyedCodec<>("@SelectedEntry", Codec.STRING), (AddWaypointPageData o, String v) -> o.selectedEntry = v, (AddWaypointPageData o) -> o.selectedEntry)
                .add())
                .build();
    }

    public AddWaypointPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, AddWaypointPageData.CODEC);
    }

    public void setSelectedIcon(String iconFileName) {
        this.selectedIcon = iconFileName;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/AddWaypointPage.ui");

        // Add icon dropdown field
        uiCommandBuilder.append("#IconDropdownContainer", "Pages/IconDropdownField.ui");

        // Get player's current position to pre-fill coordinates
        Player player = store.getComponent(ref, Player.getComponentType());
        TransformComponent transformComponent = store.getComponent(player.getReference(), TransformComponent.getComponentType());
        Position position = transformComponent.getSentTransform().position;

        // Set default coordinates to player's current position
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
        // Set default icon to Home
        uiCommandBuilder.set("#IconDropdown.Value", Constants.ICON_PATH_PREFIX + selectedIcon);

        // Add event binding for Add button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddButton",
                new EventData()
                        .append("Action", "Add")
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
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull AddWaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "Add":
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

                    // Create the waypoint
                    Position position = new Position(x, y, z);
                    Direction orientation = new Direction(0.0F, 0.0F, 0.0F);
                    Transform transform = new Transform(position, orientation);

                    String waypointId = data.name.toLowerCase().replaceAll("\\s+", "_");
                    
                    ContextMenuItem[] items = new ContextMenuItem[]{
                            new ContextMenuItem("Remove Waypoint", "waypoint remove " + data.name)
                    };

                    MapMarker playerMarker = new MapMarker(waypointId, data.name, selectedIcon, transform, items);

                    WorldMapManager.PlayerMarkerReference playerMarkerReference = WorldMapManager.createPlayerMarker(player.getReference(), playerMarker, store);

                    player.sendMessage(Message.raw("Waypoint created: " + data.name));

                    // Return to waypoint list
                    String worldName = player.getWorld().getName();
                    com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData perWorldData =
                            player.getPlayerConfigData().getPerWorldData(worldName);
                    MapMarker[] markers = perWorldData.getWorldMapMarkers();
                    
                    player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, markers != null ? markers : new MapMarker[0]));

                } catch (NumberFormatException e) {
                    player.sendMessage(Message.raw("Error: Invalid coordinates. Please enter valid numbers. Coordinates sent: X=" + data.x + ", Y=" + data.y + ", Z=" + data.z));
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
