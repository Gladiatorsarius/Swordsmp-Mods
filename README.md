# SwordSMP Mods Repository

Complete collection of Minecraft mods and Discord integration for the SwordSMP server.

## 📦 Projects

Note: Whitelist management has been split into a new top-level bundle `whitelist-handler/`.
The combat-log bundle (`combat-log/`) remains responsible for incident tracking and tickets; whitelist link management is now handled by `whitelist-handler`.

### 1. Combat Log System
**Location**: `combat-log/`

A comprehensive combat logging detection and management system consisting of:
- **Minecraft Fabric Mod** (1.21.11) - Detects combat logging, spawns player heads, manages punishments
- **Discord Bot** - Creates tickets, manages whitelist, handles appeals

**Key Features**:
- 15-second combat detection
- Player head inventory storage
- Time-based access control
- Discord ticket system with self-admission
- Automatic whitelist system
- WebSocket communication between Minecraft and Discord

### 2. Armour Invisibility Mod
**Location**: `armour-invis-template-1.21.11/`

Hides worn armor when players have Invisibility II effect.

**Key Features**:
- Client and server-side compatibility
- Only affects Invisibility II (level 2)
- Works with all armor types

### 3. SwordsSMP Mod
**Location**: `swordsmp 1.21.11/`

Custom mod with server-specific features created with MCreator.

### 4. Combined Mod
**Location**: `swordsmp 1.21.11 compined with armourinvis/`

Combines SwordsSMP and Armour Invisibility into a single mod package.

---

## 📚 Documentation

### Getting Started
- **[FEATURES.md](FEATURES.md)** - **Complete features documentation for all mods and bot** ⭐
- **[TESTING.md](TESTING.md)** - **Comprehensive testing guide** ⭐
- **[combat-log/RUNNING.md](combat-log/RUNNING.md)** - **How to run everything** ⭐
- **[combat-log/discord-bot/CONFIG.md](combat-log/discord-bot/CONFIG.md)** - **Detailed config.json guide** ⭐

### Project-Specific Docs
- [combat-log/README.md](combat-log/README.md) - Combat log system overview
- [combat-log/discord-bot/README.md](combat-log/discord-bot/README.md) - Discord bot setup
- [combat-log/combat-log-report-1.21.11/README.md](combat-log/combat-log-report-1.21.11/README.md) - Minecraft mod details

---

## 🚀 Quick Start

### Prerequisites
- **For Discord Bot**: Java 17+
- **For Minecraft Mods**: Java 21+, Fabric Loader 0.18.4+, Fabric API 0.141.3+
- **For Server**: Minecraft 1.21.11

### 1. Build Everything

```bash
# Build Discord Bot
cd combat-log/discord-bot
./gradlew build

# Build Combat Log Mod (switch to Java 21)
export JAVA_HOME=/path/to/java-21
cd ../combat-log-report-1.21.11
./gradlew build

# Build other mods as needed (see TESTING.md)
```

### 2. Configure Discord Bot

```bash
cd combat-log/discord-bot
cp config.example.json config.json
# Edit config.json with your Discord credentials
```

See [CONFIG.md](combat-log/discord-bot/CONFIG.md) for detailed configuration guide.

### 3. Run Discord Bot

```bash
java -jar build/libs/combat-log-discord-bot-1.0.0.jar
```

### 4. Install Minecraft Mods

```bash
# Copy to server mods folder
cp combat-log/combat-log-report-1.21.11/build/libs/combat-log-report-1.0.0.jar /path/to/server/mods/
```

See [RUNNING.md](combat-log/RUNNING.md) for complete setup instructions.

---

## 🎯 Feature Highlights

### Combat Log System
- ✅ **Combat Detection** - 15-second combat timer with action bar countdown
- ✅ **Combat Logging Detection** - Detects disconnects during combat
- ✅ **Player Heads** - Spawns heads with stored inventory
- ✅ **Time-Based Access** - Opponents access heads first, then everyone after 30 min
- ✅ **Discord Integration** - Automatic ticket creation with proof submission
- ✅ **Self-Admission** - Players can admit combat logging
- ✅ **Whitelist System** - Discord-based automatic whitelist with Mojang API validation
- ✅ **Player Linking** - One-to-one Discord ↔ Minecraft account linking
- ✅ **Staff Commands** - `/approve`, `/deny`, `/extend`, `/info`
- ✅ **Rocket Blocking** - Prevents firework rocket use during combat

### Armour Invisibility
- ✅ **Invisibility II Effect** - Hides all armor pieces
- ✅ **Client-Side** - Players with mod see invisible armor
- ✅ **All Armor Types** - Works with any armor

---

## 🔧 Configuration Example

