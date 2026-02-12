package es.boffmedia.waypoints.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.server.core.util.Config;
import es.boffmedia.waypoints.Constants;
import es.boffmedia.waypoints.config.WaypointsConfig;
import es.boffmedia.waypoints.util.PermissionsUtil;
import es.boffmedia.waypoints.IconNames;

import javax.annotation.Nonnull;
import java.util.Set;

public class WaypointPage extends InteractiveCustomUIPage<WaypointPage.WaypointPageData> {
    private final MapMarker[] waypoints;
    private final Config<WaypointsConfig> config;
    private final String initialQuery;
    private final String WAYPOINTS_LIST_REF = "#WaypointsList";
    private final String WAYPOINT_ITEM_UI = "Pages/WaypointItem.ui";
    // Simple debounce to avoid processing every single keystroke too rapidly
    private long lastSearchTimestamp = 0L;
    private String lastSearchQuery = "";
    // current sort mode: "distance" or "name"
    private String currentSort = "distance";
    

    public static class WaypointPageData {
        public String action;
        public String waypointId;
        public String query;
        public String sort;

        public static final BuilderCodec<WaypointPageData> CODEC = ((BuilderCodec.Builder<WaypointPageData>) ((BuilderCodec.Builder<WaypointPageData>)
                BuilderCodec.builder(WaypointPageData.class, WaypointPageData::new))
                .append(new KeyedCodec<>("Action", Codec.STRING), (WaypointPageData o, String v) -> o.action = v, (WaypointPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("WaypointId", Codec.STRING), (WaypointPageData o, String v) -> o.waypointId = v, (WaypointPageData o) -> o.waypointId)
                .add())
                .append(new KeyedCodec<>("@Query", Codec.STRING), (WaypointPageData o, String v) -> o.query = v, (WaypointPageData o) -> o.query)
                .add()
                .append(new KeyedCodec<>("@Sort", Codec.STRING), (WaypointPageData o, String v) -> o.sort = v, (WaypointPageData o) -> o.sort)
                .add()
            .build();
    }

    // Refresh only the waypoint list in-place using UICommandBuilder/UIEventBuilder
    private void refreshWaypoints(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, MapMarker[] markers, String query) {
        UICommandBuilder ui = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();

        ui.clear(WAYPOINTS_LIST_REF);

        // Restore search input value
        if (query != null) {
            ui.set("#SearchInput.Value", query);
        }

        // Player and position
        Player player = store.getComponent(ref, Player.getComponentType());
        TransformComponent transformComponent = store.getComponent(player.getReference(), TransformComponent.getComponentType());
        Position playerPosition = transformComponent.getSentTransform().position;

        boolean canTeleport = PermissionsUtil.canTeleport(player);

        java.util.List<WaypointWithDistance> waypointsWithDistance = new java.util.ArrayList<>();
        if (markers != null) {
            for (MapMarker waypoint : markers) {
                Position waypointPosition = waypoint.transform.position;
                double distance = calculateDistance(playerPosition, waypointPosition);
                waypointsWithDistance.add(new WaypointWithDistance(waypoint, distance));
            }
        }

        // Sort according to currentSort
        if ("name".equalsIgnoreCase(this.currentSort)) {
            waypointsWithDistance.sort(java.util.Comparator.comparing(w -> w.waypoint.name != null ? w.waypoint.name.toLowerCase() : ""));
        } else {
            waypointsWithDistance.sort(java.util.Comparator.comparingDouble(w -> w.distance));
        }

        if (waypointsWithDistance.size() == 0) {
            ui.appendInline(WAYPOINTS_LIST_REF, "Label { Text: \"No waypoints\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
            this.sendUpdate(ui, events, false);
            return;
        }

        int i = 0;
        for (WaypointWithDistance waypointData : waypointsWithDistance) {
            String selector = "#WaypointsList[" + i + "]";
            ui.append(WAYPOINTS_LIST_REF, WAYPOINT_ITEM_UI);

            String waypointName = waypointData.waypoint.name;
            String waypointId = waypointData.waypoint.id;
            String waypointIcon = waypointData.waypoint.markerImage;
            Position waypointPos = waypointData.waypoint.transform.position;
            String coordinatesText = String.format("X: %.0f  Y: %.0f  Z: %.0f  -  %.1f blocks away", 
                waypointPos.x, waypointPos.y, waypointPos.z, waypointData.distance);

            ui.set(selector + " #WaypointName.Text", waypointName);
            ui.set(selector + " #WaypointCoordinates.Text", coordinatesText);

            String iconUiPath = IconNames.resolveIconUiPath(waypointIcon);
            ui.append(selector + " #IconContainer", iconUiPath);

            ui.set(selector + " #TeleportButton.Visible", canTeleport);

            if (canTeleport) {
                events.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        selector + " #TeleportButton",
                        new EventData().append("Action", "Teleport").append("WaypointId", waypointId),
                        false
                );
            }

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #EditButton",
                    new EventData().append("Action", "Edit").append("WaypointId", waypointId),
                    false
            );

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #RemoveButton",
                    new EventData().append("Action", "Remove").append("WaypointId", waypointId),
                    false
            );

