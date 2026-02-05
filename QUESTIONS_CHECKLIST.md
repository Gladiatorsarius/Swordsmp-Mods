# Quick Questions Checklist

**Status:** ⏸️ Awaiting answers before implementation

## 🔴 CRITICAL (Must Answer - Required for Implementation)

### 1. Discord Bot Language?
Choose one:
- [ ] **Python (discord.py)** - Recommended for speed
  - Pros: Fast development, easy to learn, great libraries
  - Cons: Different language from Minecraft mod
  
- [ ] **JavaScript/Node.js (discord.js)** - Modern choice
  - Pros: Popular, async by default, good ecosystem
  - Cons: Can be complex for beginners
  
- [ ] **Java (JDA)** - Same as Minecraft
  - Pros: Type-safe, same language as mod, good IDE support
  - Cons: More verbose, slower development
  
- [ ] **Other**: _____________

**Your choice:** __________

---

### 2. Bot Hosting Location?
- [ ] **Same server as Minecraft** (simplest start)
- [ ] **Different local server** (specify)
- [ ] **Cloud hosting** (Heroku/Railway/AWS/etc.)
- [ ] **Not sure yet** (we can decide later)

**Your choice:** __________

---

### 3. Database Type?
- [ ] **SQLite** - Recommended for single server
  - Simple file-based database
  - Both Minecraft and bot access same file
  - Easy backup (copy file)
  
- [ ] **MySQL/PostgreSQL** - For production/multiple servers
  - Proper database server
  - Better for multiple servers
  - More robust
  
- [ ] **JSON files** - Simplest but least robust
  - Just text files
  - Easy to read/edit manually
  - Not recommended for production

**Your choice:** __________

---

### 4. Ticket System in Discord?
- [ ] **Forum Channels** - Recommended
  - Native Discord feature
  - Organized by default
  - Best for browsing history
  
- [ ] **Private Threads** - Good alternative
  - One main channel with threads
  - Clean main channel view
  - Threads auto-archive
  
- [ ] **Separate Channels** - Traditional
  - Each ticket = new channel
  - Can be messy with many tickets
  - Clear separation

**Your choice:** __________

---

### 5. Player-Discord Linking?
How do players link their Minecraft account to Discord?

- [ ] **Manual verification** (player runs /verify command)
- [ ] **Automatic** (via website or existing system)
- [ ] **Use existing plugin** (like DiscordSRV)
- [ ] **No linking** (just use player names, less reliable)
- [ ] **Not sure** (we can implement basic and improve later)

**Your choice:** __________

---

## 🟡 IMPORTANT (Recommended to Answer)

### 6. Default Timeout Duration?
How long does a player have to submit proof before auto-denial?

- Recommended: **60 minutes** (1 hour)
- Your preference: _______ minutes

---

### 7. Accepted Proof Formats?
Check all that apply:
- [ ] YouTube links
- [ ] Twitch clips
- [ ] Direct Discord video upload
- [ ] Streamable links
- [ ] Other platforms: __________

---

### 8. Admin Permission Structure?
- [ ] **Single admin role** (simple)
- [ ] **Multiple levels** (mods can extend, admins can approve/deny)
- [ ] **Use existing Discord roles** (specify which)

---

### 9. Multiple Offenses?
- [ ] **Track offense history**
- [ ] **Escalating punishments** (1st = warning, 2nd = penalty, 3rd = ban)
- [ ] **Each incident separate** (no history tracking)

---

### 10. Grace Period?
Should there be a warning before execution?
- [ ] **No grace period** (immediate on next login if auto-denied)
- [ ] **Give warning** (notify but don't punish until second login)
- [ ] **X minutes** after login: _____ minutes

---

## 🟢 OPTIONAL (Can Decide Later)

### 11. Communication Method?
How should Minecraft mod and Discord bot communicate?
- [ ] **HTTP Webhooks** (recommended - simple)
- [ ] **REST API** (bot provides API)
- [ ] **Shared Database only** (no direct communication)
- [ ] **Let you decide** (choose best option)

---

### 12. Fallback Behavior?
What happens if Discord bot is offline?
- [ ] **Queue incidents** (send when bot comes back)
- [ ] **Log only** (no ticket created)
- [ ] **Skip Discord** (just use old in-game behavior)

---

### 13. Appeal Process?
Can players appeal after auto-denial?
- [ ] **Yes** (create new ticket system for appeals)
- [ ] **No** (decision is final)
- [ ] **Manual only** (contact admin directly)

---

### 14. Statistics/Dashboard?
- [ ] **Yes, show incident statistics**
- [ ] **No, keep it simple**
- [ ] **Maybe later**

---

### 15. Multiple Minecraft Servers?
Will this bot serve multiple Minecraft servers?
- [ ] **Yes** (plan for multi-server from start)
- [ ] **No** (single server only)
- [ ] **Maybe in future** (keep option open)

---

## 📝 Additional Notes/Questions

(Write any additional requirements, concerns, or questions here)

```
Your notes:




```

---

## ✅ When You're Ready

Once you've answered the critical questions (1-5), reply with:
1. Your answers
2. Any modifications to the plan
3. Approval to start implementation

Then I'll begin building!

---

**Recommendation Summary:**
- Python bot (fastest to build)
- SQLite database (simple, works well)
- Discord Forum Channels (best organization)
- Same server hosting (easiest to start)
- 60-minute timeout (balanced)
- Manual player linking (simple start)

This gives you a working system quickly, with room to expand later!
