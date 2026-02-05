# Discord Combat Log Bot

**Status**: 🔧 Not Yet Implemented - Placeholder for Future Development

## 📋 Overview

This folder will contain the Discord bot component of the SwordSMP Combat Log System. The bot will handle:
- Creating tickets for combat log incidents
- Managing player proof submissions
- Providing admin commands for review
- Enforcing time-based decisions
- Communicating with the Minecraft mod

## 🚧 Current Status

**This bot has not been implemented yet.** This is a placeholder directory for when implementation begins.

## 📝 Planning Documents

Before implementation begins, please review:
- **../README.md** - Project overview
- **../DISCORD_INTEGRATION_PLAN.md** - Detailed technical architecture
- **../QUESTIONS_CHECKLIST.md** - Configuration questions to answer

## 🎯 Planned Features

### Ticket System
- Automatic ticket creation for combat log incidents
- Player notification (if Discord account linked)
- Staff role tagging for review
- Timer display with countdown

### Clip Management
- Accept video uploads (Discord direct upload)
- Accept video links (YouTube, Twitch, Streamable, etc.)
- Validate submission format
- Store submission timestamp

### Admin Commands
```
/approve <incident_id> [reason]
  - Approves the player's appeal
  - Clears punishment in Minecraft
  - Closes ticket with approval message

/deny <incident_id> [reason]
  - Denies the player's appeal
  - Confirms punishment in Minecraft
  - Closes ticket with denial message

/extend <incident_id> <minutes>
  - Extends the submission deadline
  - Updates ticket with new deadline
  - Notifies player of extension

/info <incident_id>
  - Shows incident details
  - Displays clip if submitted
  - Shows current status
```

### Automation
- Time-based auto-denial for expired tickets
- Automatic ticket archival
- Status updates to Minecraft mod
- Notification system

## 🏗️ Planned Structure

```
discord-bot/
├── src/
│   ├── bot.py (or bot.js/Main.java depending on language choice)
│   ├── commands/
│   │   ├── approve.py
│   │   ├── deny.py
│   │   ├── extend.py
│   │   └── info.py
│   ├── handlers/
│   │   ├── ticket_handler.py
│   │   ├── clip_handler.py
│   │   └── webhook_handler.py
│   ├── services/
│   │   ├── database.py
│   │   ├── minecraft.py
│   │   └── timer.py
│   └── utils/
│       ├── validators.py
│       ├── logger.py
│       └── config.py
│
├── config/
│   ├── config.json
│   └── .env.example
│
├── requirements.txt (or package.json/pom.xml)
├── README.md (this file)
└── .gitignore
```

## ⚙️ Configuration (Planned)

The bot will require configuration for:
- Discord bot token
- Server and channel IDs
- Webhook URLs
- Database connection
- Timeout settings
- Admin roles

Example config structure:
```json
{
  "discord": {
    "token": "your-bot-token",
    "guild_id": "server-id",
    "ticket_channel_id": "channel-id",
    "staff_role_id": "role-id"
  },
  "minecraft": {
    "webhook_url": "http://localhost:3000/webhook",
    "webhook_secret": "shared-secret"
  },
  "database": {
    "type": "sqlite",
    "path": "../data/combat-logs.db"
  },
  "settings": {
    "timeout_minutes": 60,
    "auto_deny_enabled": true,
    "require_proof": true
  }
}
```

## 🔧 Prerequisites (When Implementing)

### Required:
- Discord Bot Token (from Discord Developer Portal)
- Discord Server with appropriate permissions
- Python 3.8+ (or Node.js 16+, or Java 17+, depending on choice)
- Database server or SQLite file
- Network connectivity to Minecraft server

### Bot Permissions Needed:
- Read Messages/View Channels
- Send Messages
- Create Public Threads (if using threads)
- Manage Threads (if using threads)
- Attach Files
- Embed Links
- Read Message History

## 📚 Dependencies (Planned)

Will depend on configuration choices. Possible options:

### Python (discord.py):
```
discord.py>=2.0.0
aiohttp>=3.8.0
sqlalchemy>=2.0.0
python-dotenv>=0.19.0
```

### JavaScript (discord.js):
```
discord.js: ^14.0.0
express: ^4.18.0
sqlite3: ^5.1.0
dotenv: ^16.0.0
```

### Java (JDA):
```
JDA: 5.0.0+
OkHttp: 4.10.0+
SQLite JDBC: 3.40.0+
Gson: 2.10.0+
```

## 🚀 Installation (Future)

Installation instructions will be provided once implementation is complete.

## 📞 Support

For questions about the bot implementation plan:
- Review **../DISCORD_INTEGRATION_PLAN.md**
- Check **../QUESTIONS_CHECKLIST.md**
- Consult with server administrators

## 🎯 Next Steps

Before this bot can be implemented, the following configuration questions must be answered (see ../QUESTIONS_CHECKLIST.md):

1. ❓ Which programming language? (Python/JavaScript/Java)
2. ❓ Where will it be hosted? (Same server/separate/cloud)
3. ❓ Which database system? (SQLite/MySQL/PostgreSQL)
4. ❓ What ticket system? (Forum/Threads/Channels)
5. ❓ How to link players? (Manual/automatic/existing system)

Once these are answered, implementation can begin!

---

**Created**: 2026-02-05  
**Status**: Awaiting configuration decisions  
**Part of**: SwordSMP Combat Log System
