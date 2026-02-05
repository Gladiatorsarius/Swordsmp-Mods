# Discord Combat Log Verification - Implementation Plan

## 🎯 Feature Overview

This document outlines the implementation plan for integrating Discord bot functionality with the Combat Log Report mod to create a verification system for combat logging incidents.

## 📊 System Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PLAYER COMBAT LOGS                          │
│                                                                     │
│  Player A attacks Player B → Both tagged in combat (15s)           │
│  Player B disconnects after 5 seconds                              │
└────────────────────────────┬───────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    MINECRAFT MOD DETECTION                          │
│                                                                     │
│  ✓ Detect disconnect during combat                                │
│  ✓ Log player UUID, name, timestamp, combat time remaining        │
│  ✓ Create incident record in database                             │
│  ✓ Add to pending punishment list                                 │
│  ✓ Broadcast in-game message (current behavior)                   │
└────────────────────────────┬───────────────────────────────────────┘
                             │
                             │ Send via HTTP/Webhook
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DISCORD BOT RECEIVES EVENT                      │
│                                                                     │
│  ✓ Parse incident data                                            │
│  ✓ Look up player's Discord account (if linked)                   │
│  ✓ Create ticket thread/channel                                   │
│  ✓ Post incident details with timestamp                           │
│  ✓ Start countdown timer (default: 60 minutes)                    │
│  ✓ Notify player + tag @Staff role                                │
└────────────────────────────┬───────────────────────────────────────┘
                             │
                             ▼
                    ┌────────┴────────┐
                    │                 │
          ┌─────────▼────────┐   ┌───▼──────────────────┐
          │ PLAYER SUBMITS   │   │ TIMER EXPIRES        │
          │ CLIP IN TIME     │   │ (NO SUBMISSION)      │
          └─────────┬────────┘   └───┬──────────────────┘
                    │                │
                    ▼                ▼
          ┌─────────────────┐   ┌────────────────────────┐
          │ ADMIN REVIEWS    │   │ AUTO-DENY              │
          │ - Watch clip     │   │ - Update DB status     │
          │ - Use /approve   │   │ - Confirm punishment   │
          │   or /deny       │   │ - Close ticket         │
          │ - Update DB      │   │ - Log incident         │
          └─────────┬────────┘   └───┬──────────────────┘
                    │                │
                    └────────┬───────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│               MINECRAFT MOD CHECKS PUNISHMENT STATUS                 │
│                                                                     │
│  When player logs in:                                              │
│  ✓ Query database for pending punishment                          │
│  ✓ Check status (APPROVED, DENIED, AUTO_DENIED, PENDING)          │
│                                                                     │
│  If APPROVED:                                                      │
│    → Clear punishment, send message "Appeal approved"              │
│                                                                     │
│  If DENIED or AUTO_DENIED:                                        │
│    → Kill player, send message with ticket reference              │
│    → Clear punishment from database                                │
│                                                                     │
│  If still PENDING:                                                 │
│    → Don't kill yet, send message about pending ticket            │
└─────────────────────────────────────────────────────────────────────┘
```

## 🗂️ File Structure

### Minecraft Mod
```
combat-log-report-1.21.11/
├── src/main/java/combat/log/report/swordssmp/
│   ├── CombatLogReport.java (modified)
│   ├── CombatManager.java (modified)
│   │
│   ├── incident/
│   │   ├── CombatLogIncident.java (NEW)
│   │   ├── IncidentStatus.java (NEW - enum)
│   │   └── IncidentManager.java (NEW)
│   │
│   ├── punishment/
│   │   ├── PendingPunishment.java (NEW)
│   │   ├── PunishmentManager.java (NEW)
│   │   └── PunishmentStatus.java (NEW - enum)
│   │
│   ├── discord/
│   │   ├── DiscordWebhookClient.java (NEW)
│   │   ├── DiscordMessage.java (NEW)
│   │   └── DiscordConfig.java (NEW)
│   │
│   ├── storage/
│   │   ├── DataStore.java (NEW - interface)
│   │   ├── JsonDataStore.java (NEW - implementation)
│   │   ├── SQLiteDataStore.java (NEW - optional)
│   │   └── DataModel.java (NEW)
│   │
│   ├── mixin/
│   │   ├── PlayerDisconnectMixin.java (modified)
│   │   ├── PlayerLoginMixin.java (NEW)
│   │   └── ServerTickMixin.java (existing)
│   │
│   └── config/
│       ├── ModConfig.java (NEW)
│       └── ConfigLoader.java (NEW)
│
├── src/main/resources/
│   ├── combat-log-report.mixins.json (modified)
│   ├── fabric.mod.json (modified)
│   └── config/
│       └── combat-log-report-config.json (NEW)
│
└── data/
    └── combat-logs/ (NEW - runtime data)
        ├── incidents.json
        └── pending-punishments.json
