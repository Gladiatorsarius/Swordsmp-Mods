# Whitelist System - Implementation Plan

## 🎯 Feature Overview

This document outlines the implementation plan for a Discord-based whitelist request system that allows players to request server access through Discord, with staff approval workflow, and automatic linking between Discord accounts and Minecraft UUIDs.

## 🌟 Key Features

- **Discord-Based Requests**: Players request whitelist through Discord button
- **Staff Review Workflow**: Private threads for staff to review requests
- **Mojang API Validation**: Verify Minecraft usernames are valid
- **Automatic Whitelisting**: Server executes whitelist command on approval
- **Player Linking**: Store Discord ID ↔ Minecraft UUID relationships
- **Replace DiscordSRV**: Custom linking system eliminates external dependency

## 📊 User Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    PLAYER JOINS DISCORD                          │
│                                                                  │
│  New player joins your Discord server                           │
│  Wants to play on Minecraft server                              │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                 GO TO #WHITELIST CHANNEL                         │
│                                                                  │
│  Channel shows info message with button                         │
│  Button: "🎫 Request Whitelist"                                 │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CLICK REQUEST BUTTON                          │
│                                                                  │
│  Modal pops up                                                  │
│  Title: "Whitelist Request"                                     │
│  Field: "Minecraft Username" (required)                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                  BOT VALIDATES USERNAME                          │
│                                                                  │
│  ✓ Check username format (3-16 chars, valid chars)             │
│  ✓ Query Mojang API for UUID                                   │
│  ✓ Check if already whitelisted/requested                      │
│  ✓ Check if Discord account already linked                     │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
                  ┌──────┴──────┐
                  │             │
         ┌────────▼─────┐  ┌────▼──────────────┐
         │ VALID        │  │ INVALID           │
         │ USERNAME     │  │ - Not found       │
         └────────┬─────┘  │ - Already linked  │
                  │        │ - Format error    │
                  │        └────┬──────────────┘
                  │             │
                  │             ▼
                  │        Error message sent
                  │        (try again)
                  │
                  ▼
┌─────────────────────────────────────────────────────────────────┐
│              CREATE STAFF REVIEW THREAD                          │
│                                                                  │
│  ✓ Private thread in staff-only channel                        │
│  ✓ Post request details:                                       │
│    - Discord user                                              │
│    - Minecraft username                                        │
│    - Minecraft UUID (from Mojang)                              │
│    - Request timestamp                                         │
│  ✓ Add approve/deny buttons                                   │
│  ✓ Tag @Staff role                                            │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
              ┌──────────┴──────────┐
              │                     │
     ┌────────▼──────┐     ┌───────▼──────────┐
     │ STAFF CLICKS  │     │ STAFF CLICKS     │
     │ APPROVE       │     │ DENY             │
     └────────┬──────┘     └───────┬──────────┘
              │                    │
              ▼                    ▼
    ┌─────────────────┐  ┌──────────────────┐
    │ APPROVAL FLOW   │  │ DENIAL FLOW      │
    │                 │  │                  │
    │ 1. Store link   │  │ 1. Close thread  │
    │    in database  │  │ 2. Send DM with  │
    │ 2. Send         │  │    reason        │
    │    whitelist    │  │ 3. Log denial    │
    │    command to   │  │                  │
    │    Minecraft    │  └──────────────────┘
    │ 3. Wait for     │
    │    confirmation │
    │ 4. Send DM      │
    │    to player    │
    │ 5. Update       │
    │    thread       │
    │ 6. Close thread │
    └─────────┬───────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  PLAYER CAN JOIN SERVER                          │
│                                                                  │
│  ✓ Whitelisted on Minecraft server                             │
│  ✓ Discord ↔ Minecraft link stored                             │
│  ✓ Can be used for combat log tickets                          │
│  ✓ Future features can use linking                             │
└─────────────────────────────────────────────────────────────────┘
```

## 🗂️ Technical Architecture

### Components Overview

```
┌──────────────────────────────────────────────────────────────┐
│                      DISCORD BOT                              │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ WhitelistManager                                   │    │
│  │ - Handle requests                                  │    │
│  │ - Validate usernames                               │    │
│  │ - Create review threads                            │    │
│  │ - Process approvals/denials                        │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ MojangAPIService                                   │    │
│  │ - Query username → UUID                            │    │
│  │ - Validate username format                         │    │
│  │ - Cache results                                    │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ LinkingDatabase (SQLite)                           │    │
│  │ - Store Discord ↔ Minecraft links                  │    │
│  │ - Query links for combat log tickets              │    │
│  │ - Track whitelist status                           │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ WebSocket Client                                   │    │
│  │ - Send whitelist commands                          │    │
│  │ - Send link updates                                │    │
│  │ - Receive confirmations                            │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
                            │
                            │ WebSocket
                            │ (Port 8080)
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    MINECRAFT SERVER                           │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ WhitelistCommandHandler                            │    │
│  │ - Receive whitelist commands                       │    │
│  │ - Execute /whitelist add                           │    │
│  │ - Send confirmation back                           │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ PlayerLinkingManager                               │    │
│  │ - Store links locally                              │    │
│  │ - Query Discord ID from UUID                       │    │
│  │ - Use for combat log tickets                       │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

