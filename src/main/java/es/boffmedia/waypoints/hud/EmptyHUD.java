package es.boffmedia.waypoints.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

/**
 * Empty HUD used to "disable" the coordinates HUD without sending a null packet
 */
public class EmptyHUD extends CustomUIHud {

    public EmptyHUD(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
        // Build nothing - this clears the HUD without sending a problematic null packet
    }
}
