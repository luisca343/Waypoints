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
import es.boffmedia.waypoints.util.UIHelpers;

import javax.annotation.Nonnull;

public class AddWaypointPage extends InteractiveCustomUIPage<AddWaypointPage.AddWaypointPageData> {

    private String selectedIcon = "Coordinate.png";
    private String selectedIconDisplayName = "Coordinate";
    private String savedName = null;
    private String savedX = null;
    private String savedY = null;
    private String savedZ = null;

    public static class AddWaypointPageData {
        public String action;
        public String name;
        public String x;
        public String y;
        public String z;

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
                .add()))
                .build();
    }

    public AddWaypointPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, AddWaypointPageData.CODEC);
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
        uiCommandBuilder.append("Pages/AddWaypointPage.ui");

        // Get player's current position to pre-fill coordinates
        Player player = store.getComponent(ref, Player.getComponentType());
        TransformComponent transformComponent = store.getComponent(player.getReference(), TransformComponent.getComponentType());
        Position position = transformComponent.getSentTransform().position;

        // Set form values - use saved values if they exist, otherwise use defaults
        if (savedName != null) {
            uiCommandBuilder.set("#WaypointNameInput.Value", savedName);
        }
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

        // Add event binding for Add button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddButton",
                new EventData()
                        .append("Action", "Add")
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
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull AddWaypointPageData data) {
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

            case "Add":
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
