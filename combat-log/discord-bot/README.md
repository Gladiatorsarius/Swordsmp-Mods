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
2. Fill in IDs and settings in `config.json`
3. Create `config.local.json` with your bot token (this file is not committed)

```json
{
   "discord": {
      "token": "YOUR_BOT_TOKEN_HERE"
   }
}
```

Example base config (token stays in `config.local.json`):

```json
{
   "discord": {
      "token": "YOUR_BOT_TOKEN_HERE",
      "guildId": "YOUR_GUILD_ID",
      "staffRoleId": "YOUR_STAFF_ROLE_ID"
   },
   "websocket": {
      "port": 8080,
      "host": "0.0.0.0"
   },
   "features": {
      "useForumChannel": true,
      "autoDenyEnabled": true,
      "privateThreads": true,
      "whitelistEnabled": true,
      "mojangApiEnabled": true
   },
   "timeouts": {
      "ticketTimeoutMinutes": 60,
      "mojangCacheDurationMinutes": 5,
      "mojangApiTimeoutSeconds": 5
   },
   "channels": {
      "ticketChannelId": "YOUR_CHANNEL_ID",
      "whitelistChannelId": "YOUR_WHITELIST_CHANNEL_ID",
      "reviewChannelId": "YOUR_REVIEW_CHANNEL_ID"
   },
   "ticket": {
      "acceptedProofPlatforms": [
         "youtube.com",
         "youtu.be",
         "twitch.tv",
         "clips.twitch.tv",
         "streamable.com",
         "medal.tv",
         "discord.com/attachments"
      ]
   },
   "whitelist": {
      "buttonMessage": {
         "title": "🎫 Request Server Whitelist",
         "description": "Click the button below to request access to our Minecraft server",
         "color": "#00FF00"
      }
   },
   "buttons": {
      "ticket": {
         "approve": "✅ Approve",
         "deny": "❌ Deny",
         "admit": "🔴 I Admit Combat Log",
         "extend": "⏰ Extend"
      },
      "whitelist": {
         "request": "🎫 Request Whitelist",
         "approve": "✅ Approve",
         "deny": "❌ Deny"
      }
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

## Whitelisting Features

The Combat Log Discord Bot includes robust whitelisting features to manage player access to the Minecraft server. These features are tightly integrated with the bot's database and Discord commands:

### Whitelist Database

The bot uses a SQLite database to store whitelist information:

- **`whitelist_links` Table**:
  - Stores mappings between Discord IDs and Minecraft accounts.
  - Tracks metadata such as `linked_by`, `notes`, and `whitelisted` status.

- **`whitelist_requests` Table**:
  - Tracks whitelist requests with details such as:
    - Request ID
    - Discord and Minecraft user information
    - Request status (`PENDING`, `APPROVED`, `DENIED`)
    - Review details (e.g., `reviewed_by`, `reviewed_at`, `reason`)

### Workflow

1. **Whitelist Request**:
   - Players can request to be whitelisted by clicking the "Request Whitelist" button in the configured Discord channel.
   - The bot creates a whitelist request entry in the database and sends a notification to the whitelist channel.

2. **Approval Process**:
   - Staff members can review requests and approve or deny them using the slash commands.
   - Approved requests automatically whitelist the player on the Minecraft server.

3. **Unlinking**:
   - Staff members can unlink a Discord user from the whitelist using the `/unlink` command.
   - The bot removes the user from the whitelist and updates the database.

### Error Handling

- The bot provides detailed error messages for common issues, such as:
  - Missing permissions for staff members.
  - Invalid or missing inputs (e.g., missing Discord user or channel ID).
  - Database errors during whitelist operations.

These features ensure seamless integration between Discord and the Minecraft server, allowing server administrators to efficiently manage player access.

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
- Check bot token in config.local.json
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
├── interactions/
│   ├── ButtonHandler.java     - Ticket button interactions
│   └── ModalHandler.java      - Ticket modals
├── models/
│   ├── SocketMessage.java     - Message base
│   ├── CombatLogIncident.java - Incoming incident
│   ├── IncidentDecision.java  - Outgoing decision
│   └── Ticket.java            - Ticket model
├── websocket/
│   └── CombatLogWebSocketServer.java - WebSocket server
├── discord/
│   └── TicketManager.java     - Ticket lifecycle
├── whitelist/
│   ├── WhitelistManager.java  - Whitelist workflow
│   ├── WhitelistButtonHandler.java - Whitelist buttons
│   └── WhitelistModalHandler.java  - Whitelist modals
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