The Discord bot uses a reorganized config structure with:
1. **Essential credentials first** (token, server ID, staff role)
2. **Feature toggles grouped** (all true/false settings)
3. **Timeouts grouped** (all duration settings)
4. **Channel IDs at the end** (all Discord channels)

```json
{
  "discord": {
    "token": "YOUR_BOT_TOKEN_HERE",
    "guildId": "YOUR_GUILD_ID",
    "staffRoleId": "YOUR_STAFF_ROLE_ID"
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
  }
}
```

See [CONFIG.md](combat-log/discord-bot/CONFIG.md) for complete documentation of every field.

---

## 📋 System Requirements

### Discord Bot
- Java 17 or later
- 512MB RAM minimum (1GB recommended)
- Port 8080 open for WebSocket
- Discord bot token with necessary permissions

### Minecraft Server
- Minecraft 1.21.11
- Fabric Loader 0.18.4 or later
- Fabric API 0.141.3+1.21.11 or later
- Java 21 or later
- 2GB RAM minimum (4GB+ recommended)

### Clients (Players)
- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Combat Log mod (required)
- Armour Invisibility mod (optional, for visual effect)

---

## 🧪 Testing

Comprehensive testing guide available in [TESTING.md](TESTING.md).

Quick test checklist:
- [ ] Discord bot connects and registers commands
- [ ] Whitelist system validates Minecraft usernames
- [ ] Combat detection works in-game
- [ ] Combat logging creates Discord tickets
- [ ] Player heads spawn and store inventory
- [ ] Ticket approval/denial works
- [ ] Punishment system executes correctly

---

## 🐛 Known Limitations

### Combat Log Mod
1. **Inventory NBT Storage**: Framework ready but not fully active due to Minecraft 1.21.11 API changes
2. **Player Head Skins**: Don't display correctly (ResolvableProfile API changed in 1.21.11)
3. **Firework Rocket Field**: Warning about "attachedToPlayer" field name in 1.21.11

### Discord Bot
1. **Mojang API Rate Limits**: Built-in caching helps, but excessive requests may be throttled
2. **WebSocket Reconnect**: Minecraft must manually reconnect if bot restarts

See [FEATURES.md](FEATURES.md) for detailed information on all features and limitations.

---

## 📊 Build Status

All components build successfully:

| Component | Build Status | Size | Java Version |
|-----------|-------------|------|--------------|
| Discord Bot | ✅ Successful | ~37MB | Java 17+ |
| Combat Log Mod | ✅ Successful | ~2.9MB | Java 21+ |
| Armour Invisibility | ✅ Successful | ~500KB | Java 21+ |
| SwordsSMP Mod | ✅ Successful | ~1MB | Java 21+ |
| Combined Mod | ✅ Successful | ~1.5MB | Java 21+ |

---

## 🤝 Contributing

This is a private server project for SwordSMP. For suggestions or issues:
1. Review the documentation (especially [FEATURES.md](FEATURES.md))
2. Test your changes using [TESTING.md](TESTING.md)
3. Check existing issues
4. Discuss with server administrators

---

## 📞 Support

### For Setup Help
- Check [RUNNING.md](combat-log/RUNNING.md) for deployment instructions
- See [CONFIG.md](combat-log/discord-bot/CONFIG.md) for configuration details
- Review [TESTING.md](TESTING.md) for troubleshooting

### For Feature Questions
- See [FEATURES.md](FEATURES.md) for complete feature documentation
- Check project-specific READMEs for detailed information

### Common Issues
- **"Bot token not configured"**: Edit config.json with your actual bot token
- **"Cannot find guild"**: Verify guildId is correct
- **"WebSocket connection failed"**: Check bot is running and port 8080 is open
- **"Mod won't load"**: Ensure Java 21 and Fabric API are installed

---

## 🎮 For SwordSMP

This system provides:
- ✅ Fair PvP combat enforcement
- ✅ Transparent combat logging detection
- ✅ Community-driven moderation
- ✅ Reduced admin workload through automation
- ✅ Player appeals process with proof submission
- ✅ Automatic whitelist management
- ✅ Discord-Minecraft integration

---

## 📜 License

See LICENSE file in the root of the repository.

---

## ⭐ Quick Links

- **[📖 Complete Features Documentation](FEATURES.md)**
- **[🧪 Testing Guide](TESTING.md)**
- **[⚙️ Configuration Guide](combat-log/discord-bot/CONFIG.md)**
- **[🚀 Running Guide](combat-log/RUNNING.md)**
- **[🤖 Discord Bot Setup](combat-log/discord-bot/README.md)**
- **[🎮 Minecraft Mod Details](combat-log/combat-log-report-1.21.11/README.md)**

---

**Status**: ✅ All components functional and documented

Last Updated: February 2026
