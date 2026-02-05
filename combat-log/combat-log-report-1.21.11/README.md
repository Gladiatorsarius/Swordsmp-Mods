# SwordSMP Combat Log Report

A Fabric mod for Minecraft 1.21.11 that tracks and reports combat logging on multiplayer servers.

## 🎯 What is Combat Logging?

**Combat logging** is when a player disconnects from the server during PvP combat to avoid death and losing their items. This mod tracks these incidents and reports them to all players on the server.

## ✨ Features

### Combat Tagging System
- When players engage in PvP combat (hitting or being hit by another player), both players are tagged as "in combat"
- Combat tag lasts for **15 seconds** after the last hit
- Combat timer resets if either player attacks or is attacked again

### Player Notifications
- **Combat Entry**: Players receive a warning message when they enter combat: "§e§lYou are now in combat! Logging out will be reported for 15 seconds!"
- **Combat Countdown**: Players are notified when combat is about to end (at 5, 3, 2, 1 seconds remaining)
- **Combat Exit**: Players receive a confirmation message when they leave combat: "§aYou are no longer in combat!"

### Combat Logging Reporting
- If a player disconnects while in combat, a report is broadcast to all players
- Message format: "§e[Combat Log Report] §c[Player Name] logged out during combat with X.X seconds remaining!"
- This creates transparency and tracks combat logging incidents without punishment

## 📖 Example Scenarios

### Scenario 1: Normal Combat Exit
```
1. Player A attacks Player B
   → Both receive: "§e§lYou are now in combat! Logging out will be reported for 15 seconds!"

2. 10 seconds pass with no further attacks
   → Players receive: "§eCombat ends in 5 seconds..."
   → Players receive: "§eCombat ends in 4 seconds..."
   → ... countdown continues ...

3. 15 seconds total pass
   → Both receive: "§aYou are no longer in combat!"
   → Players can safely log out without reports
```

### Scenario 2: Combat Logging Report
```
1. Player A attacks Player B
   → Both tagged in combat

2. Player B disconnects after 5 seconds
   → Server broadcasts: "§e[Combat Log Report] §cPlayer B logged out during combat with 10.0 seconds remaining!"
   → All players see the report
   → Player B's character remains in-game until normal disconnect
```

### Scenario 3: Extended Combat
```
1. Player A hits Player B → 15 second timer starts
2. After 10 seconds, Player B hits back
   → Timer resets to 15 seconds for both players
3. Combat continues with timers resetting on each hit
4. When fighting stops, 15 seconds must pass before no report on logout
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
- **Reporting**: Broadcasts a message to all players when someone logs out during combat
- **Thread Safety**: Uses ConcurrentHashMap for safe multi-threaded access
- **No Punishment**: Players are NOT killed - only reported in chat

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
- SMP servers wanting combat transparency
- Servers that prefer reporting over punishment
- Community-driven servers with honor systems
- Servers tracking PvP statistics and behavior
- Any server where combat logging awareness is important

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

This plugin functions similarly to combat log tracking systems found on various SMP servers, providing transparency about combat logging incidents without applying harsh punishments.

## 📜 License

See LICENSE file for details.

## 🤝 Credits

- Built with Fabric Mod Loader
- Uses Fabric API for event handling
- Designed for SwordSMP server
- Tracks combat logging for server transparency
