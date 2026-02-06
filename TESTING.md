# Testing Guide for SwordSMP Mods

This guide provides comprehensive testing procedures for all mods and the Discord bot.

## 🧪 Overview

This repository contains multiple components that need to be tested:

1. **Discord Bot** - Combat log ticket system and whitelist management
2. **Combat Log Minecraft Mod** - Combat detection and logging
3. **Armour Invisibility Mod** - Hides armor with Invisibility II
4. **SwordsSMP Mod** - Custom server features
5. **Combined Mod** - SwordsSMP + Armour Invisibility

---

## 📋 Pre-Testing Checklist

### Build All Components

```bash
# Build Discord Bot (requires Java 17+)
cd combat-log/discord-bot
./gradlew build

# Build Combat Log Mod (requires Java 21+)
cd ../combat-log-report-1.21.11
./gradlew build

# Build Armour Invisibility Mod (requires Java 21+)
cd ../../armour-invis-template-1.21.11
./gradlew build

# Build SwordsSMP Mod (requires Java 21+)
cd "../swordsmp 1.21.11"
./gradlew build

# Build Combined Mod (requires Java 21+)
cd "../swordsmp 1.21.11 compined with armourinvis"
./gradlew build
```

### Verify Builds

Check that JAR files were created:

```bash
# Discord Bot (should be ~37MB)
ls -lh combat-log/discord-bot/build/libs/combat-log-discord-bot-1.0.0.jar

# Combat Log Mod (should be ~2.9MB)
ls -lh combat-log/combat-log-report-1.21.11/build/libs/combat-log-report-1.0.0.jar

# Armour Invisibility Mod
ls -lh armour-invis-template-1.21.11/build/libs/*.jar

# SwordsSMP Mod
ls -lh "swordsmp 1.21.11"/build/libs/*.jar

# Combined Mod
ls -lh "swordsmp 1.21.11 compined with armourinvis"/build/libs/*.jar
```

---

## 🤖 Discord Bot Testing

### Setup

1. **Create Test Discord Server**
   - Create a new Discord server for testing (or use test channels in existing server)
   - Create required channels:
     - `#combat-log-tickets` (Forum channel)
     - `#whitelist` (Text channel)
     - `#staff-review` (Text channel)
   - Create a staff role (e.g., "Moderator")

2. **Configure Bot**
   ```bash
   cd combat-log/discord-bot
   cp config.example.json config.json
   ```
   
   Edit `config.json`:
   ```json
   {
     "discord": {
       "token": "YOUR_ACTUAL_BOT_TOKEN",
       "guildId": "YOUR_SERVER_ID",
       "staffRoleId": "YOUR_STAFF_ROLE_ID"
     },
     "channels": {
       "ticketChannelId": "YOUR_FORUM_CHANNEL_ID",
       "whitelistChannelId": "YOUR_WHITELIST_CHANNEL_ID",
       "reviewChannelId": "YOUR_REVIEW_CHANNEL_ID"
     }
   }
   ```

3. **Start Bot**
   ```bash
   java -jar build/libs/combat-log-discord-bot-1.0.0.jar
   ```

### Test Cases

#### ✅ Test 1: Bot Connection
- **Expected**: Bot connects and shows as online in Discord
- **Verify**: Check console output for:
  ```
  Discord bot connected as: YourBotName
  WebSocket server listening on 0.0.0.0:8080
  Combat Log Discord Bot is ready!
  ```

#### ✅ Test 2: Slash Commands Registration
- **Expected**: Slash commands appear in Discord
- **Verify**: Type `/` in Discord and check for these commands:
  - `/approve`
  - `/deny`
  - `/extend`
  - `/info`
  - `/whitelist-setup`

#### ✅ Test 3: Whitelist Setup
- **Action**: Run `/whitelist-setup channel_id:<whitelist-channel-id>`
- **Expected**: Button message posted in whitelist channel
- **Verify**:
  - Message has green embed with title "🎫 Request Server Whitelist"
  - "Request Whitelist" button present

#### ✅ Test 4: Whitelist Request (Valid Username)
- **Action**: Click "Request Whitelist" button, enter "Notch"
- **Expected**: 
  - Modal opens for username input
  - Bot validates with Mojang API
  - Success message sent to user
  - Link stored in database
- **Verify**:
  - Check console for: `Validated username: Notch`
  - Check database: `sqlite3 database/whitelist.db "SELECT * FROM player_links;"`

#### ✅ Test 5: Whitelist Request (Invalid Username)
- **Action**: Click button, enter "ThisUserDoesNotExist123456789"
- **Expected**: Error message "Invalid Minecraft username"
- **Verify**: No database entry created