            i++;
        }

        // Keep Create button binding (no selector changes needed)
        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CreateWaypointButton",
                new EventData().append("Action", "Create"),
                false
        );

        this.sendUpdate(ui, events, false);
    }

    public WaypointPage(@Nonnull PlayerRef playerRef, MapMarker[] waypoints, Config<WaypointsConfig> config) {
        this(playerRef, waypoints, config, "");
    }

    public WaypointPage(@Nonnull PlayerRef playerRef, MapMarker[] waypoints, Config<WaypointsConfig> config, String initialQuery) {
        super(playerRef, CustomPageLifetime.CanDismiss, WaypointPageData.CODEC);
        this.waypoints = waypoints;
        this.config = config;
        this.initialQuery = initialQuery != null ? initialQuery : "";
    }

    

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/WaypointPage.ui");
        uiCommandBuilder.clear(WAYPOINTS_LIST_REF);

        // Bind search field to trigger server-side filtering
        // Live typing: send Search action on value changes so list updates while typing
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#SearchInput",
            new EventData().append("Action", "Search").append("@Query", "#SearchInput.Value"),
            false
        );

        // Populate sort dropdown entries and bind changes
        DropdownEntryInfo[] sortEntries = new DropdownEntryInfo[]{
            new DropdownEntryInfo(LocalizableString.fromString("Distance"), "distance"),
            new DropdownEntryInfo(LocalizableString.fromString("Name"), "name")
        };
        uiCommandBuilder.set("#SortDropdown.Entries", sortEntries);
        uiCommandBuilder.set("#SortDropdown.Value", this.currentSort);
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#SortDropdown",
            new EventData().append("Action", "Sort").append("@Sort", "#SortDropdown.Value"),
            false
        );

        if(waypoints.length == 0){
            uiCommandBuilder.appendInline(WAYPOINTS_LIST_REF, "Label { Text: \"No waypoints\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
        }

        // Restore search input value so typing doesn't disappear when the page is rebuilt
        if (this.initialQuery != null && !this.initialQuery.isEmpty()) {
            uiCommandBuilder.set("#SearchInput.Value", this.initialQuery);
        }

        // Get player's current position
        Player player = store.getComponent(ref, Player.getComponentType());
        TransformComponent transformComponent = store.getComponent(player.getReference(), TransformComponent.getComponentType());
        Position playerPosition = transformComponent.getSentTransform().position;

        // Check if player can teleport using the new utility
        boolean canTeleport = PermissionsUtil.canTeleport(player);

        // Calculate distances and create sorted list
        java.util.List<WaypointWithDistance> waypointsWithDistance = new java.util.ArrayList<>();
        for (MapMarker waypoint : waypoints) {
            Position waypointPosition = waypoint.transform.position;
            double distance = calculateDistance(playerPosition, waypointPosition);
            waypointsWithDistance.add(new WaypointWithDistance(waypoint, distance));
        }

        // Sort according to currentSort
        if ("name".equalsIgnoreCase(this.currentSort)) {
            waypointsWithDistance.sort(java.util.Comparator.comparing(w -> {
                String n = w.waypoint.name != null ? w.waypoint.name : "";
                return n.toLowerCase();
            }));
        } else {
            // default: distance
            waypointsWithDistance.sort(java.util.Comparator.comparingDouble(w -> w.distance));
        }

        int i = 0;

        for (WaypointWithDistance waypointData : waypointsWithDistance) {

            String selector = "#WaypointsList[" + i + "]";
            uiCommandBuilder.append(WAYPOINTS_LIST_REF, WAYPOINT_ITEM_UI);

            String waypointName = waypointData.waypoint.name;
            String waypointId = waypointData.waypoint.id;
            String waypointIcon = waypointData.waypoint.markerImage;
            Position waypointPos = waypointData.waypoint.transform.position;
            String coordinatesText = String.format("X: %.0f  Y: %.0f  Z: %.0f  -  %.1f blocks away", 
                waypointPos.x, waypointPos.y, waypointPos.z, waypointData.distance);

            uiCommandBuilder.set(selector + " #WaypointName.Text", waypointName);
            uiCommandBuilder.set(selector + " #WaypointCoordinates.Text", coordinatesText);

            String iconUiPath = IconNames.resolveIconUiPath(waypointIcon);
            uiCommandBuilder.append(selector + " #IconContainer", iconUiPath);


            // Show/hide TP button based on permission
            uiCommandBuilder.set(selector + " #TeleportButton.Visible", canTeleport);

            if (canTeleport) {
                uiEventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        selector + " #TeleportButton",
                        new EventData().append("Action", "Teleport").append("WaypointId", waypointId),
                        false
                );
            }

            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #EditButton",
                    new EventData().append("Action", "Edit").append("WaypointId", waypointId),
                    false
            );

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
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Close"),
            false
        );
    }

    /**
     * Calculate the Euclidean distance between two positions
     */
    private double calculateDistance(Position pos1, Position pos2) {
        double dx = pos2.x - pos1.x;
        double dy = pos2.y - pos1.y;
        double dz = pos2.z - pos1.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Helper class to store waypoint with its calculated distance
     */
    private static class WaypointWithDistance {
        final MapMarker waypoint;
        final double distance;

        WaypointWithDistance(MapMarker waypoint, double distance) {
            this.waypoint = waypoint;
            this.distance = distance;
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "Teleport":
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

                    // Find the waypoint to teleport to
                    MapMarker waypoint = null;
                    for (MapMarker marker : markers) {
                        if (marker.id.equals(data.waypointId)) {
                            waypoint = marker;
                            break;
                        }
                    }

                    if (waypoint == null) {
                        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("No waypoint was found with that ID."));
                        break;
                    }

                    // Teleport to waypoint
                    com.hypixel.hytale.math.vector.Vector3d targetPos = new com.hypixel.hytale.math.vector.Vector3d(
                            waypoint.transform.position.x,
                            waypoint.transform.position.y,
                            waypoint.transform.position.z
                    );
                    com.hypixel.hytale.math.vector.Vector3f rotation = new com.hypixel.hytale.math.vector.Vector3f(0.0F, 0.0F, 0.0F);
                    com.hypixel.hytale.server.core.modules.entity.teleport.Teleport teleport = new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(targetPos, rotation);
                    store.addComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(), teleport);

                    player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Teleported to '" + waypoint.name + "'!"));
                }
                break;
            case "Edit":
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

                    // Find the waypoint to edit
                    MapMarker waypointToEdit = null;
                    for (MapMarker marker : markers) {
                        if (marker.id.equals(data.waypointId)) {
                            waypointToEdit = marker;
                            break;
                        }
                    }

                    if (waypointToEdit == null) {
                        player.sendMessage(com.hypixel.hytale.server.core.Message.raw("No waypoint was found with that ID."));
                        break;
                    }

                    // Open edit page
                    player.getPageManager().openCustomPage(ref, store, new EditWaypointPage(playerRef, waypointToEdit, config));
                }
                break;
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
                    player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, updatedMarkers.toArray(new MapMarker[0]), config));
                }
                break;
            case "Create":
                player.getPageManager().openCustomPage(ref, store, new AddWaypointPage(playerRef, config));
                break;
            case "Search":
                // Debug: show received query and total waypoints
                String q = data.query != null ? data.query.trim() : "";
                // Debounce and skip identical queries to avoid unnecessary work
                long now = System.currentTimeMillis();
                if (q.equals(this.lastSearchQuery) && (now - this.lastSearchTimestamp) < 1000) {
                    break;
                }
                this.lastSearchTimestamp = now;
                this.lastSearchQuery = q;

                // If empty, restore full list from per-world markers (in-place update)
                if (q.isEmpty()) {
                    String worldName = player.getWorld().getName();
                    com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData perWorldData =
                            player.getPlayerConfigData().getPerWorldData(worldName);
                    MapMarker[] markers = perWorldData != null ? perWorldData.getWorldMapMarkers() : null;
                    // debug logging removed
                    refreshWaypoints(ref, store, markers != null ? markers : new MapMarker[0], "");
                    break;
                }

                String qLower = q.toLowerCase();
                java.util.List<MapMarker> filtered = new java.util.ArrayList<>();
                for (MapMarker m : this.waypoints) {
                    String name = m.name != null ? m.name : "";
                    String id = m.id != null ? m.id : "";
                    if (name.toLowerCase().contains(qLower) || id.toLowerCase().contains(qLower)) {
                        filtered.add(m);
                    }
                }
                // debug logging removed

                // If the filtered results are identical to the currently-displayed list (by id and order), skip reopening
                boolean sameAsCurrent = false;
                if (this.waypoints != null && this.waypoints.length == filtered.size()) {
                    sameAsCurrent = true;
                    for (int idx = 0; idx < filtered.size(); idx++) {
                        MapMarker fm = filtered.get(idx);
                        MapMarker cm = this.waypoints[idx];
                        String fid = fm != null && fm.id != null ? fm.id : "";
                        String cid = cm != null && cm.id != null ? cm.id : "";
                        if (!fid.equals(cid)) {
                            sameAsCurrent = false;
                            break;
                        }
                    }
                }

                if (sameAsCurrent) {
                    // results unchanged — skip rebuild to preserve focus
                    break;
                }

                MapMarker[] arr = filtered.toArray(new MapMarker[0]);
                // Update list in-place
                refreshWaypoints(ref, store, arr, q);
                break;
                case "Sort":
                String sortMode = data.sort != null ? data.sort : "distance";
                this.currentSort = sortMode;
                // Recompute markers from per-world data and refresh
                String worldNameSort = player.getWorld().getName();
                com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData perWorldDataSort =
                    player.getPlayerConfigData().getPerWorldData(worldNameSort);
                MapMarker[] markersSort = perWorldDataSort != null ? perWorldDataSort.getWorldMapMarkers() : new MapMarker[0];
                refreshWaypoints(ref, store, markersSort, this.lastSearchQuery != null ? this.lastSearchQuery : "");
                break;
            case "Close":
                this.close();
                break;
            default:
                break;
        }

    }
}