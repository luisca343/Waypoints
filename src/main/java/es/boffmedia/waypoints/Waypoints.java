package es.boffmedia.waypoints;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import es.boffmedia.waypoints.commands.waypoints.WaypointCommand;
import es.boffmedia.waypoints.commands.waypoints.ListWaypointsCommand;
import es.boffmedia.waypoints.commands.waypoints.ResetWaypointsCommand;
import es.boffmedia.waypoints.commands.waypoints.WaypointTeleportCommand;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class Waypoints extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Waypoints(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.getCommandRegistry().registerCommand(new WaypointCommand());
        this.getCommandRegistry().registerCommand(new ResetWaypointsCommand());
        this.getCommandRegistry().registerCommand(new ListWaypointsCommand());
        this.getCommandRegistry().registerCommand(new WaypointTeleportCommand());

    }
}