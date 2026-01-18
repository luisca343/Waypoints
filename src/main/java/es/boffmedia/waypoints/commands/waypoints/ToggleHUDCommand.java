package es.boffmedia.waypoints.commands.waypoints;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import es.boffmedia.waypoints.hud.CoordinatesHUD;
import es.boffmedia.waypoints.hud.EmptyHUD;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Command to toggle the coordinates HUD on/off
 */
public class ToggleHUDCommand extends AbstractPlayerCommand {

    // Store active HUD update tasks per player
    private static final Map<UUID, ScheduledFuture<?>> activeHudTasks = new ConcurrentHashMap<>();

    public ToggleHUDCommand() {
        super("togglehud", "Toggle the coordinates HUD display");
        addAliases("hud");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = commandContext.senderAs(Player.class);
        
        // Check if player is still online
        if (player == null) {
            return;
        }


        UUID playerUUID = player.getUuid();
        HudManager hudManager = player.getHudManager();
        
        // Check if HUD task is currently active for this player
        boolean isEnabled = activeHudTasks.containsKey(playerUUID);
        
        if (isEnabled) {
            // Disable HUD - cancel the update task
            ScheduledFuture<?> task = activeHudTasks.remove(playerUUID);
            if (task != null) {
                task.cancel(false);
            }
            
            EmptyHUD emptyHud = new EmptyHUD(playerRef);
            hudManager.setCustomHud(playerRef, emptyHud);
            player.sendMessage(Message.raw("Coordinates HUD disabled"));
        } else {
            // Enable HUD - get initial position and show it immediately
            Vector3d position = playerRef.getTransform().getPosition();
            
            CoordinatesHUD initialHud = new CoordinatesHUD(playerRef, position.x, position.y, position.z);
            hudManager.setCustomHud(playerRef, initialHud);
            
            // Start scheduled updates
            ScheduledFuture<?> updateTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
                world.execute(() -> {
                    try {
                        // Check if this task should still be running
                        if (!activeHudTasks.containsKey(playerUUID)) {
                            return; // Task was cancelled, stop executing
                        }
                        
                        final Ref<EntityStore> playerRefEntity = player.getReference();
                        if (playerRefEntity != null && playerRefEntity.isValid()) {
                            final Store<EntityStore> playerStore = playerRefEntity.getStore();
                            final PlayerRef playerRefComponent = playerStore.getComponent(playerRefEntity, PlayerRef.getComponentType());
                            final Vector3d pos = playerRefComponent.getTransform().getPosition();
                            
                            // Create new HUD instance with updated coordinates
                            final CoordinatesHUD newHud = new CoordinatesHUD(playerRefComponent, pos.x, pos.y, pos.z);
                            player.getHudManager().setCustomHud(playerRefComponent, newHud);
                        } else {
                            // Player disconnected, cancel task
                            ScheduledFuture<?> task = activeHudTasks.remove(playerUUID);
                            if (task != null) {
                                task.cancel(false);
                            }
                        }
                    } catch (Exception e) {
                        // Error occurred, cancel task
                        ScheduledFuture<?> task = activeHudTasks.remove(playerUUID);
                        if (task != null) {
                            task.cancel(false);
                        }
                    }
                });
            }, 50L, 50L , TimeUnit.MILLISECONDS);
            
            activeHudTasks.put(playerUUID, updateTask);
            player.sendMessage(Message.raw("Coordinates HUD enabled"));
        }
    }
}
