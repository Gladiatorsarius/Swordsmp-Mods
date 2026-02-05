# SwordSMP Combat Log Plugin

A Fabric mod for Minecraft 1.21.11 that prevents combat logging on multiplayer servers.

## 🎯 What is Combat Logging?

**Combat logging** is when a player disconnects from the server during PvP combat to avoid death and losing their items. This is considered unfair gameplay. This mod prevents that by applying consequences to players who disconnect while fighting.

## ✨ Features

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

## 📖 Example Scenarios

### Scenario 1: Normal Combat Exit
```
1. Player A attacks Player B
   → Both receive: "§c§lYou are now in combat! Do not log out for 15 seconds!"

2. 10 seconds pass with no further attacks
   → Players receive: "§eCombat ends in 5 seconds..."
   → Players receive: "§eCombat ends in 4 seconds..."
   → ... countdown continues ...

3. 15 seconds total pass
   → Both receive: "§aYou are no longer in combat!"
   → Players can safely log out
```

### Scenario 2: Combat Logging Attempt
```
1. Player A attacks Player B
   → Both tagged in combat

2. Player B panics and disconnects after 5 seconds
   → Player B is killed (drops all items)
   → Server broadcasts: "§cPlayer B logged out during combat!"
   → Player A can collect the dropped items
```

### Scenario 3: Extended Combat
```
1. Player A hits Player B → 15 second timer starts
2. After 10 seconds, Player B hits back
   → Timer resets to 15 seconds for both players
3. Combat continues with timers resetting on each hit
4. When fighting stops, 15 seconds must pass before safe logout
```

## 🔧 How It Works

The plugin uses:
1. **Fabric API Events** to detect when players damage each other
2. **Mixins** to intercept player disconnections and server ticks
3. **CombatManager** singleton to track combat timers for all players

### Technical Details
- **Combat Duration**: 15 seconds (15000 milliseconds)
- **Timer Reset**: Every time a tagged player attacks or is attacked
- **Countdown Warnings**: Displayed at 5, 3, 2, and 1 seconds remaining
- **Punishment**: Player is killed using maximum damage (Float.MAX_VALUE)
- **Thread Safety**: Uses ConcurrentHashMap for safe multi-threaded access

## 📦 Installation

### Requirements
- Minecraft 1.21.11
- Fabric Loader 0.18.4 or later
- Fabric API 0.141.3+1.21.11 or later
- Java 21 or later

### Steps
1. Install Fabric Loader for Minecraft 1.21.11
2. Download and install Fabric API 0.141.3+1.21.11
3. Place `combat-log-report-1.0.0.jar` in your server's `mods` folder
4. Restart your server
5. Check the server log for: "Combat Log Report mod initialized!"

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/combat-log-report-1.0.0.jar`

## 🎮 Server Integration

This mod is designed for multiplayer survival servers (SMP) where PvP is enabled. It's particularly useful for:
- Faction servers
- Anarchy servers
- SMP servers with PvP zones
- Any server where fair PvP is important

## 🔍 Troubleshooting

### The mod isn't detecting combat
- Ensure both players are hitting each other (player vs player damage)
- Check that Fabric API is properly installed
- Verify the mod is loading in the server logs

### Players aren't receiving messages
- Messages use Minecraft color codes (§c, §a, §e)
- Check if any chat plugins are interfering
- Verify the mod is running server-side

### Combat tag isn't clearing
- The tag automatically clears after 15 seconds of no combat
- Server restart will clear all combat tags
- The tag is stored in memory only (not persistent)

## 💡 Similar To

This plugin functions similarly to combat log systems found on popular SMP servers like Donut SMP, where players cannot escape PvP by disconnecting.

## 📜 License

See LICENSE file for details.

## 🤝 Credits

- Built with Fabric Mod Loader
- Uses Fabric API for event handling
- Designed for SwordSMP server
