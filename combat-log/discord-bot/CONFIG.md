# Discord Bot Configuration Guide

This guide explains every field in the `config.json` file for the Combat Log Discord Bot.

## 📋 Quick Start

1. Copy `config.example.json` to `config.json`
2. Fill in IDs and settings in `config.json`
3. Create `config.local.json` with your bot token (this file is not committed)
4. Configure channel IDs for tickets and whitelist
5. Adjust features and timeouts as needed
6. Run the bot with `java -jar combat-log-discord-bot-1.0.0.jar`

## 🔧 Configuration Structure

The configuration is organized into logical sections:

### 1. Discord Credentials (Required)
These are the essential Discord settings needed to connect the bot.

### 2. WebSocket Settings
Configuration for the WebSocket server that communicates with Minecraft.

### 3. Feature Toggles
Boolean flags to enable/disable specific bot features.

### 4. Timeout Settings
Time-based configuration for various bot operations.

### 5. Channel IDs
Discord channel identifiers for where the bot operates.

### 6. Additional Settings
Platform-specific and advanced configuration options.

---

## 📝 Detailed Field Reference

### Discord Settings

#### `discord.token` (Required)
- **Type**: String
- **Example**: `"YOUR_BOT_TOKEN_HERE"` (Replace with your actual bot token)
- **Description**: Your Discord bot token from the [Discord Developer Portal](https://discord.com/developers/applications)
- **How to Get**:
  1. Go to Discord Developer Portal
  2. Create a new application or select existing
  3. Go to "Bot" section
  4. Click "Reset Token" or "Copy" to get your bot token
- **Security**: ⚠️ **Never share your bot token publicly!** It provides full control over your bot.

#### Local override file (Recommended)
To keep tokens out of git, use a local override file. The bot loads `config.json` first, then applies `config.local.json` on top.

Create `config.local.json` next to `config.json`:

```json
{
  "discord": {
    "token": "YOUR_BOT_TOKEN_HERE"
  }
}
```

#### `discord.guildId` (Required)
- **Type**: String (Snowflake ID)
- **Example**: `"123456789012345678"`
- **Description**: The ID of your Discord server where the bot will operate
- **How to Get**:
  1. Enable Developer Mode in Discord (Settings → Advanced → Developer Mode)
  2. Right-click your server icon
  3. Click "Copy Server ID"

#### `discord.staffRoleId` (Required)
- **Type**: String (Snowflake ID)
- **Example**: `"987654321098765432"`
- **Description**: The role ID for staff members who can manage combat log tickets
- **How to Get**:
  1. Enable Developer Mode in Discord
  2. Go to Server Settings → Roles
  3. Right-click the staff role
  4. Click "Copy Role ID"
- **Permissions**: Members with this role can use `/approve`, `/deny`, `/extend`, and `/info` commands

---

### WebSocket Settings

#### `websocket.port`
- **Type**: Integer
- **Default**: `8080`
- **Example**: `8080`
- **Description**: The port number for the WebSocket server that receives combat log incidents from Minecraft
- **Note**: Make sure this port is not blocked by firewall and matches the Minecraft mod configuration

#### `websocket.host`
- **Type**: String
- **Default**: `"0.0.0.0"`
- **Example**: `"0.0.0.0"` or `"127.0.0.1"`
- **Description**: The host address to bind the WebSocket server
  - `"0.0.0.0"` - Listen on all network interfaces (recommended for servers)
  - `"127.0.0.1"` - Listen only on localhost (for testing or same-machine setup)

---

### Feature Toggles

#### `features.useForumChannel`
- **Type**: Boolean
- **Default**: `true`
- **Description**: Whether to use Forum channels for tickets instead of text channels with threads
  - `true` - Creates Forum posts for each ticket (recommended, cleaner organization)
  - `false` - Creates threads in a text channel
- **Requirements**: If `true`, `channels.ticketChannelId` must be a Forum channel

#### `features.autoDenyEnabled`
- **Type**: Boolean
- **Default**: `true`
- **Description**: Automatically deny combat log appeals if no proof is submitted within the timeout period
  - `true` - Tickets are auto-denied after `timeouts.ticketTimeoutMinutes`
  - `false` - Tickets remain open indefinitely until staff manually approves/denies

#### `features.privateThreads`
- **Type**: Boolean
- **Default**: `true`
- **Description**: Whether to create private threads for linked players
  - `true` - Only the player and staff can see the thread
  - `false` - Thread is public to all server members
- **Note**: Requires player to be linked via the whitelist system

#### `features.whitelistEnabled`
- **Type**: Boolean
- **Default**: `true`
- **Description**: Enable the whitelist system
  - `true` - Players can request whitelist through Discord
  - `false` - Whitelist system is disabled
- **Requirements**: If enabled, requires `channels.whitelistChannelId` to be set

#### `features.mojangApiEnabled`
- **Type**: Boolean
- **Default**: `true`
- **Description**: Enable Mojang API integration for validating Minecraft usernames
  - `true` - Validates usernames and gets UUIDs from Mojang API
  - `false` - Skips validation (not recommended)

---

### Timeout Settings

#### `timeouts.ticketTimeoutMinutes`
- **Type**: Integer (minutes)
- **Default**: `60`
- **Example**: `60` (1 hour)
- **Description**: How long players have to submit proof before their ticket is auto-denied (if `features.autoDenyEnabled` is `true`)
- **Recommended Values**:
  - `30` - Fast-paced servers with strict rules
  - `60` - Standard (1 hour)
  - `120` - More lenient (2 hours)

#### `timeouts.mojangCacheDurationMinutes`
- **Type**: Integer (minutes)
- **Default**: `5`
- **Example**: `5`
- **Description**: How long to cache Mojang API responses to reduce API calls
- **Note**: Mojang API has rate limits, so caching is important

#### `timeouts.mojangApiTimeoutSeconds`
- **Type**: Integer (seconds)
- **Default**: `5`
- **Example**: `5`
- **Description**: Maximum time to wait for Mojang API responses before timing out
- **Recommended**: Keep at 5 seconds to avoid long waits if Mojang API is slow

---

### Channel IDs

#### `channels.ticketChannelId` (Required)
- **Type**: String (Snowflake ID)
- **Example**: `"111222333444555666"`
- **Description**: The channel where combat log tickets are created
- **Requirements**:
  - Must be a Forum channel if `features.useForumChannel` is `true`
  - Must be a Text channel if `features.useForumChannel` is `false`
- **How to Get**:
  1. Enable Developer Mode in Discord
  2. Right-click the channel
  3. Click "Copy Channel ID"

#### `channels.whitelistChannelId` (Required if whitelist enabled)
- **Type**: String (Snowflake ID)
- **Example**: `"777888999000111222"`
- **Description**: The channel where players can request whitelist access
- **Note**: The bot will post a button message here with `/whitelist-setup` command
- **Required**: Only if `features.whitelistEnabled` is `true`

#### `channels.reviewChannelId` (Required if whitelist enabled)
- **Type**: String (Snowflake ID)
- **Example**: `"333444555666777888"`
- **Description**: The channel where whitelist approvals are logged (for staff tracking)
- **Note**: The bot posts confirmation messages here when players are whitelisted
- **Required**: Only if `features.whitelistEnabled` is `true`

---

### Ticket Settings

#### `ticket.acceptedProofPlatforms`
- **Type**: Array of Strings
- **Default**: `["youtube.com", "youtu.be", "twitch.tv", "clips.twitch.tv", "streamable.com", "medal.tv", "discord.com/attachments"]`
- **Description**: List of platforms accepted as valid proof for combat log appeals
- **Behavior**: When a player posts a URL containing any of these strings, it's marked as proof submitted
- **Customization**: Add or remove platforms based on your server's policy
- **Examples**:
  ```json
  ["youtube.com", "youtu.be"]  // Only YouTube
  ["twitch.tv", "clips.twitch.tv", "medal.tv"]  // Only streaming platforms
  ```

---

### Whitelist Settings

#### `whitelist.buttonMessage.title`
- **Type**: String
- **Default**: `"🎫 Request Server Whitelist"`
- **Description**: The title shown in the whitelist request embed message
- **Customization**: Change to match your server's branding

#### `whitelist.buttonMessage.description`
- **Type**: String
- **Default**: `"Click the button below to request access to our Minecraft server"`
- **Description**: The description shown in the whitelist request embed message
- **Customization**: Add instructions or requirements for your server

#### `whitelist.buttonMessage.color`
- **Type**: String (Hex color)
- **Default**: `"#00FF00"` (Green)
- **Example**: `"#FF0000"` (Red), `"#0099FF"` (Blue)
- **Description**: The color of the embed message border
- **Format**: Must be a hex color code starting with `#`

---

### Button Labels

#### `buttons.ticket.approve`
- **Type**: String
- **Default**: `"✅ Approve"`
- **Description**: Label for the ticket approve button

#### `buttons.ticket.deny`
- **Type**: String
- **Default**: `"❌ Deny"`
- **Description**: Label for the ticket deny button

#### `buttons.ticket.admit`
- **Type**: String
- **Default**: `"🔴 I Admit Combat Log"`
- **Description**: Label for the ticket self-admit button

#### `buttons.ticket.extend`
- **Type**: String
- **Default**: `"⏰ Extend"`
- **Description**: Label for the ticket extend button

#### `buttons.whitelist.request`
- **Type**: String
- **Default**: `"🎫 Request Whitelist"`
- **Description**: Label for the whitelist request button

#### `buttons.whitelist.approve`
- **Type**: String
- **Default**: `"✅ Approve"`
- **Description**: Label for the whitelist approve button

#### `buttons.whitelist.deny`
- **Type**: String
- **Default**: `"❌ Deny"`
- **Description**: Label for the whitelist deny button

---


### Scenario 1: Basic Setup
Minimal configuration for a new server:

```json
{
  "discord": {
    "guildId": "YOUR_SERVER_ID",
    "staffRoleId": "YOUR_MODERATOR_ROLE_ID"
  },
  "channels": {
    "ticketChannelId": "YOUR_FORUM_CHANNEL_ID",
    "whitelistChannelId": "YOUR_WHITELIST_CHANNEL_ID",
    "reviewChannelId": "YOUR_STAFF_CHANNEL_ID"
  },
  "features": {
    "useForumChannel": true,
    "autoDenyEnabled": true,
    "privateThreads": true,
    "whitelistEnabled": true,
    "mojangApiEnabled": true
  }
}
```

Token goes in `config.local.json`:

```json
{
  "discord": {
    "token": "YOUR_BOT_TOKEN"
  }
}
```

### Scenario 2: No Whitelist
If you don't want the whitelist feature:

```json
{
  "features": {
    "whitelistEnabled": false
  }
}
```

### Scenario 3: Strict Combat Log Policy
Fast timeout with limited platforms:

```json
{
  "timeouts": {
    "ticketTimeoutMinutes": 30
  },
  "ticket": {
    "acceptedProofPlatforms": ["youtube.com", "youtu.be"]
  }
}
```

### Scenario 4: Local Testing
Testing on the same machine as Minecraft server:

```json
{
  "websocket": {
    "port": 8080,
    "host": "127.0.0.1"
  }
}
```

---

## ⚠️ Important Security Notes

1. **Do not commit your bot token** - Keep it in `config.local.json`
2. **Use environment variables** for sensitive data in production
3. **Restrict bot permissions** - Only give necessary Discord permissions
4. **Keep backups** - Save your configuration before making changes
5. **Use strong passwords** - If using MySQL for DiscordSRV

---

## 🔍 Troubleshooting

### "Bot token not configured" Error
- Make sure `discord.token` is set in `config.local.json` and not the default placeholder value

### "Cannot find guild" Error
- Verify `discord.guildId` is correct
- Make sure bot is invited to the server

### "Channel not found" Error
- Double-check all channel IDs with Copy Channel ID
- Ensure bot has access to all configured channels

### WebSocket Connection Fails
- Check firewall settings for the configured port
- Verify `websocket.port` matches Minecraft mod config
- Try `"host": "0.0.0.0"` instead of localhost

### Mojang API Errors
- Check your internet connection
- Try increasing `timeouts.mojangApiTimeoutSeconds`
- If persistent, set `features.mojangApiEnabled` to `false` (not recommended)

---

## 📚 Related Documentation

- [Discord Bot README](README.md) - General bot information
- [Running Guide](../RUNNING.md) - How to run the bot and mod
- [Discord Developer Portal](https://discord.com/developers/docs) - Discord API documentation

---

## 🆘 Need Help?

If you're stuck with configuration:
1. Check the example configuration files
2. Review the troubleshooting section above
3. Check bot logs for specific error messages
4. Verify all IDs are correct (common mistake)
5. Make sure all required channels exist and bot has access