```

### Discord Bot (Separate Project)
```
discord-combat-log-bot/
├── src/
│   ├── bot.js (or bot.py, main.java)
│   ├── commands/
│   │   ├── approve.js
│   │   ├── deny.js
│   │   ├── extend.js
│   │   └── info.js
│   │
│   ├── handlers/
│   │   ├── ticketHandler.js
│   │   ├── clipHandler.js
│   │   └── webhookHandler.js
│   │
│   ├── services/
│   │   ├── databaseService.js
│   │   ├── minecraftService.js
│   │   └── timerService.js
│   │
│   └── utils/
│       ├── validators.js
│       └── logger.js
│
├── config/
│   └── config.json
│
└── database/
    └── combat-logs.db (or schema.sql)
```

## 📋 Detailed Class Descriptions

### CombatLogIncident.java
```java
public class CombatLogIncident {
    private UUID id;
    private UUID playerUuid;
    private String playerName;
    private long timestamp;
    private double combatTimeRemaining;
    private String discordTicketId;
    private IncidentStatus status;
    private String clipUrl;
    private long clipUploadTime;
    private String adminDecision;
    private UUID adminUuid;
    private long decisionTime;
    private String notes;
    
    // Constructor, getters, setters, builders
}
```

### IncidentStatus.java (Enum)
```java
public enum IncidentStatus {
    PENDING,           // Waiting for clip submission
    CLIP_UPLOADED,     // Clip submitted, awaiting admin review
    APPROVED,          // Admin approved, no punishment
    DENIED,           // Admin denied, punishment confirmed
    AUTO_DENIED,      // Timeout expired, auto-punishment
    EXPIRED           // Old incident, archived
}
```

### PunishmentManager.java
```java
public class PunishmentManager {
    // Singleton instance
    private Map<UUID, PendingPunishment> pendingPunishments;
    
    public void addPendingPunishment(UUID player, CombatLogIncident incident);
    public boolean hasPendingPunishment(UUID player);
    public PendingPunishment getPendingPunishment(UUID player);
    public void clearPunishment(UUID player);
    public void executePunishment(ServerPlayer player);
    public void updatePunishmentStatus(UUID player, IncidentStatus newStatus);
    
    // Persistence
    public void saveToFile();
    public void loadFromFile();
}
```

### DiscordWebhookClient.java
```java
public class DiscordWebhookClient {
    private final String webhookUrl;
    private final HttpClient httpClient;
    
    public CompletableFuture<Boolean> sendCombatLogIncident(CombatLogIncident incident);
    public CompletableFuture<IncidentStatus> checkIncidentStatus(UUID incidentId);
    