## 📊 Database Schema

### Discord Bot Database (SQLite)

```sql
-- Store Discord ↔ Minecraft links
CREATE TABLE whitelist_links (
    discord_id VARCHAR(20) PRIMARY KEY,
    minecraft_uuid VARCHAR(36) NOT NULL UNIQUE,
    minecraft_name VARCHAR(16) NOT NULL,
    linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    whitelisted BOOLEAN DEFAULT true,
    linked_by VARCHAR(20),  -- Staff Discord ID who approved
    notes TEXT
);

CREATE INDEX idx_minecraft_uuid ON whitelist_links(minecraft_uuid);
CREATE INDEX idx_minecraft_name ON whitelist_links(minecraft_name);

-- Track whitelist requests
CREATE TABLE whitelist_requests (
    request_id VARCHAR(36) PRIMARY KEY,
    discord_id VARCHAR(20) NOT NULL,
    discord_username VARCHAR(100) NOT NULL,
    minecraft_name VARCHAR(16) NOT NULL,
    minecraft_uuid VARCHAR(36),
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, APPROVED, DENIED
    reviewed_by VARCHAR(20),  -- Staff Discord ID
    reviewed_at TIMESTAMP,
    reason TEXT,
    thread_id VARCHAR(20)
);

CREATE INDEX idx_request_status ON whitelist_requests(status);
CREATE INDEX idx_request_discord_id ON whitelist_requests(discord_id);
```

## 🗄️ File Structure

### Discord Bot (Java)

```
discord-bot/
├── src/main/java/combat/log/discord/
│   ├── whitelist/
│   │   ├── WhitelistManager.java           (NEW)
│   │   ├── WhitelistRequest.java           (NEW)
│   │   ├── WhitelistButtonHandler.java     (NEW)
│   │   └── WhitelistModalHandler.java      (NEW)
│   │
│   ├── api/
│   │   ├── MojangAPIService.java           (NEW)
│   │   └── MojangProfile.java              (NEW)
│   │
│   ├── database/
│   │   └── LinkingDatabase.java            (NEW)
│   │
│   └── models/
│       ├── WhitelistAddMessage.java        (NEW)
│       └── PlayerLinkMessage.java          (NEW)
│
└── database/
    └── whitelist.db                         (Runtime - SQLite)
```

### Minecraft Mod

```
combat-log-report-1.21.11/
└── src/main/java/combat/log/report/swordssmp/
    ├── linking/
    │   ├── PlayerLinkingManager.java       (NEW)
    │   └── PlayerLink.java                 (NEW)
    │
    ├── whitelist/
    │   └── WhitelistCommandHandler.java    (NEW)
    │
    └── socket/
        ├── WhitelistAddMessage.java        (NEW)
        └── PlayerLinkMessage.java          (NEW)
```

## 📡 WebSocket Message Protocol

### Whitelist Add Command (Discord → Minecraft)

```json
{
  "type": "whitelist_add",
  "timestamp": 1707139200000,
  "requestId": "uuid",
  "playerName": "PlayerName",
  "playerUuid": "player-uuid",
  "discordId": "123456789",
  "requestedBy": "staff-discord-id"
}
```

### Link Player (Discord → Minecraft)

```json
{
  "type": "link_player",
  "timestamp": 1707139200000,
  "discordId": "123456789",
  "playerUuid": "player-uuid",
  "playerName": "PlayerName",
  "whitelisted": true
}
```

### Whitelist Confirmation (Minecraft → Discord)

```json
{
  "type": "whitelist_confirmation",
  "timestamp": 1707139200000,
  "requestId": "uuid",
  "success": true,
  "playerName": "PlayerName",
  "error": null
}
```

