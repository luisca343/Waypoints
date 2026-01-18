package es.boffmedia.waypoints;

public class Constants {
    public static final String ICON_PATH_PREFIX = "Markers/";
    // Permission node to control access to the /waypoint UI. Default granted.
    // Note: no mod id prefix — keep it simple and consistent with other permission nodes.
    public static final String PERMISSION_WAYPOINT_OPEN = "waypoints.command.waypoint";

    // Permission node required to see/use the teleport button
    public static final String PERMISSION_WAYPOINT_TELEPORT = "waypoints.command.teleport";
    
    // Default maximum waypoints value. Use -1 to indicate unlimited.
    public static final int DEFAULT_MAX_WAYPOINTS = -1;
}
