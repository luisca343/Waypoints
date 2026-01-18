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

*   `waypoints.teleport` - Required to see and use the teleport button (Creative mode gets this automatically)

### Granting Permissions

*   **On servers**: Use `/perm user add [YOUR_UUID] boffmedia.waypoints.command.teleport` to grant teleport command access
*   **On singleplayer**: Run `/op self` to grant yourself permissions

## Commands

*   `/waypoint` or `/wp` - Opens the waypoint UI
*   `/teleport [name]` - Teleport to a waypoint by name

## TODO

*   Allow users to add their own icons
*   Translations

_Questions or suggestions? Feel free to drop a comment below!_