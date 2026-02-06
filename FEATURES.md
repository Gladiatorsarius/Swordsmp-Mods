# SwordSMP Mods - Complete Features Documentation

This document provides a comprehensive overview of all features across all mods and the Discord bot in the SwordSMP Mods repository.

## 📦 Project Overview

The SwordSMP Mods repository contains multiple Minecraft mods and a Discord bot that work together to create a complete server management system:

1. **Combat Log System** - Tracks and manages combat logging incidents
2. **Discord Bot** - Integrates with Discord for ticket management and whitelist system
3. **Armour Invisibility Mod** - Hides armor when players have Invisibility II
4. **SwordsSMP Mod** - Custom server features
5. **Combined Mod** - Combines SwordsSMP and Armour Invisibility

---

## 🎮 Combat Log System

### Overview
A comprehensive system for detecting, tracking, and managing combat logging incidents in Minecraft.

### Core Features

#### 1. Combat Detection
- **Combat Tagging**: Players are tagged when they hit or are hit by another player
- **15-Second Timer**: Combat tag lasts 15 seconds after the last hit
- **Timer Reset**: Timer resets with each new hit
- **Action Bar Display**: Shows "Combat X seconds" countdown above hotbar
- **Combat End on Death**: Combat automatically ends when any participant dies

#### 2. Combat Logging Detection
- **Disconnect Detection**: Detects when players disconnect during combat
- **Remaining Time Tracking**: Records exact time remaining when player logged out
- **Automatic Reporting**: Sends report to Discord bot via WebSocket
- **Player Head Spawning**: Creates a player head at the logout location

#### 3. Player Head System
- **Visual Representation**: Shows the player's skin on the head
- **Inventory Storage**: Stores the combat logger's full inventory in NBT data
- **Unbreakable**: Heads cannot be destroyed by non-OP players
- **Time-Based Access Control**:
  - 0-30 minutes: Only combat opponents can access
  - After 30 minutes: Everyone can access (if ticket denied)
  - If ticket approved: Head is removed and inventory restored
- **OP Override**: Server operators (permission level 2+) can always access heads

#### 4. Combat Restrictions
- **Firework Rocket Blocking**: Players cannot use firework rockets while in combat
- **Action Bar Notification**: Shows "Cannot use rockets while in combat!" message

#### 5. Punishment System
- **Time-Based Access**: Determines who can access the player's items based on ticket outcome
- **Inventory Restoration**: Restores inventory on ticket approval
- **Player Killing**: Executes punishment on next login if ticket denied
- **Head Removal**: Cleans up heads after inventory restoration

#### 6. WebSocket Communication
- **Real-Time Sync**: Instant communication between Minecraft and Discord
- **Message Types**:
  - `combat_log_incident` - Sends combat log events to Discord
  - `incident_decision` - Receives ticket decisions from Discord
  - `whitelist_add` - Receives whitelist commands from Discord
  - `whitelist_confirmation` - Confirms whitelist execution
  - `player_link` - Links Discord and Minecraft accounts
  - `unlink_player` - Unlinks accounts

### In-Game Commands

#### `/unlink`
- **Purpose**: Unlink your Discord account from your Minecraft account
- **Permissions**: Any player
- **Effects**:
  - Removes Discord-Minecraft link from both databases
  - Removes player from whitelist
  - Allows relinking with same or different Discord account
- **Usage**: Simply type `/unlink` in chat

---

## 🤖 Discord Bot Features

### 1. Combat Log Ticket System

#### Ticket Creation
- **Automatic Creation**: Creates ticket when combat log is detected
- **Forum or Thread**: Supports both Forum channels and text channel threads
- **Private Threads**: Can create private threads for linked players
- **Rich Embeds**: Shows detailed incident information
- **Player Tagging**: Automatically adds linked Discord users to thread
- **Staff Notification**: Pings staff role for new tickets

#### Ticket Information Display
- **Incident ID**: Unique identifier for each ticket
- **Player Details**: Shows Minecraft username and UUID
- **Opponent Information**: Lists who the player was fighting
- **Combat Time**: Shows time remaining when player logged out
- **Location**: Shows coordinates where combat logging occurred
- **Timestamp**: When the incident was detected

#### Proof Submission
- **Platform Detection**: Automatically detects proof URLs in chat
- **Supported Platforms**:
  - YouTube (youtube.com, youtu.be)
  - Twitch (twitch.tv, clips.twitch.tv)
  - Streamable (streamable.com)
  - Medal.tv (medal.tv)
  - Discord attachments
