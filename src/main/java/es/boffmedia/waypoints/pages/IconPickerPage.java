package es.boffmedia.waypoints.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import es.boffmedia.waypoints.Constants;
import es.boffmedia.waypoints.Icons;

import javax.annotation.Nonnull;

public class IconPickerPage extends InteractiveCustomUIPage<IconPickerPage.IconPickerPageData> {
    
    private final InteractiveCustomUIPage<?> returnPage;
    private final String currentIcon;
    private final String ICON_GRID_REF = "#IconGrid";
    private final String ICON_GRID_ROW_UI = "Pages/IconGridRow.ui";
    private final String ICON_PICKER_ITEM_UI = "Pages/IconPickerItem.ui";
    private final String ICON_SPACER_UI = "Pages/IconSpacer.ui";
    
    public static class IconPickerPageData {
        public String action;
        public String iconFileName;

        public static final BuilderCodec<IconPickerPageData> CODEC = ((BuilderCodec.Builder<IconPickerPageData>) ((BuilderCodec.Builder<IconPickerPageData>)
                BuilderCodec.builder(IconPickerPageData.class, IconPickerPageData::new))
                .append(new KeyedCodec<>("Action", Codec.STRING), (IconPickerPageData o, String v) -> o.action = v, (IconPickerPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("IconFileName", Codec.STRING), (IconPickerPageData o, String v) -> o.iconFileName = v, (IconPickerPageData o) -> o.iconFileName)
                .add())
                .build();
    }

    public IconPickerPage(@Nonnull PlayerRef playerRef, String currentIcon, InteractiveCustomUIPage<?> returnPage) {
        super(playerRef, CustomPageLifetime.CanDismiss, IconPickerPageData.CODEC);
        this.currentIcon = currentIcon;
        this.returnPage = returnPage;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/IconPickerPage.ui");
        uiCommandBuilder.clear(ICON_GRID_REF);

        // Create a grid layout container
        java.util.List<Icons.Icon> icons = Icons.getDefaultIcons();
        int iconsPerRow = 6;
        int totalIcons = icons.size();
        int rows = (int) Math.ceil((double) totalIcons / iconsPerRow);

        for (int row = 0; row < rows; row++) {
            String rowSelector = ICON_GRID_REF + "[" + row + "]";
            
            // Create a row container
            uiCommandBuilder.append(ICON_GRID_REF, ICON_GRID_ROW_UI);

            for (int col = 0; col < iconsPerRow; col++) {
                int iconIndex = row * iconsPerRow + col;
                if (iconIndex >= totalIcons) break;

                Icons.Icon icon = icons.get(iconIndex);
                String iconSelector = rowSelector + "[" + col + "]";

                // Add icon button
                uiCommandBuilder.append(rowSelector, ICON_PICKER_ITEM_UI);
                uiCommandBuilder.set(iconSelector + " #IconButton.Text", icon.getDisplayName());

                // Highlight selected icon
                if (icon.getFileName().equals(currentIcon)) {
                    //uiCommandBuilder.set(iconSelector + " #IconButton.Background", "#3a7bd5(0.6)");
                }

                // Add event binding
                uiEventBuilder.addEventBinding(
                        CustomUIEventBindingType.Activating,
                        iconSelector + " #IconButton",
                        new EventData()
                                .append("Action", "Select")
                                .append("IconFileName", icon.getFileName()),
                        false
                );

                // Add spacing between icons
                if (col < iconsPerRow - 1 && iconIndex < totalIcons - 1) {
                    uiCommandBuilder.append(rowSelector, ICON_SPACER_UI);
                }
            }
        }

        // Add event binding for Back button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BackButton",
                new EventData().append("Action", "Back"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IconPickerPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "Select":
                if (returnPage instanceof AddWaypointPage) {
                    ((AddWaypointPage) returnPage).setSelectedIcon(data.iconFileName);
                } else if (returnPage instanceof EditWaypointPage) {
                    ((EditWaypointPage) returnPage).setSelectedIcon(data.iconFileName);
                }
                player.getPageManager().openCustomPage(ref, store, returnPage);
                break;

            case "Back":
                player.getPageManager().openCustomPage(ref, store, returnPage);
                break;

            default:
                break;
        }
    }
}
