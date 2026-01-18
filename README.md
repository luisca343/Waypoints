# Waypoints

A waypoint management system for Hytale with an in-game UI.

![Compass](https://i.imgur.com/4CCCdaT.png) ![Waypoints main view](https://i.imgur.com/lqG4f1F.jpeg)

## Features

*   **UI-based waypoint manager** - Create, edit, and remove waypoints through a graphical interface
*   **Distance display** - Shows distance in blocks from your current position to each waypoint
*   **Auto-sorting** - Waypoints are sorted by distance, closest first
*   **Teleportation** - Optional teleport button (Creative mode or with permission)
*   **Map markers** - Waypoints appear on your in-game map

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

## Permissions

- `boffmedia.waypoints.command.waypoint` — Access to the `/waypoint` UI. Granted by default; server admins can revoke or grant access.
- `boffmedia.waypoints.command.teleport` — Required to use the teleport command/button.

### Granting Permissions

*   **On servers**: Example commands to manage these permissions:

```
/perm group add Default -boffmedia.waypoints.command.waypoint    # revoke UI access from Default (Using "-" to deny permission)
/perm user add <UUID> boffmedia.waypoints.command.waypoint      # grant UI access to a player
/perm user add <UUID> boffmedia.waypoints.command.teleport      # grant teleport permission to a player
```
*   **On singleplayer**: Run `/op self` to grant yourself permissions

## Commands

*   `/waypoint` or `/wp` - Opens the waypoint UI
*   `/teleport [name]` - Teleport to a waypoint by name

## TODO

*   Allow users to add their own icons
*   Translations

_Questions or suggestions? Feel free to drop a comment below!_