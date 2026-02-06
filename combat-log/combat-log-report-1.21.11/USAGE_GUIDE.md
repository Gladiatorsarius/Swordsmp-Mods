# Combat Log Report - Player's Guide

## 🎮 For Players: What You Need to Know

### What is this mod?
This mod tracks when you disconnect during PvP combat and reports it to all players on the server. Combat loggers will have their inventory stored in a player head at their disconnect location.

### When am I "in combat"?
You're in combat when:
- ⚔️ You hit another player, OR
- 🛡️ Another player hits you

Both you and your opponent will be tagged in combat **for 15 seconds**.

### What happens when I enter combat?

```
You hit another player...
┌─────────────────────────────────────────────────┐
│ §c§lCOMBAT MODE                                 │
│     (displayed in action bar)                   │
└─────────────────────────────────────────────────┘
```

### How long does combat last?

**15 SECONDS** from the last hit.

- If you hit someone again, the timer resets to 15 seconds
- If someone hits you again, the timer resets to 15 seconds
- You must wait 15 full seconds with NO fighting to safely log out

### Combat Timer Countdown

You'll see a live countdown in your action bar:

```
§c§lCOMBAT 15s
§c§lCOMBAT 14s
§c§lCOMBAT 13s
...
§c§lCOMBAT 2s
§c§lCOMBAT 1s
✅ You are no longer in combat!
```

### Combat Ends on Death

If you die during combat:
- Your combat tag is immediately cleared
- Your opponent's combat tag is also cleared
- Normal death mechanics apply

## ⚠️ What Happens if I Log Out During Combat?

**YOUR INVENTORY WILL BE STORED IN A PLAYER HEAD!**

If you disconnect while in combat:
1. 📢 All players on the server are notified
2. 🕒 The message shows how much combat time was remaining
3. 💀 A player head is spawned at your disconnect location
4. 📦 Your inventory is stored (future feature - currently placeholder)
5. 🎫 A Discord ticket is created for admin review
6. 🚫 You are temporarily banned until the ticket is reviewed

### Example of Combat Logging Report:

```
You're losing a fight with Steve...
You disconnect...

📢 SERVER BROADCAST
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[Combat Log Report] YourName logged out during combat 
with 10.5 seconds remaining! Ticket will be created in Discord.

💀 A player head spawns at your location
📦 Contains your inventory (future feature)
🎫 Ticket created in Discord
🚫 You are banned until ticket is resolved
```

### Player Head Access Control

**Who can access the combat log head:**
- ⏰ **First 30 minutes:** Only combat opponents
- ⏰ **After 30 minutes:** Everyone can access
- 👑 **Server operators:** Can always access (future feature)

## ✅ How to Log Out Without Being Reported

### Option 1: Wait Out the Timer
```
1. Stop fighting
2. Run away if needed
3. Wait 15 seconds
4. When you see "You are no longer in combat!"
5. Now you can log out without being reported
```

### Option 2: Defeat Your Opponent
```
1. Win the fight
2. Wait 15 seconds after the last hit
3. Receive the "no longer in combat" message
4. Log out without report
```

### Option 3: Create Distance
```
1. Disengage from combat
2. Create distance between you and opponent
3. Wait for the 15 second timer
4. Log out when clear
```

## 🤔 Frequently Asked Questions

### What happens if I disconnect during combat?
A report message is broadcast to all players. You won't die or lose items, but everyone will know you combat logged.

### What if my game crashes during combat?
The mod can't tell the difference between a crash and intentionally disconnecting. A report will still be broadcast.

### What if someone keeps hitting me?
The timer resets every time either player attacks. You need 15 seconds of NO combat to be clear.

### Does this apply to PvE (fighting mobs)?
**No.** Only player vs player combat triggers the combat tag.

### What about accidental hits?
Any player damage counts. Even one accidental hit will tag both players for 15 seconds.

### Will I lose my items if I combat log?
**No.** This mod only reports the incident. You keep all your items and log out normally.

### Can server admins see the reports?
Yes, the reports are broadcast to all online players including admins.

## 💡 Pro Tips

### Tip 1: Check Your Status
Watch for the combat messages. If you see countdown warnings, you're still tagged.

### Tip 2: Plan Your Exits
If you need to log out, finish your fights first or avoid combat zones.

### Tip 3: Maintain Your Reputation
Being reported for combat logging may affect how other players view you.

### Tip 4: Communicate
If you need to leave urgently, try to communicate with your opponent. Some players may understand.

### Tip 5: Fight or Flight
When tagged in combat, decide quickly: fight to win or disengage and wait 15 seconds.

## 🎯 Summary

**Simple Rules:**
1. ⚔️ Fighting another player = 15 second combat tag
2. ⏱️ Timer resets with each hit
3. ⚠️ Logging out during combat = Public report to all players
4. ⏰ WAIT 15 seconds after fighting stops
5. ✅ No report when you get the clear message

**Remember:** This mod tracks behavior and promotes transparency, not punishment!

---

## 👑 For Server Operators

### Gamerule Commands

The mod provides custom gamerules for server configuration:

#### `bypass_combat_log_system`
Completely bypasses the combat log detection system.

**Usage:**
```mcfunction
/gamerule bypass_combat_log_system true   # Enable bypass
/gamerule bypass_combat_log_system false  # Disable bypass (default)
```

**When enabled (true):**
- ❌ No Discord incidents/tickets created
- ❌ No player heads spawned
- ❌ No temporary bans
- ❌ No punishments on login
- ✅ Items drop naturally like normal death
- ✅ Simple broadcast message when player logs during combat

**When disabled (false - default):**
- ✅ Full combat log system active
- ✅ Discord tickets created
- ✅ Player heads with inventory spawned
- ✅ Temporary bans until ticket resolved

**Use Cases:**
- Testing the server without triggering the full system
- Special events where combat logging is allowed
- Temporary bypass during server issues
- Situations where you want normal death mechanics

**Example Scenarios:**
```mcfunction
# During a special PvP event with no penalties
/gamerule bypass_combat_log_system true

# After the event, restore normal combat log enforcement
/gamerule bypass_combat_log_system false
```

**Note:** This is a server-side setting only. No client modification needed.
