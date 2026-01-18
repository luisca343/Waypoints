package es.boffmedia.waypoints.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

/**
 * HUD that displays player's current coordinates in the top-right corner
 */
public class CoordinatesHUD extends CustomUIHud {
    
    private static final String HUD_UI_PATH = "HUD/CoordinatesHUD.ui";
    private final double x;
    private final double y;
    private final double z;

    public CoordinatesHUD(@Nonnull PlayerRef playerRef, double x, double y, double z) {
        super(playerRef);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder builder) {
        // Append the HUD UI
        builder.append(HUD_UI_PATH);
        
        // Set coordinate text
        String coordText = String.format("X: %.1f  Y: %.1f  Z: %.1f", x, y, z);
        builder.set("#CoordinateText.Text", coordText);
    }
}
