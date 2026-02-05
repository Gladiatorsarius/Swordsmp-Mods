# Combat Log Discord Bot

Discord bot that manages combat log appeal tickets for the Minecraft server.

## Features

- 🔌 **WebSocket Server** - Receives combat log incidents from Minecraft in real-time
- 🎫 **Ticket System** - Creates Discord tickets (Forum posts or Threads)
- ⚖️ **Admin Commands** - Slash commands for reviewing and deciding appeals
- ⏱️ **Auto-timeout** - Automatically denies tickets after deadline
- 📹 **Clip Validation** - Accepts proof from multiple platforms
- 🔄 **Real-time Sync** - Sends decisions back to Minecraft instantly

## Setup

### 1. Discord Bot Setup

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Go to "Bot" section and create a bot
4. Enable these Privileged Gateway Intents:
   - Server Members Intent
   - Message Content Intent
5. Copy the bot token
6. Go to OAuth2 → URL Generator:
   - Scopes: `bot`, `applications.commands`
   - Bot Permissions: 
     - Send Messages
     - Embed Links
     - Attach Files
     - Manage Threads
     - Create Public Threads
     - Use Slash Commands
7. Use generated URL to invite bot to your server

### 2. Discord Server Setup

**Option A: Forum Channel (Recommended)**
1. Create a Forum Channel (e.g., #combat-log-tickets)
2. Copy the channel ID

**Option B: Text Channel with Threads**
1. Create a Text Channel (e.g., #combat-log-tickets)
2. Copy the channel ID

3. Create a staff role (or use existing moderator role)
4. Copy your server (guild) ID and staff role ID

### 3. Configuration

1. Copy `config.example.json` to `config.json`
2. Fill in your Discord credentials:

```json
{
  "discord": {
    "token": "YOUR_BOT_TOKEN_HERE",
    "guildId": "123456789012345678",
    "ticketChannelId": "123456789012345678",
    "staffRoleId": "123456789012345678",
    "useForumChannel": true
  },
  "websocket": {
    "port": 8080,
    "host": "0.0.0.0"
  },
  "ticket": {
    "timeoutMinutes": 60,
    "autoDenyEnabled": true
  }
}
```

### 4. Build and Run

**Build:**
```bash
./gradlew shadowJar
```

This creates: `build/libs/combat-log-discord-bot-1.0.0.jar`

**Run:**
```bash
java -jar build/libs/combat-log-discord-bot-1.0.0.jar
```

Or use the Gradle run task:
```bash
./gradlew run
```

## Admin Commands

All commands are slash commands in Discord:

### `/approve <incident_id> [reason]`
Approve an appeal and clear the punishment.
- Player will NOT be killed on next login
- Ticket is closed

### `/deny <incident_id> [reason]`
Deny an appeal and confirm the punishment.
- Player WILL be killed on next login
- Ticket is closed

### `/extend <incident_id> <minutes>`
Extend the deadline for submitting proof.
- Adds specified minutes to the deadline
- Useful for legitimate delays

### `/info <incident_id>`
View detailed information about a ticket.
- Shows status, timings, clip URL (if submitted)
- Shows time remaining

## Workflow

1. **Player Combat Logs**
   - Minecraft detects disconnect during combat
   - Sends incident to Discord bot via WebSocket

2. **Bot Creates Ticket**
   - Creates Forum post or Thread
   - Includes incident details
   - Tags staff role
   - Starts countdown timer

3. **Player Submits Proof**
   - Player posts clip/video URL in ticket
   - Bot detects and records submission
   - Marks as "Clip Uploaded"

4. **Staff Reviews**
   - Admin watches clip
   - Uses `/approve` or `/deny` command
   - Bot sends decision to Minecraft
   - Ticket is closed

5. **Auto-timeout (if no proof)**
   - After deadline, bot auto-denies
   - Sends AUTO_DENIED to Minecraft
   - Ticket is closed

## Accepted Proof Platforms

- YouTube (youtube.com, youtu.be)
- Twitch (twitch.tv, clips.twitch.tv)
- Streamable (streamable.com)
- Medal.tv (medal.tv)
- Discord Attachments

## Troubleshooting

### Bot doesn't connect to Discord
- Check bot token in config.json
- Verify bot has required intents enabled
- Check console for error messages

### Bot doesn't receive incidents from Minecraft
- Verify Minecraft mod config has correct WebSocket URL
- Check firewall allows port 8080
- Verify both are running and check logs

### Slash commands don't appear
- Wait a few minutes after bot starts (Discord caches commands)
- Try leaving and rejoining the Discord server
- Check bot has "Use Slash Commands" permission

### Tickets don't create
- Verify channel ID is correct
- Check bot has permissions in that channel
- If using Forum Channel, ensure it's actually a Forum type channel

## Development

### Project Structure
```
src/main/java/combat/log/discord/
├── CombatLogBot.java          - Main class
├── config/
│   └── BotConfig.java         - Configuration
├── models/
│   ├── SocketMessage.java     - Message base
│   ├── CombatLogIncident.java - Incoming incident
│   ├── IncidentDecision.java  - Outgoing decision
│   └── Ticket.java            - Ticket model
├── websocket/
│   └── CombatLogWebSocketServer.java - WebSocket server
├── discord/
│   └── TicketManager.java     - Ticket lifecycle
└── commands/
    └── TicketCommands.java    - Slash commands
```

### Dependencies
- JDA 5.0.0 - Discord API
- Java-WebSocket 1.5.6 - WebSocket server
- Gson 2.10.1 - JSON parsing
- Logback 1.4.14 - Logging

## License

Part of the Combat Log Report system for Swordsmp Mods.