    // Error handling
    private void handleNetworkError(Exception e);
    private void retryWithBackoff(Runnable action);
}
```

## 🔐 Configuration Format

### combat-log-report-config.json
```json
{
  "discord": {
    "webhookUrl": "https://discord.com/api/webhooks/...",
    "botToken": "your-bot-token-here",
    "serverId": "123456789",
    "ticketChannelId": "987654321",
    "staffRoleId": "456789123",
    "notificationChannelId": "789123456"
  },
  "punishment": {
    "enabled": true,
    "autoKillOnTimeout": true,
    "timeoutMinutes": 60,
    "gracePeriodMinutes": 5,
    "allowMultipleOffenses": true,
    "maxOffensesBeforeBan": 3,
    "punishmentMessage": "You were killed for combat logging. Ticket: {ticketId}"
  },
  "clipRequirements": {
    "required": true,
    "acceptedPlatforms": ["youtube", "twitch", "streamable", "discord"],
    "minimumLengthSeconds": 10,
    "allowScreenshots": false
  },
  "storage": {
    "type": "json",
    "dataDirectory": "./data/combat-logs/",
    "backupEnabled": true,
    "backupIntervalHours": 24
  },
  "features": {
    "linkMinecraftToDiscord": true,
    "sendDmToPlayer": true,
    "logToConsole": true,
    "broadcastInGame": true
  }
}
```

## 🔄 Event Flow Timeline

### Timeline Example: 60-minute timeout
```
T+0:00  Player combat logs
        ├─ Mod detects disconnect
        ├─ Creates incident record
        ├─ Sends to Discord bot
        └─ Adds to pending punishment

T+0:05  Discord ticket created
        ├─ Player notified (if linked)
        ├─ Staff tagged
        └─ Timer starts (60 min)

T+0:10  [OPTION A] Player uploads clip
        ├─ Bot validates format
        ├─ Updates status to CLIP_UPLOADED
        ├─ Notifies staff for review
        └─ Awaits admin decision

T+0:30  [OPTION A continues] Admin reviews
        ├─ Watches clip
        ├─ Uses /approve or /deny
        ├─ Bot updates database
        └─ Mod notified of decision

T+1:00  [OPTION B] No clip uploaded
        ├─ Timer expires
        ├─ Bot marks AUTO_DENIED
        ├─ Updates punishment status
        └─ Closes ticket

T+2:00  Player logs back in
        ├─ Mod checks punishment DB
        ├─ If APPROVED: Clear, message sent
        ├─ If DENIED/AUTO_DENIED: Execute punishment
        └─ Clear from database
```

## 🎨 Discord Ticket Template

### Initial Ticket Message
```
🚨 Combat Log Incident Report

**Player:** {playerName} ({playerUuid})
**Time:** {timestamp}
**Combat Time Remaining:** {remainingTime} seconds
**Incident ID:** {incidentId}

**What happened:**
This player disconnected during active combat. They have **60 minutes** to submit video proof that this was not intentional combat logging.

**Required Action:**
@{playerDiscordMention} Please upload a clip showing:
- The moments before disconnect
- The reason for disconnect (crash, internet issue, etc.)
- Timestamp matching the incident

**Accepted proof formats:**
✅ YouTube link
✅ Twitch clip
✅ Streamable link  
✅ Direct video upload to Discord

**Admin Actions:**
`/approve {incidentId}` - Clear punishment
`/deny {incidentId}` - Confirm punishment
`/extend {incidentId} [minutes]` - Extend deadline
`/info {incidentId}` - View details

⏰ **Deadline:** {deadline} ({timeRemaining} remaining)
⚠️ **If no proof is submitted by deadline, punishment will be automatically applied on next login.**
```

## 🛠️ Admin Commands Reference

### Command Syntax
```
/approve <incident_id> [reason]
  - Clears punishment for the player
  - Optional reason is logged
  - Closes ticket with approval message
  
/deny <incident_id> [reason]
  - Confirms punishment will be applied
  - Reason is shown to player
  - Closes ticket with denial message
  
/extend <incident_id> <minutes>
  - Extends deadline by specified minutes
  - Updates timer in ticket
  - Notifies player of extension
  
/info <incident_id>
  - Shows full incident details
  - Displays clip if submitted
  - Shows admin action history
  
/history <player>
  - Shows all incidents for a player
  - Useful for repeat offenders
  - Can help inform decision
