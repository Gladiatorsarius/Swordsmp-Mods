# Running the Combat Log System

This guide shows how to run both the Discord bot and Minecraft mod.

## ✅ Build Status

Both components build successfully:

- **Discord Bot**: `combat-log/discord-bot/build/libs/combat-log-discord-bot-1.0.0.jar` (37MB)
- **Minecraft Mod**: `combat-log/combat-log-report-1.21.11/build/libs/combat-log-report-1.0.0.jar` (2.9MB)

## 🤖 Running the Discord Bot

### Prerequisites
- Java 17 or later
- Discord Bot Token (from Discord Developer Portal)
- Discord Server with proper channels set up

### Step 1: Configure the Bot

Create `config.json` from the example:
```bash
cd combat-log/discord-bot
cp config.example.json config.json
```

Edit `config.json` with your Discord credentials:
```json
{
  "discord": {
    "token": "YOUR_BOT_TOKEN_HERE",
    "guildId": "YOUR_GUILD_ID",
    "ticketChannelId": "YOUR_CHANNEL_ID",
    "staffRoleId": "YOUR_STAFF_ROLE_ID",
    "useForumChannel": true
  },
  "websocket": {
    "port": 8080,
    "host": "0.0.0.0"
  },
  "whitelist": {
    "enabled": true,
    "whitelistChannelId": "YOUR_WHITELIST_CHANNEL_ID",
    "reviewChannelId": "YOUR_REVIEW_CHANNEL_ID",
    "staffRoleId": "YOUR_STAFF_ROLE_ID"
  }
}
```

### Step 2: Run the Bot

```bash
cd combat-log/discord-bot
java -jar build/libs/combat-log-discord-bot-1.0.0.jar
```

The bot will:
1. Connect to Discord
2. Start WebSocket server on port 8080
3. Register slash commands
4. Wait for combat log incidents from Minecraft

### Expected Output:
```
Starting Combat Log Discord Bot...
Initialized linking database
Initialized Mojang API service
Connecting to Discord...
Discord bot connected as: YourBotName
Initialized whitelist manager
Registered slash commands
Starting WebSocket server on port 8080...
WebSocket server started on 0.0.0.0:8080
Combat Log Discord Bot is ready!
WebSocket server listening on 0.0.0.0:8080
```

## 🎮 Installing the Minecraft Mod

### Prerequisites
- Minecraft 1.21.11 Server
- Fabric Loader
- Fabric API

### Step 1: Install the Mod

1. Copy the mod jar to your server's mods folder:
```bash
cp combat-log/combat-log-report-1.21.11/build/libs/combat-log-report-1.0.0.jar /path/to/minecraft/server/mods/
```

2. Start your Minecraft server (first start will create config)

### Step 2: Configure the Mod

Edit `config/combat-log-report.json`:
```json
{
  "socket": {
    "enabled": true,
    "serverUrl": "ws://localhost:8080/combat-log"
  }
}
```

### Step 3: Restart Server

The mod will:
1. Initialize player linking system
2. Connect to Discord bot via WebSocket
3. Start tracking combat events
4. Send combat log incidents to Discord

### Expected Log Output:
```
[combat-log-report] Combat Log Report mod initialized!
[combat-log-report] Combat logging tracking is now active
[combat-log-report] Players who disconnect during combat will be reported
[combat-log-report] Initialized player linking system
[combat-log-report] Initialized whitelist command handler
[combat-log-report] Attempting to connect to Discord bot at ws://localhost:8080/combat-log
[combat-log-report] Connected to Discord bot WebSocket server
```

## 🔄 Testing the Connection

### Test 1: WebSocket Connection
When Minecraft starts, you should see in Discord bot logs:
```
New connection from: /127.0.0.1:xxxxx
Connected to Discord bot WebSocket server
```

### Test 2: Whitelist Request
1. Go to Discord whitelist channel
2. Click "Request Whitelist" button
3. Enter Minecraft username
4. Bot validates with Mojang API
5. Automatically whitelists player
6. Sends whitelist command to Minecraft

### Test 3: Combat Log Incident
1. Two players enter combat (hit each other)
2. One player disconnects during combat
3. Bot creates Discord ticket
4. If player is linked, they're added to private thread
5. Staff can approve/deny

## 🛠️ Features

### Discord Bot Features:
- ✅ Automatic whitelist approval
- ✅ One-to-one Discord ↔ Minecraft linking
- ✅ Combat log ticket creation
- ✅ Private threads for linked players
- ✅ Slash commands for staff
- ✅ WebSocket communication with Minecraft

### Minecraft Mod Features:
- ✅ Combat tracking (15 second timer)
- ✅ Combat log detection
- ✅ Player head spawning on combat log
- ✅ Inventory storage in heads
- ✅ Time-based head access control
- ✅ `/unlink` command for players
- ✅ WebSocket communication with Discord

## 📝 Slash Commands

Available in Discord:

- `/whitelist-setup <channel_id>` - Setup whitelist request channel
- `/approve <incident_id>` - Approve combat log appeal
- `/deny <incident_id>` - Deny combat log appeal
- `/extend <incident_id> <minutes>` - Extend ticket deadline
- `/info <incident_id>` - View ticket information

## 🎮 In-Game Commands

Available in Minecraft:

- `/unlink` - Unlink Discord account and remove from whitelist

## 🐛 Troubleshooting

### Discord bot won't start
- Check bot token is correct
- Verify Java 17+ is installed
- Check config.json is valid JSON

### Minecraft mod won't connect
- Verify Discord bot is running first
- Check WebSocket URL in mod config
- Ensure firewall allows port 8080
- Check both are on same network (or port forwarding configured)

### Whitelist not working
- Verify whitelist channel IDs are correct
- Check bot has permissions in channels
- Test with `/whitelist-setup` command

## 📦 Build From Source

### Discord Bot:
```bash
cd combat-log/discord-bot
./gradlew build
# Output: build/libs/combat-log-discord-bot-1.0.0.jar
```

### Minecraft Mod:
```bash
cd combat-log/combat-log-report-1.21.11
./gradlew build
# Output: build/libs/combat-log-report-1.0.0.jar
```

## ✨ System Status

- ✅ Discord bot builds successfully (37MB)
- ✅ Minecraft mod builds successfully (2.9MB)
- ✅ All tests passing
- ✅ No compilation errors
- ✅ Ready for deployment