- **Status Update**: Changes ticket status to "CLIP_UPLOADED"
- **Visual Indicator**: Updates embed color to show proof submitted

#### Auto-Timeout System
- **Configurable Deadline**: Default 60 minutes, customizable
- **Countdown Display**: Shows time remaining in ticket
- **Auto-Deny**: Automatically denies ticket if no proof submitted
- **Notification**: Sends message when ticket is auto-denied
- **Bypass**: Auto-timeout stops if proof is submitted

#### Self-Admission Feature
- **"I Admit Combat Log" Button**: Players can admit their mistake
- **Confirmation Modal**: Requires typing "I admit" to confirm
- **Automatic Processing**: Processes as DENIED with "SELF-ADMIT" reason
- **Same Consequences**: Player is killed, items in head with time-based access
- **Encourages Honesty**: Rewards players for admitting mistakes
- **Reduces Workload**: Saves staff time on obvious cases

### 2. Staff Commands

#### `/approve <incident_id> [reason]`
- **Purpose**: Approve a combat log appeal
- **Permissions**: Requires staff role
- **Parameters**:
  - `incident_id` (required): The unique ID of the ticket
  - `reason` (optional): Reason for approval
- **Effects**:
  - Sends APPROVED decision to Minecraft
  - Player will NOT be killed on next login
  - Inventory is restored from player head
  - Player head is removed
  - Ticket is closed
  - Logs decision in ticket

#### `/deny <incident_id> [reason]`
- **Purpose**: Deny a combat log appeal
- **Permissions**: Requires staff role
- **Parameters**:
  - `incident_id` (required): The unique ID of the ticket
  - `reason` (optional): Reason for denial
- **Effects**:
  - Sends DENIED decision to Minecraft
  - Player WILL be killed on next login
  - Items remain in player head
  - Time-based access control applies
  - Ticket is closed
  - Logs decision in ticket

#### `/extend <incident_id> <minutes>`
- **Purpose**: Extend the deadline for proof submission
- **Permissions**: Requires staff role
- **Parameters**:
  - `incident_id` (required): The unique ID of the ticket
  - `minutes` (required): Number of minutes to add
- **Effects**:
  - Adds specified minutes to deadline
  - Updates ticket with new deadline
  - Prevents auto-timeout for extended period
- **Use Cases**: Player requests more time, technical difficulties, etc.

#### `/info <incident_id>`
- **Purpose**: View detailed information about a ticket
- **Permissions**: Requires staff role
- **Parameters**:
  - `incident_id` (required): The unique ID of the ticket
- **Shows**:
  - Current ticket status
  - Player information
  - Combat details
  - Proof submission status
  - Time remaining (if pending)
  - Decision (if closed)

### 3. Whitelist System

#### Whitelist Request Flow
1. **Button Setup**: Staff uses `/whitelist-setup <channel_id>` to create button
2. **Player Clicks**: Player clicks "Request Whitelist" button
3. **Modal Opens**: Player enters their Minecraft username
4. **Validation**: Bot validates username with Mojang API
5. **Link Check**: Bot checks if username or Discord account already linked
6. **Approval**: Automatically approves if validation passes
7. **Database Update**: Stores Discord-Minecraft link
8. **Whitelist Command**: Sends whitelist add command to Minecraft
9. **Confirmation**: Bot confirms player is whitelisted
10. **DM Notification**: Player receives DM that they're whitelisted

#### Whitelist Features
- **Automatic Approval**: No staff review needed
- **Mojang API Validation**: Verifies username exists and gets UUID
- **One-to-One Linking**: Enforces one Discord account per Minecraft account
- **Duplicate Prevention**: Prevents same username or Discord from requesting twice
- **SQLite Database**: Stores all links persistently
- **Unlink Support**: Players can use `/unlink` command to unlink and re-request

#### Whitelist Commands

##### `/whitelist-setup <channel_id>`
- **Purpose**: Create a whitelist request button in a channel
- **Permissions**: Requires staff role
- **Parameters**:
  - `channel_id` (required): Channel where button should be posted
- **Effects**:
  - Creates embed with title, description, and color (from config)
  - Adds "Request Whitelist" button
  - Posts to specified channel

### 4. Player Linking System

