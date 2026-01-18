# Waypoints

A waypoint management system for Hytale with an in-game UI.

![Compass](https://i.imgur.com/4CCCdaT.png) ![Waypoints main view](https://i.imgur.com/lqG4f1F.jpeg)

## Features

*   **UI-based waypoint manager** - Create, edit, and remove waypoints through a graphical interface
*   **Distance display** - Shows distance in blocks from your current position to each waypoint
*   **Auto-sorting** - Waypoints are sorted by distance, closest first
*   **Teleportation** - Optional teleport button (Creative mode or with permission)
*   **Map markers** - Waypoints appear on your in-game map
*   **Coordinates HUD** - Toggle a HUD display showing your current coordinates in real-time

## Usage

Use `/waypoint` or `/wp` to open the waypoint manager.

The UI lets you:

*   Create waypoints at your current location or custom coordinates
*   Edit waypoint names and coordinates
*   Delete waypoints
*   Teleport to waypoints (if permitted)

Each waypoint shows three buttons:

*   **TP** (green) - Teleport to waypoint
*   **EDIT** (blue) - Modify waypoint
*   **REMOVE** (red) - Delete waypoint

![Waypoint edit view](https://i.imgur.com/zLlJpmr.jpeg)

### Coordinates HUD

Toggle the coordinates HUD with `/waypoint togglehud` or `/waypoint hud`. This displays your current X, Y, Z coordinates in the top-right corner of your screen in real-time.

## Permissions

- `waypoints.command.waypoint` — Access to the `/waypoint` UI. Granted by default; server admins can revoke or grant access.
- `waypoints.command.teleport` — Required to use the teleport command/button.
- `waypoints.command.hud` — Permission to toggle the coordinates HUD.

### Granting Permissions

*   **On servers**: Example commands to manage these permissions:

```
/perm group add Default -waypoints.command.waypoint    # revoke UI access from Default (Using "-" to deny permission)
/perm user add <UUID> waypoints.command.waypoint      # grant UI access to a player
/perm user add <UUID> waypoints.command.teleport      # grant teleport permission to a player
/perm user add <UUID> waypoints.command.hud           # grant HUD toggle permission to a player
```
*   **On singleplayer**: Run `/op self` to grant yourself permissions

## Commands

*   `/waypoint` or `/wp` - Opens the waypoint UI
*   `/waypoint togglehud` or `/waypoint hud` - Toggle the coordinates HUD display
*   `/teleport [name]` - Teleport to a waypoint by name

## Configuration

This plugin writes and reads a configuration file so server operators can control behavior.

- **Config file:** `mods/Bofffmedia_Waypoints/waypoints_config.json` (created under your world save at `user_data/saves/<world>/mods/Bofffmedia_Waypoints/`)
- **Setting:** `MaxWaypoints` (integer, default `-1`)
	- `-1` means unlimited waypoints per player per world.
	- Any non-negative integer sets the maximum number of waypoints a player may create in a single world.

Example generated config:

```
{
	"MaxWaypoints": -1
}
```

After changing the config file, restart the world or reload the mod so the new value is applied.

## TODO

*   Allow users to add their own icons
*   Translations
*   Persist HUD state across sessions

_Questions or suggestions? Feel free to drop a comment below!_