#### ✅ Test 6: Duplicate Whitelist Prevention
- **Action**: Same Discord user requests whitelist twice
- **Expected**: Error message "You are already linked"
- **Verify**: Only one database entry exists

### Manual WebSocket Testing

To test without Minecraft, use a WebSocket client:

```bash
# Install websocat (WebSocket CLI tool)
# On Linux: sudo apt install websocat
# On macOS: brew install websocat

# Connect to bot
websocat ws://localhost:8080/combat-log
```

Send test messages:

```json
// Test combat log incident
{
  "type": "combat_log_incident",
  "incidentId": "test_123",
  "playerName": "TestPlayer",
  "playerUuid": "12345678-1234-1234-1234-123456789012",
  "opponents": ["Player2"],
  "combatTimeRemaining": 10.5,
  "location": {"x": 100, "y": 64, "z": 200},
  "timestamp": 1677777777000
}
```

**Expected**: Ticket created in combat-log-tickets channel

---

## 🎮 Minecraft Mod Testing

### Setup Test Server

1. **Install Fabric Server**
   ```bash
   # Download Fabric Installer for Minecraft 1.21.11
   # Install server with Fabric Loader 0.18.4+
   
   # Download Fabric API
   # Place in mods folder
   ```

2. **Install Mods**
   ```bash
   # Copy mods to server mods folder
   cp combat-log/combat-log-report-1.21.11/build/libs/combat-log-report-1.0.0.jar server/mods/
   
   # Optional: Add armour invisibility or combined mod
   ```

3. **Configure Combat Log Mod**
   
   First run will create config file: `config/combat-log-report.json`
   
   Edit it:
   ```json
   {
     "socket": {
       "enabled": true,
       "serverUrl": "ws://localhost:8080/combat-log"
     }
   }
   ```

4. **Start Server**
   ```bash
   java -jar server.jar nogui
   ```

### Combat Log Mod Test Cases

#### ✅ Test 1: Mod Loading
- **Expected**: Mod loads without errors
- **Verify**: Check server log for:
  ```
  [combat-log-report] Combat Log Report mod initialized!
  [combat-log-report] Attempting to connect to Discord bot at ws://localhost:8080/combat-log
  [combat-log-report] Connected to Discord bot WebSocket server
  ```

#### ✅ Test 2: Combat Detection
- **Setup**: Two test players (or use command blocks for testing)
- **Action**: Player1 hits Player2
- **Expected**:
  - Both players see: "§e§lYou are now in combat! Logging out will be reported for 15 seconds!"
  - Action bar shows: "Combat 15 seconds"
- **Verify**: Check combat timers are active

