# SwordSMP Combat Log Plugin

A Fabric mod for Minecraft 1.21.11 that prevents combat logging on multiplayer servers.

## Features

### Combat Tagging System
- When players engage in PvP combat (hitting or being hit by another player), both players are tagged as "in combat"
- Combat tag lasts for **15 seconds** after the last hit
- Combat timer resets if either player attacks or is attacked again

### Player Notifications
- **Combat Entry**: Players receive a warning message when they enter combat: "§c§lYou are now in combat! Do not log out for 15 seconds!"
- **Combat Countdown**: Players are notified when combat is about to end (at 5, 3, 2, 1 seconds remaining)
- **Combat Exit**: Players receive a confirmation message when they leave combat: "§aYou are no longer in combat!"

### Combat Logging Prevention
- If a player disconnects while in combat, they are **killed** as punishment
- All other players on the server are notified: "§c[Player Name] logged out during combat!"
- This prevents players from escaping PvP by disconnecting

## How It Works

The plugin uses:
1. **Fabric API Events** to detect when players damage each other
2. **Mixins** to intercept player disconnections and server ticks
3. **CombatManager** singleton to track combat timers for all players

## Installation

1. Install Fabric Loader for Minecraft 1.21.11
2. Install Fabric API 0.141.3+1.21.11 or later
3. Place the mod JAR in your `mods` folder
4. Restart your server

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/combat-log-report-1.0.0.jar`

## Similar To

This plugin functions similarly to combat log systems found on popular SMP servers like Donut SMP, where players cannot escape PvP by disconnecting.

## License

See LICENSE file for details.