```

## 📊 Database Schema (Detailed)

### Incidents Table
```sql
CREATE TABLE incidents (
    id VARCHAR(36) PRIMARY KEY,
    player_uuid VARCHAR(36) NOT NULL,
    player_name VARCHAR(16) NOT NULL,
    minecraft_timestamp BIGINT NOT NULL,
    combat_time_remaining REAL NOT NULL,
    
    discord_ticket_id VARCHAR(50),
    discord_message_id VARCHAR(50),
    
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at BIGINT NOT NULL,
    deadline BIGINT NOT NULL,
    
    clip_url TEXT,
    clip_uploaded_at BIGINT,
    clip_validated BOOLEAN DEFAULT FALSE,
    
    admin_decision VARCHAR(20),
    admin_discord_id VARCHAR(50),
    admin_minecraft_uuid VARCHAR(36),
    decision_timestamp BIGINT,
    decision_reason TEXT,
    
    notes TEXT,
    metadata JSON
);

CREATE INDEX idx_player_uuid ON incidents(player_uuid);
CREATE INDEX idx_status ON incidents(status);
CREATE INDEX idx_deadline ON incidents(deadline);
```

### Punishments Table
```sql
CREATE TABLE punishments (
    player_uuid VARCHAR(36) PRIMARY KEY,
    incident_id VARCHAR(36) NOT NULL,
    should_execute BOOLEAN DEFAULT TRUE,
    execution_type VARCHAR(20) DEFAULT 'KILL',
    custom_message TEXT,
    created_at BIGINT NOT NULL,
    
    FOREIGN KEY (incident_id) REFERENCES incidents(id)
);
```

### Player Links Table (Optional)
```sql
CREATE TABLE player_links (
    minecraft_uuid VARCHAR(36) PRIMARY KEY,
    discord_id VARCHAR(50) NOT NULL UNIQUE,
    verified BOOLEAN DEFAULT FALSE,
    linked_at BIGINT NOT NULL
);

CREATE INDEX idx_discord_id ON player_links(discord_id);
```

## ⚠️ Error Handling & Edge Cases

### Network Failures
- **Discord bot offline**: Queue incidents locally, retry with exponential backoff
- **Webhook fails**: Log error, continue with in-game punishment, manual ticket creation
- **Database unavailable**: Fallback to file-based storage, sync when available

### Server Crashes
- **Mid-punishment**: Load pending punishments on startup
- **During timer**: Recalculate deadline based on stored timestamp
- **Lost connection**: Reconnect and sync state

### Player Edge Cases
- **Player banned before login**: Clear punishment, log incident
- **Player never returns**: Archive incident after 7 days
- **Multiple rapid combat logs**: Group into single ticket with multiple incidents
- **Player changes name**: Track by UUID, update display name

### Admin Edge Cases
- **Conflicting decisions**: Last decision wins, log all attempts
- **Admin abuse**: Log all actions with timestamps for audit
- **No admins online**: Auto-deny after timeout, can be appealed later

## 🚀 Deployment Checklist

### Pre-deployment
- [ ] Test Discord bot on dev server
- [ ] Test Minecraft mod on test server
- [ ] Verify database schema
- [ ] Configure webhooks and tokens
- [ ] Set up backup system
- [ ] Prepare rollback plan

### Deployment
- [ ] Deploy database
- [ ] Deploy Discord bot
- [ ] Update Minecraft mod
- [ ] Configure mod settings
- [ ] Test end-to-end flow
- [ ] Monitor logs for 24h

### Post-deployment
- [ ] Train staff on commands
- [ ] Document player appeal process
- [ ] Set up monitoring alerts
- [ ] Schedule first backup
- [ ] Gather feedback

## 📝 Questions Checklist

Before implementation, please provide answers to:

**Critical (Required):**
- [ ] Discord bot language preference (Java/Python/Node.js)
- [ ] Ticket system type (Forums/Threads/Channels)
- [ ] Database type (SQLite/MySQL/JSON)
- [ ] Default timeout duration in minutes
- [ ] Player-Discord linking method

**Important (Recommended):**
- [ ] Accepted clip platforms
- [ ] Admin role permissions structure
- [ ] Multi-offense handling
- [ ] Grace period duration
- [ ] Communication method (Webhook/API/Database)

**Optional (Nice to have):**
- [ ] Custom punishment messages
- [ ] Appeal process after auto-deny
- [ ] Statistics dashboard
- [ ] Integration with existing systems

---

**Next Step:** Once you answer these questions, I'll create the detailed implementation with code structure!