## 🔧 Implementation Timeline

### Phase 1: Database Setup (30 minutes)
- [ ] Create SQLite database schema
- [ ] Write migration scripts
- [ ] Test database operations
- [ ] Add indexes for performance

### Phase 2: Mojang API Integration (1 hour)
- [ ] Create `MojangAPIService` class
- [ ] Implement username → UUID lookup
- [ ] Add response caching (5 min TTL)
- [ ] Handle API errors gracefully
- [ ] Test with various usernames

### Phase 3: Discord Bot - Request Flow (2 hours)
- [ ] Create whitelist channel setup command
- [ ] Add "Request Whitelist" button
- [ ] Create modal for username input
- [ ] Implement `WhitelistButtonHandler`
- [ ] Implement `WhitelistModalHandler`
- [ ] Validate username format
- [ ] Query Mojang API
- [ ] Check for duplicates

### Phase 4: Discord Bot - Review Flow (1.5 hours)
- [ ] Create `WhitelistManager` class
- [ ] Generate staff review threads
- [ ] Add approve/deny buttons
- [ ] Handle approve action:
  - Store link in database
  - Send whitelist command to Minecraft
  - DM player
  - Update thread
- [ ] Handle deny action:
  - Log reason
  - DM player
  - Close thread

### Phase 5: Minecraft Mod Integration (2 hours)
- [ ] Create `WhitelistCommandHandler`
- [ ] Parse whitelist_add messages
- [ ] Execute whitelist command
- [ ] Send confirmation back
- [ ] Create `PlayerLinkingManager`
- [ ] Store links locally
- [ ] Query methods for combat log system
- [ ] Update combat log to use new links

### Phase 6: Testing & Polish (1 hour)
- [ ] Test full flow end-to-end
- [ ] Test error scenarios
- [ ] Test duplicate requests
- [ ] Test invalid usernames
- [ ] Verify linking works
- [ ] Test combat log integration
- [ ] Documentation

**Total Estimated Time: 6-8 hours**

## ⚙️ Configuration

### Discord Bot Config

```json
{
  "whitelist": {
    "enabled": true,
    "whitelistChannelId": "channel-id-here",
    "reviewChannelId": "staff-channel-id-here",
    "staffRoleId": "staff-role-id-here",
    "buttonMessage": {
      "title": "🎫 Request Server Whitelist",
      "description": "Click the button below to request access to our Minecraft server",
      "color": "#00FF00"
    }
  },
  "mojangApi": {
    "enabled": true,
    "cacheDurationMinutes": 5,
    "timeout": 5000
  },
  "linking": {
    "databasePath": "./database/whitelist.db",
    "allowMultipleMinecraftAccounts": false,
    "allowMultipleDiscordAccounts": false
  }
}
```

### Minecraft Mod Config

```json
{
  "whitelist": {
    "enabled": true,
    "autoWhitelist": true,
    "storeLinkLocally": true,
    "linkDatabasePath": "./config/player-links.json"
  }
}
```

## 🧪 Testing Checklist

### Unit Tests
- [ ] Mojang API username validation
- [ ] Database CRUD operations
- [ ] Message serialization/deserialization
- [ ] Link lookup performance

### Integration Tests
- [ ] Discord button → modal flow
- [ ] Modal submission → validation
- [ ] Approval → whitelist command
- [ ] WebSocket message delivery
- [ ] Database persistence

### End-to-End Tests
- [ ] Player requests whitelist
- [ ] Staff reviews and approves
- [ ] Minecraft receives command
- [ ] Player can join server
- [ ] Link stored in both systems
- [ ] Combat log uses link

### Error Scenarios
- [ ] Invalid username format
- [ ] Username not found (Mojang)
- [ ] Already whitelisted
- [ ] Already requested
- [ ] Discord user already linked
- [ ] Minecraft account already linked
- [ ] WebSocket disconnected
- [ ] Database unavailable

## 🚀 Deployment Guide

### Step 1: Database Setup
```bash
# Create database directory
mkdir -p discord-bot/database

# Database will be auto-created on first run
# Or manually create with schema:
sqlite3 discord-bot/database/whitelist.db < schema.sql
```

### Step 2: Discord Bot Configuration
```bash
# Edit config.json
nano discord-bot/config.json

# Add whitelist channel IDs
# Add staff role ID
# Configure Mojang API settings
```

