# SwordSMP Combat Log Report

A Fabric mod for Minecraft 1.21.11 that detects combat logging, integrates with Discord for appeals, and enforces punishments.

## 🎯 What is Combat Logging?

**Combat logging** is when a player disconnects from the server during PvP combat to avoid death and losing their items. This mod detects combat logging, creates Discord tickets for appeals, spawns player heads with stored inventory, and enforces time-based punishments.

## ✨ Features

### Combat Tagging System
- When players engage in PvP combat (hitting or being hit by another player), both players are tagged as "in combat"
- Combat tag lasts for **15 seconds** after the last hit
- Combat timer resets if either player attacks or is attacked again
- **Action Bar Display**: Live countdown shown above hotbar (§c§lCOMBAT 15s, 14s, 13s...)
- **Combat Ends on Death**: When any player dies, all combat tags are cleared

### Combat Restrictions
- **Firework Rocket Blocking**: Players cannot use firework rockets while in combat
- Action bar message: "Cannot use rockets while in combat!"
- Prevents flying away with elytra during PvP

### Combat Logging Detection & Punishment
- Detects when players disconnect during active combat
- **Player Head System**: Spawns player head at disconnect location
  - Contains player's inventory (framework ready)
  - Time-based access control (opponents first 30 min, then everyone)
  - Server operators can always access heads
- **Discord Integration**: Sends incident to Discord bot via WebSocket
- **Automatic Ticket**: Creates Discord ticket for player appeal
- **Punishment on Login**: If ticket denied, player is killed on next login

### Discord Integration (WebSocket)
- **Real-time Communication**: WebSocket connection to Discord bot (port 8080)
- **Incident Reporting**: Sends combat log incidents with full details
- **Decision Processing**: Receives APPROVED/DENIED decisions from Discord
- **Whitelist Commands**: Executes whitelist add/remove commands from Discord
- **Player Linking**: Syncs Discord ↔ Minecraft account links

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

### Scenario 2: Combat Logging Detection
```
1. Player A attacks Player B
   → Both tagged in combat

2. Player B disconnects after 5 seconds
   → Player head spawns at Player B's location
   → Server broadcasts: "§e[Combat Log Report] §cPlayer B logged out during combat with 10.0 seconds remaining!"
   → Discord ticket created automatically
   → Player B has 60 minutes to submit proof
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

## 📦 Installation

### Requirements
- Minecraft 1.21.11
- Fabric Loader 0.18.4 or later
- Fabric API 0.141.3+1.21.11 or later
- Java 21 or later
- **Discord Bot** (for full functionality)

### Steps
1. Install Fabric Loader for Minecraft 1.21.11
2. Download and install Fabric API 0.141.3+1.21.11
3. Place `combat-log-report-1.0.0.jar` in your server's `mods` folder
4. Start server (first run creates config file)
5. Edit `config/combat-log-report.json` to configure WebSocket

### Configuration

First run creates `config/combat-log-report.json`:

```json
{
  "socket": {
    "enabled": true,
    "serverUrl": "ws://localhost:8080/combat-log"
  }
}
```

**Configuration Options:**
- `socket.enabled` - Enable/disable Discord integration
- `socket.serverUrl` - WebSocket URL of Discord bot (default: ws://localhost:8080/combat-log)

### Game Rules

The mod adds custom gamerules that can be configured in-game:

**`bypassCombatLogSystem`** (default: `false`)
- **Type**: Boolean (true/false)
- **Category**: Player
- **Description**: When enabled, completely bypasses the combat log system
  - ❌ No Discord incidents created
  - ❌ No player heads spawned
  - ❌ No punishments applied
  - ✅ Items drop naturally like a normal death
  - ✅ Simple broadcast message when player logs during combat
- **Use Case**: Testing, events, or situations where you want normal death behavior
- **Command**: `/gamerule bypassCombatLogSystem true` (requires OP)
- **Server-side only**: No client modifications needed

**Example Usage:**
```mcfunction
# Enable bypass (items drop normally, no Discord tickets)
/gamerule bypassCombatLogSystem true

# Disable bypass (full combat log system active)
/gamerule bypassCombatLogSystem false
```

### Server Log Verification

Check the server log for successful initialization:
```
[combat-log-report] Combat Log Report mod initialized!
[combat-log-report] Combat logging tracking is now active
[combat-log-report] Players who disconnect during combat will be reported
[combat-log-report] Initialized player linking system
[combat-log-report] Initialized whitelist command handler
[combat-log-report] Attempting to connect to Discord bot at ws://localhost:8080/combat-log
[combat-log-report] Connected to Discord bot WebSocket server
```

## 🤖 Discord Bot Integration

This mod works with the **Combat Log Discord Bot** for:
- Automatic ticket creation for combat log incidents
- Player appeals with proof submission
- Staff commands for approval/denial
- Automatic whitelist system with Mojang API
- Discord-Minecraft account linking

See [../discord-bot/README.md](../discord-bot/README.md) for Discord bot setup.

## 🎮 For Players

See [USAGE_GUIDE.md](USAGE_GUIDE.md) for a complete player guide on:
- How combat tagging works
- What happens when you combat log
- How to avoid being reported
- Understanding the punishment system

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/combat-log-report-1.0.0.jar`

## 🛠️ Build Instructions

To build the Combat Log mod, navigate to the `combat-log/combat-log-report-1.21.11` directory and run the following command:

```
.\gradlew.bat build
```

This will compile the mod and generate the output JAR file in the `build/libs` directory.

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
