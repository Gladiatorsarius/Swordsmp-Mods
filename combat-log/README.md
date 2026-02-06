# SwordSMP Combat Log System

A comprehensive combat logging detection and verification system for Minecraft servers, consisting of a Fabric mod and Discord bot integration.

## 📦 Project Structure

```
combat-log/
├── README.md                           (This file - project overview)
├── DISCORD_INTEGRATION_PLAN.md         (Detailed technical plan)
├── QUESTIONS_CHECKLIST.md              (Configuration questionnaire)
│
├── combat-log-report-1.21.11/          (Minecraft Fabric Mod)
│   ├── src/                            (Mod source code)
│   ├── build.gradle                    (Build configuration)
│   └── README.md                       (Mod-specific documentation)
│
└── discord-bot/                         (Discord Bot - PLANNED)
    └── README.md                        (Bot documentation - when implemented)
```

## 🎯 System Overview

This project creates a complete combat logging detection and verification system:

### Combat Log Detection (Minecraft Mod)
- Detects when players disconnect during combat
- Tracks combat status and timers
- Reports incidents to Discord
- Enforces punishments based on Discord decisions

### Verification System (Discord Bot)
- Creates tickets for combat log incidents
- Allows players to submit video proof
- Provides admin commands for review
- Manages time-based auto-denial
- Communicates decisions back to Minecraft

## 🔄 Workflow

```
1. Player combat logs in Minecraft
   ↓
2. Mod detects and sends to Discord bot
   ↓
3. Bot creates ticket in Discord server
   ↓
4. Player has X minutes to submit clip/proof
   ↓
   ├─→ [Clip submitted] → Admin reviews → Approve/Deny
   │                                          ↓
   └─→ [No clip] → Auto-deny after timeout   ↓
                                              ↓
5. Decision sent back to Minecraft mod
   ↓
6. Player logs in next time
   ↓
   ├─→ [Approved] Clear punishment, notify player
   └─→ [Denied] Execute punishment (kill player)
```

## 📋 Current Status

### ✅ Completed:
- [x] Basic combat log detection mod
- [x] In-game combat tagging system
- [x] Combat timer (15 seconds)
- [x] In-game reporting messages
- [x] Comprehensive planning documentation

### 🔄 In Planning:
- [ ] Discord bot implementation
- [ ] Ticket system
- [ ] Admin commands
- [ ] Database integration
- [ ] Punishment system on login
- [ ] Clip upload validation

### ⏸️ Awaiting Configuration:
- Bot language selection (Python/JavaScript/Java)
- Database choice (SQLite/MySQL/JSON)
- Ticket system type (Forum/Threads/Channels)
- Player linking method
- Timeout duration settings

See **QUESTIONS_CHECKLIST.md** for full list of configuration questions.

## 📚 Documentation

### For Server Admins:
- **README.md** (this file) - Project overview
- **[FEATURES.md](../FEATURES.md)** - Complete features documentation for all mods
- **[TESTING.md](../TESTING.md)** - Comprehensive testing guide
- **combat-log-report-1.21.11/README.md** - Mod installation and usage
- **discord-bot/README.md** - Discord bot setup guide
- **[discord-bot/CONFIG.md](discord-bot/CONFIG.md)** - **Detailed config.json documentation**
- **[RUNNING.md](RUNNING.md)** - **How to run everything**

### For Developers:
- **[FEATURES.md](../FEATURES.md)** - Architecture and all system features
- **combat-log-report-1.21.11/src/** - Mod source code
- **discord-bot/src/** - Bot source code
- **[BUILD_VERIFICATION.md](BUILD_VERIFICATION.md)** - Build verification logs

### For Players:
- **combat-log-report-1.21.11/USAGE_GUIDE.md** - Player guide

## 🚀 Quick Start (When Implemented)

### Prerequisites:
- Minecraft Server with Fabric Loader
- Discord Server with bot permissions
- Database server (or SQLite file)

### Installation:
1. Install Minecraft mod (see combat-log-report-1.21.11/README.md)
2. Set up Discord bot (see discord-bot/README.md - when available)
3. Configure both components to communicate
4. Test the integration

## ⚙️ Configuration

Configuration files will be located in:
- **Minecraft Mod**: `config/combat-log-report-config.json`
- **Discord Bot**: `discord-bot/config/config.json`

Both must be configured to share:
- Database connection details
- Webhook URLs
- Server identifiers
- Timeout settings

## 🔧 Development

### Building the Minecraft Mod:
```bash
cd combat-log-report-1.21.11
./gradlew build
```

### Running the Discord Bot:
```bash
cd discord-bot
# Instructions will be added when bot is implemented
```

## 🤝 Contributing

This is a private server project for SwordSMP. If you have suggestions or find issues:
1. Review the planning documents
2. Check existing issues/questions
3. Discuss with server administrators

## 📅 Implementation Timeline

- **Phase 1**: Data structures & storage (1-2 days)
- **Phase 2**: Minecraft mod enhancements (2-3 days)
- **Phase 3**: Discord bot core functionality (3-4 days)
- **Phase 4**: Integration & testing (2-3 days)
- **Phase 5**: Documentation & deployment (1 day)

**Estimated Total**: 10-15 days

## 📞 Support

For questions about:
- **Mod functionality**: See combat-log-report-1.21.11/README.md
- **Discord bot**: See discord-bot/README.md (when available)
- **Integration**: See DISCORD_INTEGRATION_PLAN.md
- **Configuration**: See QUESTIONS_CHECKLIST.md

## 📜 License

See LICENSE file in the root of the repository.

## 🎮 For SwordSMP

This system is specifically designed for the SwordSMP Minecraft server to provide:
- Fair PvP combat
- Transparent combat logging detection
- Community-driven moderation
- Reduced admin workload through automation
- Player appeals process

---

**Status**: ⏸️ Planning Phase Complete - Awaiting Configuration Answers

See **QUESTIONS_CHECKLIST.md** to provide the necessary configuration details to begin implementation.