### Step 3: Discord Channel Setup
```
1. Create #whitelist channel
2. Set permissions (everyone can view, no one can send)
3. Run bot command: /whitelist-setup
4. Bot posts info message with button
5. Create staff review channel (staff-only)
```

### Step 4: Minecraft Mod Configuration
```bash
# Edit mod config
nano config/combat-log-report.json

# Enable whitelist feature
# Configure link database path
```

### Step 5: Start Both Systems
```bash
# Start Discord bot
cd discord-bot
java -jar combat-log-discord-bot-1.0.0.jar

# Minecraft server with mod already running
# Verify WebSocket connection in logs
```

### Step 6: Test
```
1. Go to #whitelist channel
2. Click "Request Whitelist" button
3. Enter Minecraft username
4. Check staff review channel for thread
5. Approve request
6. Verify player can join Minecraft
7. Check combat log ticket uses link
```

## 🔍 Monitoring & Maintenance

### Logs to Watch
- `[Whitelist] Request from {discord_user} for {minecraft_name}`
- `[Whitelist] Validated {minecraft_name} → {uuid}`
- `[Whitelist] Link stored: {discord_id} ↔ {minecraft_uuid}`
- `[Whitelist] Command sent to Minecraft: whitelist add {name}`
- `[Whitelist] Confirmed: {name} whitelisted`

### Common Issues
- **Mojang API down**: Requests will fail validation, show error to user
- **WebSocket disconnected**: Commands queued, sent on reconnect
- **Database locked**: Rare, use WAL mode in SQLite
- **Duplicate requests**: Prevented by checks, show error

### Maintenance Tasks
- Weekly: Review pending requests
- Monthly: Clean up old denied requests
- Quarterly: Database backup
- Verify Mojang API still works

## 📊 Statistics & Analytics

### Track These Metrics
- Total requests submitted
- Approval rate
- Average review time
- Denied request reasons
- Active links count
- Links used in combat log tickets

### Useful Queries
```sql
-- Approval rate
SELECT 
  status,
  COUNT(*) as count,
  ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM whitelist_requests), 2) as percentage
FROM whitelist_requests
GROUP BY status;

-- Average review time
SELECT 
  AVG(reviewed_at - requested_at) as avg_review_time_seconds
FROM whitelist_requests
WHERE status IN ('APPROVED', 'DENIED');

-- Most common denial reasons
SELECT 
  reason,
  COUNT(*) as count
FROM whitelist_requests
WHERE status = 'DENIED'
GROUP BY reason
ORDER BY count DESC
LIMIT 10;
```

## 🎯 Success Criteria

### Functional Requirements
- ✅ Players can request whitelist via Discord
- ✅ Staff can review in private threads
- ✅ Mojang API validates usernames
- ✅ Minecraft auto-whitelists on approval
- ✅ Links stored in database
- ✅ Combat log system uses links

### Non-Functional Requirements
- ✅ Request processed in < 5 seconds
- ✅ Mojang API response cached
- ✅ Database queries < 100ms
- ✅ WebSocket reliable with retry
- ✅ No data loss on restart
- ✅ Audit trail for all actions

## 📝 Future Enhancements

### Optional Features (Not in Initial Scope)
- [ ] Multiple Minecraft accounts per Discord
- [ ] Whitelist renewal system
- [ ] Automatic unlink on unwhitelist
- [ ] Link verification via in-game code
- [ ] Web dashboard for links
- [ ] API for external integrations
- [ ] Whitelist appeal process
- [ ] Temporary whitelist (time-limited)

## 🔗 Integration Points

### With Combat Log System
```java
// In TicketManager.java - modified
UUID discordId = PlayerLinkingManager.getInstance()
    .getDiscordId(incident.getPlayerUuid());

if (discordId != null) {
    User user = jda.retrieveUserById(discordId).complete();
    // Create private ticket
    // Tag user
    // Send DM
}
```

### With Other Systems
- **Permissions**: Could grant Discord roles based on Minecraft rank
- **Statistics**: Track player activity across both platforms
- **Verification**: Use links for other verification purposes
- **Events**: Notify Discord of in-game events

---

## Summary

This plan provides a complete, production-ready whitelist system that:
- ✅ Eliminates DiscordSRV dependency
- ✅ Provides better user experience
- ✅ Gives staff control
- ✅ Integrates seamlessly with combat log
- ✅ Is maintainable and extensible

**Status: 📋 Ready for implementation (6-8 hours of development)**