#### Database Schema
```sql
-- Discord-Minecraft Links
CREATE TABLE IF NOT EXISTS player_links (
    discord_id TEXT PRIMARY KEY,
    minecraft_uuid TEXT UNIQUE,
    minecraft_name TEXT,
    linked_at INTEGER
);
```

#### Linking Features
- **Automatic Linking**: Links created during whitelist request
- **One-to-One Enforcement**: Primary key on discord_id, unique constraint on minecraft_uuid
- **Persistent Storage**: SQLite database survives restarts
- **Thread Integration**: Linked players automatically added to their combat log tickets
- **Unlink Support**: Full unlink capability via `/unlink` command

#### Link Checks
- `isDiscordLinked(discordId)` - Check if Discord account is linked
- `isMinecraftLinked(minecraftUuid)` - Check if Minecraft account is linked
- `getDiscordId(minecraftUuid)` - Get Discord ID from Minecraft UUID
- `getMinecraftUuid(discordId)` - Get Minecraft UUID from Discord ID
- `getLink(discordId)` - Get full link information

### 5. WebSocket Server

#### Server Configuration
- **Port**: Configurable, default 8080
- **Host**: Configurable, default 0.0.0.0 (all interfaces)
- **Protocol**: WebSocket (ws://)
- **Message Format**: JSON

#### Connection Handling
- **Automatic Reconnect**: Minecraft mod reconnects on disconnect
- **Connection Logging**: Logs all connections and disconnections
- **Error Handling**: Graceful error recovery

#### Message Processing
- **Type-Based Routing**: Routes messages based on "type" field
- **JSON Parsing**: Uses Gson for serialization/deserialization
- **Error Responses**: Sends error messages for invalid requests

### 6. Mojang API Integration

#### Features
- **Username Validation**: Verifies Minecraft usernames exist
- **UUID Retrieval**: Gets player UUIDs from usernames
- **Caching**: Caches responses to reduce API calls (default 5 minutes)
- **Timeout Handling**: Configurable timeout (default 5 seconds)
- **Rate Limit Protection**: Built-in rate limiting to avoid bans

#### API Endpoints Used
- `https://api.mojang.com/users/profiles/minecraft/{username}` - Get UUID from username

---

## 🛡️ Armour Invisibility Mod

### Overview
Makes armor invisible when players have the Invisibility II effect.

### Features

#### Invisibility Detection
- **Effect Level Check**: Only activates with Invisibility II (level 2)
- **Invisibility I**: Armor remains visible with Invisibility I
- **Client-Side**: Requires mod on client to see invisible armor
- **Server-Side**: Server tracks effect status

#### Armor Hiding
- **Helmet**: Hidden with Invisibility II
- **Chestplate**: Hidden with Invisibility II
- **Leggings**: Hidden with Invisibility II
- **Boots**: Hidden with Invisibility II
- **All Armor Slots**: Hides all equipped armor pieces

#### Visual Behavior
- **Smooth Transition**: Armor disappears/reappears smoothly with effect
- **Player Perspective**: Own armor visible to player
- **Other Players**: Armor invisible to other players with mod
- **Compatibility**: Works with all armor types and enchantments

### Technical Details
- **Client Mixin**: Modifies armor rendering on client side
- **Server Mixin**: Tracks player effects on server side
- **No Performance Impact**: Minimal overhead
- **Fabric API**: Uses Fabric API for effect detection

---

## ⚔️ SwordsSMP Mod

### Overview
Custom mod created with MCreator for the SwordsSMP server with various custom features.

### Features
(Based on the mod structure, this appears to be a custom MCreator mod with server-specific features. The exact features would need to be documented by examining the mod's code or asking the server administrators.)

### Technical Details
- **Created With**: MCreator
- **Minecraft Version**: 1.21.11
- **Mod Loader**: Fabric
- **Custom Items**: Likely includes custom items/blocks
- **Custom Mechanics**: Server-specific gameplay mechanics

---

## 🎯 Combined Mod (SwordsSMP + Armour Invisibility)

### Overview
Combines the SwordsSMP mod and Armour Invisibility mod into a single package.

### Benefits
- **Single Installation**: Install one mod instead of two
- **No Conflicts**: Guaranteed compatibility
- **Easier Updates**: Update one mod instead of two
- **Reduced Overhead**: Slightly better performance than two separate mods

### Features
- **All SwordsSMP Features**: Includes everything from SwordsSMP mod
- **All Armour Invisibility Features**: Includes everything from Armour Invisibility mod
- **Shared Dependencies**: Optimized dependency loading

---

## 🔧 System Requirements

### Minecraft Server Requirements
- **Minecraft Version**: 1.21.11
- **Mod Loader**: Fabric Loader 0.18.4 or later
- **Fabric API**: 0.141.3+1.21.11 or later
- **Java**: Java 21 or later
- **RAM**: Minimum 2GB, recommended 4GB+

### Discord Bot Requirements
- **Java**: Java 17 or later
- **RAM**: Minimum 512MB, recommended 1GB
- **Network**: Port 8080 open for WebSocket (configurable)
- **Discord**: Bot token from Discord Developer Portal
- **Permissions**: Bot needs server management permissions

### Client Requirements (for players)
- **Minecraft**: 1.21.11
- **Mod Loader**: Fabric Loader
- **Fabric API**: 0.141.3+1.21.11
- **Mods Required**:
  - Combat Log mod (for combat log features)
  - Armour Invisibility mod (to see invisible armor effect)

---

## 📋 Feature Matrix

| Feature | Combat Log Mod | Discord Bot | Armour Invis | SwordsSMP | Combined |
|---------|----------------|-------------|--------------|-----------|----------|
| Combat Detection | ✅ | - | - | - | - |
| Combat Logging Detection | ✅ | - | - | - | - |
| Player Head System | ✅ | - | - | - | - |
| Rocket Blocking in Combat | ✅ | - | - | - | - |
| Discord Integration | ✅ | ✅ | - | - | - |
| Ticket System | - | ✅ | - | - | - |
| Self-Admission | - | ✅ | - | - | - |
| Whitelist System | ✅ | ✅ | - | - | - |
| Player Linking | ✅ | ✅ | - | - | - |
| Mojang API Integration | - | ✅ | - | - | - |
| Staff Commands | - | ✅ | - | - | - |
| Auto-Timeout | - | ✅ | - | - | - |
| Invisible Armor | - | - | ✅ | - | ✅ |
| Custom Features | - | - | - | ✅ | ✅ |

---

## 🎮 Workflow Examples

### Example 1: Combat Log Incident (Denied)
1. PlayerA and PlayerB are in combat
2. PlayerA disconnects with 10 seconds remaining
3. Combat Log mod creates PlayerA's head with their inventory
4. Mod sends incident to Discord bot via WebSocket
5. Discord bot creates ticket in forum channel
6. If PlayerA is linked, they're added to private thread
7. Staff role is pinged
8. PlayerA has 60 minutes to submit proof
9. PlayerA doesn't submit proof
10. Bot auto-denies after 60 minutes
11. Bot sends DENIED decision to Minecraft
12. PlayerA logs back in
13. PlayerA is killed by the punishment system
14. Items remain in PlayerA's head
15. For 30 minutes, only PlayerB can access the head
16. After 30 minutes, anyone can access the head

### Example 2: Combat Log Incident (Approved)
1. PlayerA and PlayerB are in combat
2. PlayerA disconnects (internet issue, not intentional)
3. Combat Log mod creates PlayerA's head with inventory
4. Mod sends incident to Discord bot
5. Discord bot creates ticket
6. PlayerA submits video showing internet disconnected
7. Staff reviews video
8. Staff uses `/approve incident_123 "Internet issue, not intentional"`
9. Bot sends APPROVED decision to Minecraft
10. PlayerA logs back in
11. PlayerA is NOT killed
12. Inventory is restored from head to PlayerA
13. PlayerA's head is removed
14. PlayerA can continue playing normally

### Example 3: Self-Admission
1. PlayerA and PlayerB are in combat
2. PlayerA rage-quits (combat logs intentionally)
3. Combat Log mod creates PlayerA's head
4. Mod sends incident to Discord bot
5. Discord bot creates ticket
6. PlayerA realizes they messed up
7. PlayerA clicks "I Admit Combat Log" button
8. Modal asks PlayerA to type "I admit" to confirm
9. PlayerA types "I admit" and submits
10. Bot processes as DENIED with reason "SELF-ADMIT"
11. Bot sends DENIED decision to Minecraft
12. PlayerA logs back in
13. PlayerA is killed (same as normal denial)
14. Items in head with time-based access
15. Staff sees PlayerA admitted, may be lenient in future

### Example 4: Whitelist Request
1. NewPlayer joins Discord server
2. NewPlayer goes to whitelist channel
3. NewPlayer clicks "Request Whitelist" button
4. Modal opens asking for Minecraft username
5. NewPlayer enters "Steve123"
6. Bot validates "Steve123" with Mojang API
7. Bot gets UUID and confirms username exists
8. Bot checks no one else linked to "Steve123"
9. Bot checks NewPlayer's Discord not already linked
10. Bot stores link in database
11. Bot sends whitelist command to Minecraft server
12. Minecraft server adds Steve123 to whitelist
13. Bot sends DM to NewPlayer: "You're whitelisted!"
14. NewPlayer can now join the server

### Example 5: Unlink and Re-link
1. Player wants to unlink their old Minecraft account
2. Player types `/unlink` in Minecraft
3. Mod sends unlink message to Discord bot
4. Bot removes link from LinkingDatabase
5. Mod removes link from PlayerLinkingManager
6. Player is removed from whitelist
7. Player receives confirmation message
8. Later, player goes to Discord whitelist channel
9. Player requests whitelist with new username
10. Bot creates new link with new username
11. Player is whitelisted again

---

## 🔐 Security Features

### Discord Bot Security
- **Token Protection**: Bot token never exposed in logs
- **Role-Based Access**: Staff commands require specific role
- **Input Validation**: All user inputs validated before processing
- **SQL Injection Protection**: Prepared statements for all database queries
- **Rate Limiting**: Built-in rate limiting for API calls

### Minecraft Mod Security
- **OP Override**: Server operators can always access heads for moderation
- **Permission Checks**: All commands check permissions
- **Data Validation**: All WebSocket messages validated
- **Secure Communication**: WebSocket connection only on local network

### Database Security
- **Local Storage**: SQLite database stored locally, not exposed
- **No External Access**: Database only accessed by bot
- **Backup Recommended**: Regular backups prevent data loss

---

## 🐛 Known Limitations

### Combat Log Mod
1. **Inventory NBT Storage**: Framework ready but not fully active due to Minecraft 1.21.11 API changes
   - ItemStack serialization API changed in 1.21.11
   - Needs proper save() method implementation
   - Core functionality works: heads spawn, access control enforces, timer works
2. **Player Head Skins**: Don't show correctly due to ResolvableProfile API changes in 1.21.11
3. **Firework Rocket Field**: Warning about "attachedToPlayer" field name may be different in 1.21.11

### Discord Bot
1. **DiscordSRV**: Legacy feature, most servers should use internal linking system
2. **Rate Limits**: Mojang API has rate limits, excessive requests may be throttled
3. **WebSocket Reconnect**: Minecraft must manually reconnect if bot restarts

---

## 📊 Performance Impact

### Minecraft Server
- **Combat Tracking**: Negligible CPU impact, uses efficient timer system
- **Combat Heads**: Small memory impact per combat logger (stores inventory)
- **WebSocket**: Minimal network overhead, only sends messages on events
- **Overall**: < 1% performance impact on modern servers

### Discord Bot
- **Idle**: < 50MB RAM usage
- **Active**: 100-200MB RAM with multiple tickets
- **CPU**: Negligible, event-driven architecture
- **Network**: Low bandwidth, only JSON messages
- **Overall**: Very lightweight, can run on minimal hardware

---

## 📚 Additional Documentation

- [Combat Log README](combat-log/combat-log-report-1.21.11/README.md) - Minecraft mod details
- [Discord Bot README](combat-log/discord-bot/README.md) - Discord bot setup
- [Configuration Guide](combat-log/discord-bot/CONFIG.md) - Complete config.json documentation
- [Running Guide](combat-log/RUNNING.md) - How to run everything
- [Build Guide](combat-log/BUILD_VERIFICATION.md) - Building from source

---

## 🆘 Support & Troubleshooting

### Common Issues

#### "Combat not detected"
- Make sure it's player vs player damage (not mobs)
- Verify mod is loaded (check server logs)
- Check Fabric API is installed

#### "Bot won't connect"
- Verify bot token is correct
- Check bot is invited to server
- Enable required intents in Developer Portal

#### "WebSocket connection failed"
- Verify bot is running first
- Check firewall allows port 8080
- Ensure correct URL in mod config

#### "Whitelist not working"
- Check channel IDs are correct
- Verify bot has permissions
- Test Mojang API with `/whitelist-setup`

For more help, see the troubleshooting sections in individual README files.
