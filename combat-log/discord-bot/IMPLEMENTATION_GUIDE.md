# Discord Bot - WebSocket Server Implementation Guide

## Overview

This Discord bot will receive combat log incidents from the Minecraft server via WebSocket and create tickets for admin review.

## Architecture

```
Discord Bot (WebSocket Server on port 8080)
    ↓ Receives incidents from Minecraft
    ↓ Creates Discord tickets
    ↓ Admin uses commands (/approve, /deny)
    ↓ Sends decision back to Minecraft
```

## Message Protocol

### Incoming: Combat Log Incident
From Minecraft when player combat logs:

```json
{
  "type": "combat_log_incident",
  "timestamp": 1707139200000,
  "incidentId": "uuid-string",
  "playerUuid": "uuid-string",
  "playerName": "PlayerName",
  "combatTimeRemaining": 10.5
}
```

### Outgoing: Incident Decision
To Minecraft after admin review:

```json
{
  "type": "incident_decision",
  "timestamp": 1707139300000,
  "incidentId": "uuid-string",
  "status": "APPROVED|DENIED|AUTO_DENIED",
  "adminName": "AdminName",
  "reason": "Optional reason text"
}
```

## Bot Requirements

### Dependencies (Java with JDA)
- JDA 5.0.0+ (Discord API)
- Java-WebSocket or similar WebSocket server library
- Gson for JSON parsing
- SQLite or PostgreSQL for persistence (optional)

### Features to Implement

1. **WebSocket Server**
   - Listen on port 8080 (configurable)
   - Handle incoming connections from Minecraft
   - Parse JSON messages
   - Send JSON responses

2. **Discord Ticket System**
   - Create ticket when incident received
   - Use Forum Channels, Threads, or Channels
   - Include incident details
   - Tag player (if linked via DiscordSRV)
   - Tag staff role
   - Show countdown timer

3. **Admin Commands**
   ```
   /approve <incident_id> [reason]
   /deny <incident_id> [reason]
   /extend <incident_id> <minutes>
   /info <incident_id>
   ```

4. **Timer System**
   - Track ticket deadlines (60 minutes default)
   - Auto-deny on timeout
   - Send periodic reminders

5. **Clip Validation**
   - Accept YouTube links
   - Accept Twitch clips
   - Accept Discord video uploads
   - Accept Streamable links
   - Accept Medal.tv links

## Configuration

```json
{
  "discord": {
    "token": "your-bot-token",
    "guildId": "server-id",
    "ticketChannelId": "channel-id",
    "staffRoleId": "role-id"
  },
  "websocket": {
    "port": 8080,
    "host": "0.0.0.0"
  },
  "settings": {
    "timeoutMinutes": 60,
    "autoDenyEnabled": true
  }
}
```

## Next Steps

1. Choose bot framework:
   - Java + JDA (recommended, same language as Minecraft)
   - Python + discord.py
   - Node.js + discord.js

2. Set up project structure
3. Implement WebSocket server
4. Implement Discord commands
5. Test with Minecraft mod

## Testing

The Minecraft mod is configured to connect to `ws://localhost:8080/combat-log` by default.

To test:
1. Start Discord bot (WebSocket server on port 8080)
2. Start Minecraft server with mod
3. Trigger combat log
4. Check bot receives message and creates ticket
5. Use admin command to approve/deny
6. Check Minecraft receives decision
