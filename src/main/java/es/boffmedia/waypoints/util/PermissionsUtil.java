package es.boffmedia.waypoints.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import es.boffmedia.waypoints.Constants;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for checking permissions safely
 */
public final class PermissionsUtil {
    
    private PermissionsUtil() {
        // Utility class, no instantiation
    }

    public static boolean hasNegatedPermission(@Nonnull final Player player, @Nonnull final String permission) {
        final PermissionsModule perms = PermissionsModule.get();
        
        final UUID uuid = ((CommandSender)player).getUuid();
        return perms.hasPermission(uuid, "-" + permission);
    }
    
    /**
     * Check if a player has permission to open the waypoint UI
     * @param player the player to check
     * @return true if the player has permission
     */
    public static boolean canOpenWaypointUI(@Nonnull final Player player) {
        final PermissionsModule perms = PermissionsModule.get();
        
        final UUID uuid = ((CommandSender)player).getUuid();
        final Set<String> groups = perms.getGroupsForUser(uuid);
        
        // Check if player is OP or has the specific permission
        return (groups != null && groups.contains("OP")) 
            || !hasNegatedPermission(player, Constants.PERMISSION_WAYPOINT_OPEN);
    }
    
    /**
     * Check if a player has permission to teleport to waypoints
     * @param player the player to check
     * @return true if the player has permission
     */
    public static boolean canTeleport(@Nonnull final Player player) {
        final PermissionsModule perms = PermissionsModule.get();
        
        final UUID uuid = ((CommandSender)player).getUuid();
        final Set<String> groups = perms.getGroupsForUser(uuid);
        
        // Check if player is OP or has teleport permission
        return (groups != null && groups.contains("OP")) 
            || perms.hasPermission(uuid, Constants.PERMISSION_WAYPOINT_TELEPORT);
    }
    
    /**
     * Check if a player is an admin/operator
     * @param player the player to check
     * @return true if the player is an admin
     */
    public static boolean isAdmin(@Nonnull final Player player) {
        final PermissionsModule perms = PermissionsModule.get();
        
        final UUID uuid = ((CommandSender)player).getUuid();
        final Set<String> groups = perms.getGroupsForUser(uuid);
        
        return groups != null && groups.contains("OP");
    }
}