#### ✅ Test 3: Combat Timer Countdown
- **Action**: Wait during combat (don't hit again)
- **Expected**:
  - Action bar updates: "Combat 14 seconds", "Combat 13 seconds", etc.
  - At 5 seconds: "§eCombat ends in 5 seconds..."
  - At 0 seconds: "§aYou are no longer in combat!"
- **Verify**: Players can safely log out after message

#### ✅ Test 4: Combat Timer Reset
- **Action**: Player1 hits Player2 again after 5 seconds
- **Expected**: Timer resets to 15 seconds
- **Verify**: Action bar shows "Combat 15 seconds"

#### ✅ Test 5: Combat Logging Detection
- **Action**: Player1 disconnects during active combat
- **Expected**:
  - Player head spawns at logout location
  - Server broadcasts: "§e[Combat Log Report] §cPlayer1 logged out during combat with X.X seconds remaining!"
  - Discord bot receives WebSocket message
  - Ticket created in Discord
- **Verify**:
  - Check player head exists in world
  - Check Discord for new ticket
  - Check bot console for incident message

#### ✅ Test 6: Firework Rocket Blocking
- **Setup**: Player in combat with elytra and firework rockets
- **Action**: Try to use firework rocket while in combat
- **Expected**: 
  - Rocket doesn't launch
  - Action bar shows: "Cannot use rockets while in combat!"
- **Verify**: Player cannot fly with elytra during combat

#### ✅ Test 7: Player Head Access Control (First 30 minutes)
- **Setup**: Player1 combat logs, Player2 was opponent
- **Action**: Player2 tries to open head within 30 minutes
- **Expected**: Head opens (opponent can access)
- **Action**: Player3 (not opponent) tries to open head
- **Expected**: Head doesn't open or access denied

#### ✅ Test 8: Player Head OP Override
- **Setup**: Combat log head exists
- **Action**: OP player tries to open any head
- **Expected**: Opens successfully (OPs can always access)
- **Verify**: Permission level 2+ can access

#### ✅ Test 9: Combat End on Death
- **Action**: Player1 hits Player2, then Player1 dies
- **Expected**: Combat immediately ends for Player2
- **Verify**: Player2 sees "§aYou are no longer in combat!"

#### ✅ Test 10: Ticket Approval Flow
- **Action**: 
  1. Player combat logs
  2. Staff uses `/approve <incident_id>`
- **Expected**:
  - Player logs back in
  - Player is NOT killed
  - Inventory restored from head
  - Head removed from world
- **Verify**: Player can play normally

#### ✅ Test 11: Ticket Denial Flow
- **Action**:
  1. Player combat logs
  2. Staff uses `/deny <incident_id>`
- **Expected**:
  - Player logs back in
  - Player is killed
  - Items remain in head
  - After 30 minutes, everyone can access head
- **Verify**: Punishment applied correctly

#### ✅ Test 12: Self-Admission
- **Action**:
  1. Player combat logs
  2. Player clicks "I Admit Combat Log" button
  3. Types "I admit" in modal
- **Expected**: Same as denial (player killed on login)
- **Verify**: Ticket shows "SELF-ADMIT" reason

#### ✅ Test 13: Player Unlinking
- **Action**: Linked player types `/unlink` in Minecraft
- **Expected**:
  - Confirmation message
  - Removed from whitelist
  - Link removed from database
- **Verify**: 
  - Check database: link removed
  - Player no longer whitelisted
  - Can request whitelist again

---

## 🛡️ Armour Invisibility Mod Testing

### Setup

1. **Install Mod (Client and Server)**
   ```bash
   # Copy to both client and server mods folders
   cp armour-invis-template-1.21.11/build/libs/*.jar minecraft/mods/
   ```

2. **Start Minecraft**

### Test Cases

#### ✅ Test 1: Mod Loading
- **Expected**: Mod loads without errors
- **Verify**: Check logs for mod initialization

#### ✅ Test 2: Invisibility I (Armor Visible)
- **Setup**: Player with full armor
- **Action**: Apply Invisibility I effect: `/effect give @s invisibility 60 0`
- **Expected**: Armor remains visible
- **Verify**: Other players can see the armor

#### ✅ Test 3: Invisibility II (Armor Hidden)
- **Action**: Apply Invisibility II effect: `/effect give @s invisibility 60 1`
- **Expected**: Armor becomes invisible
- **Verify**: 
  - Own armor visible to player
  - Armor invisible to other players (with mod)
  - Armor visible to players without mod (client-side)

#### ✅ Test 4: Effect Removal
- **Action**: Remove invisibility effect: `/effect clear @s invisibility`
- **Expected**: Armor becomes visible again
- **Verify**: Smooth transition back to visible

#### ✅ Test 5: All Armor Slots
- **Setup**: Full armor (helmet, chestplate, leggings, boots)
- **Action**: Apply Invisibility II
- **Expected**: All armor pieces become invisible
- **Verify**: Each piece individually invisible

---

## ⚔️ SwordsSMP Mod Testing

### Setup

1. **Install Mod**
   ```bash
   cp "swordsmp 1.21.11"/build/libs/*.jar minecraft/mods/
   ```

2. **Start Minecraft**

### Test Cases

**Note**: Specific test cases depend on what custom features this mod implements. Check the mod's source code for:
- Custom items
- Custom blocks
- Custom mechanics
- Server-specific features

#### ✅ Test 1: Mod Loading
- **Expected**: Mod loads without errors
- **Verify**: Check logs for "SwordsSMP" initialization

#### ✅ Test 2: Custom Features
- **Action**: Test any custom items/blocks/mechanics
- **Expected**: Features work as designed
- **Verify**: No errors or crashes

---

## 🎯 Combined Mod Testing

The combined mod includes both SwordsSMP and Armour Invisibility features.

### Setup

```bash
cp "swordsmp 1.21.11 compined with armourinvis"/build/libs/*.jar minecraft/mods/
```

### Test Cases

Run all test cases from:
- **Armour Invisibility Mod Testing**
- **SwordsSMP Mod Testing**

#### ✅ Test 1: Both Mods Work Together
- **Expected**: No conflicts between features
- **Verify**: Both mod features functional

---

## 🔍 Integration Testing

### Full System Test

1. **Start Discord Bot**
2. **Start Minecraft Server with Combat Log Mod**
3. **Connect Two Test Clients**

#### End-to-End Combat Log Flow

1. **Player1 and Player2 enter combat**
   - ✅ Both see combat messages
   - ✅ Action bar countdown shows
   
2. **Player1 disconnects during combat**
   - ✅ Player head spawns
   - ✅ Discord ticket created
   - ✅ If Player1 linked, added to thread
   
3. **Player1 submits proof in Discord**
   - ✅ Bot detects proof URL
   - ✅ Ticket status updates to "CLIP_UPLOADED"
   
4. **Staff reviews and approves**
   - ✅ `/approve` command works
   - ✅ Decision sent to Minecraft
   
5. **Player1 logs back in**
   - ✅ NOT killed
   - ✅ Inventory restored
   - ✅ Head removed

#### End-to-End Whitelist Flow

1. **New player joins Discord**
2. **Player clicks "Request Whitelist"**
   - ✅ Modal opens
   
3. **Player enters Minecraft username**
   - ✅ Mojang API validates
   - ✅ Link created
   - ✅ Whitelist command sent to Minecraft
   
4. **Player joins Minecraft server**
   - ✅ Can connect (whitelisted)
   - ✅ Can use `/unlink` command

---

## 📊 Performance Testing

### Discord Bot Performance

```bash
# Monitor bot resource usage
# While bot is running:
ps aux | grep java
# Check memory usage (should be < 200MB under normal load)
```

### Minecraft Server Performance

```bash
# Use /debug start and /debug stop in Minecraft
# Check TPS (ticks per second)
# Should maintain 20 TPS with mods installed
```

---

## 🐛 Known Issues & Limitations

### Combat Log Mod
1. **Inventory NBT Storage**: Framework ready but not fully active due to Minecraft 1.21.11 API changes
2. **Player Head Skins**: Don't display correctly (ResolvableProfile API changed)
3. **Firework Rocket Field Warning**: Field name may be different in 1.21.11

### Discord Bot
1. **Rate Limits**: Mojang API has rate limits
2. **WebSocket Reconnect**: Manual reconnect needed if bot restarts

---

## ✅ Testing Checklist Summary

### Discord Bot
- [ ] Bot connects successfully
- [ ] Slash commands registered
- [ ] Whitelist setup works
- [ ] Whitelist requests validated
- [ ] Duplicate prevention works
- [ ] Combat log tickets created
- [ ] Self-admission works
- [ ] Ticket approval/denial works
- [ ] WebSocket communication works

### Combat Log Mod
- [ ] Mod loads without errors
- [ ] Combat detection works
- [ ] Combat timer counts down
- [ ] Combat timer resets on hit
- [ ] Combat ends on death
- [ ] Combat logging detected
- [ ] Player heads spawn
- [ ] Firework rockets blocked
- [ ] Head access control works
- [ ] OP override works
- [ ] Punishment system works
- [ ] `/unlink` command works

### Armour Invisibility Mod
- [ ] Mod loads without errors
- [ ] Invisibility I keeps armor visible
- [ ] Invisibility II hides armor
- [ ] Effect removal shows armor
- [ ] All armor slots hide

### Integration
- [ ] Discord bot and Minecraft communicate
- [ ] Full combat log flow works
- [ ] Full whitelist flow works
- [ ] No performance issues

---

## 📞 Troubleshooting

### Bot Won't Connect
1. Check bot token is correct
2. Verify intents are enabled in Developer Portal
3. Check firewall settings for port 8080

### Mod Won't Load
1. Verify Java 21 is installed
2. Check Fabric API is installed
3. Review server logs for errors

### WebSocket Connection Fails
1. Ensure bot is running before starting Minecraft
2. Check firewall allows port 8080
3. Verify URL in mod config is correct

### Tickets Not Creating
1. Check channel IDs are correct
2. Verify bot has permissions in channels
3. Check WebSocket connection is active

---

## 📝 Test Reporting

When reporting test results, include:

1. **Environment**
   - Java version
   - Minecraft version
   - Fabric Loader version
   - Operating system

2. **Test Results**
   - Which tests passed
   - Which tests failed
   - Error messages
   - Screenshots (for visual issues)

3. **Logs**
   - Discord bot console output
   - Minecraft server logs
   - Client logs (if relevant)

---

## 🎓 Next Steps

After testing:

1. **Document any issues** found during testing
2. **Report bugs** with detailed reproduction steps
3. **Suggest improvements** based on testing experience
4. **Update documentation** with any new findings

---

For more information, see:
- [FEATURES.md](../FEATURES.md) - Complete feature documentation
- [CONFIG.md](combat-log/discord-bot/CONFIG.md) - Configuration guide
- [RUNNING.md](combat-log/RUNNING.md) - How to run everything
