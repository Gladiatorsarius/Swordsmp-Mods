# Swords SMP 1.0.12 - detailed bug reports

Branch simplify-swordsmp. 26 commits took main @ c157794 (version 1.0.7) to 1.0.11 (commit f5de153, the previous candidate - prepared and reviewed, but its build was never shipped); nine further bug fixes on top (Section E) bring the branch to 1.0.12. This document describes every bug fixed along the way: what was broken, the root cause, how to reproduce it on your current 1.0.7 build, what the fix does, and exactly how it was verified.

The bugs split into groups. Section A covers bugs that already existed in 1.0.7 and were fixed in 1.0.11. Section B covers mistakes the cleanup itself introduced, which were caught during the two independent review rounds and fixed while that candidate was being prepared - listed separately so you can see what the review process was for. Section C lists the pure cleanup commits, Section D two suspected bugs that turned out not to be bugs, Section E the bugs found and fixed after that candidate was prepared (all of them also present in 1.0.7), and Section F the exact state of testing.

# Section A - Bugs that existed in 1.0.7

### A1. TNT Rod cast everything twice

- Broken: one right-click with the TNT Rod summoned 10 TNT over about 2.5 seconds and drained 1000 durability. Intended: 5 TNT and 500 durability.
- Cause: the ability was wired up twice - registered on Fabric's UseItemCallback and also called from the rod item's own use() method. Both fire on every right-click, so the whole cast ran twice. A second defect in the same procedure: the delayed durability cost re-read your main hand when each timer fired, so if you switched items within the 10-50 tick window, the damage landed on whatever you were holding instead of the rod.
- Reproduce on 1.0.7: hold the TNT Rod, right-click once, count the TNT spawns (10) and watch the durability drop (1000). For the second part: right-click, then immediately switch to another item - that item takes the later durability hits.
- Fix: the callback registration is removed; the item's use() is the single cast path. The rod's ItemStack is captured at click time so the delayed cost always lands on the rod. Commit ea0b290.
- Verified: code-path analysis (both registration paths confirmed in source; single path after). Not exercised in a running game.

### A2. Smelter Pickaxe dropped everything twice

- Broken: breaking a block with the Smelter Pickaxe produced the drops twice. Normal blocks dropped two copies; smeltable ores dropped the smelted result AND the raw drops (iron ore gave an iron ingot plus raw iron).
- Cause: the mod handled the break inside Fabric's BEFORE-break event but returned true, which tells vanilla to continue its own break handling. Vanilla then dropped the same block's loot a second time.
- Reproduce on 1.0.7: mine stone or iron ore with the Smelter Pickaxe and watch the doubled drops.
- Fix: the final implementation intercepts the exact call vanilla uses to drop a broken block's loot (Block.dropResources) at the moment of this specific break, only when the Smelter Pickaxe is the tool, the block has no inventory, and the block item has a smelting recipe. Normal drops are replaced by the same output the mod always gave: one smelted result per block, Fortune and Silk Touch ignored. Durability loss, ore XP and mined statistics run through vanilla's normal path untouched, and container contents are never touched. One deliberate change from the old code: it used to force loot out of blocks even when the tool could not really harvest them; now normal harvest rules apply (in practice irrelevant - it is a netherite-tier pickaxe). Commits dfc7b5e, d7d9367, 25b7812, 6b49f03, f5de153.
- Verified: runtime. An automated test inside a real dedicated server drove actual block breaks: iron ore gave exactly 1 iron ingot with normal tool wear, clay gave 1 terracotta, diamond ore gave 1 diamond plus XP, a chest kept its full contents, plain blocks dropped normally, container spill was untouched. 6 of 6 checks passed. Still owed: one run with a Fortune or Silk Touch enchanted Smelter.

### A3. The Abomination gave Creative to the wrong player

- Broken: the Abomination's ability lends Creative mode for 60 seconds. Hitting another player ran the gamemode command at the victim's position - so the player you hit got Creative, not you. Triggering it on a block break gave Creative to whichever player stood nearest the block. Worse, the cleanup ran gamemode survival @p 60 seconds later, which demoted even players who were legitimately in Creative.
- Cause: the item passes the hit entity into the procedure, but the procedure ran /gamemode creative @p at that position - @p picks the nearest player to the wrong anchor, and the later demote did not check what mode the player had before.
- Reproduce on 1.0.7: hit another player with the Abomination item and check their gamemode - they turn Creative, you do not. Wait 60 seconds near a player who was already Creative: they get forced to Survival.
- Fix: uses ServerPlayer.setGameMode on the actual user, remembers that player's previous game mode, restores exactly that mode after 60 seconds, and skips players who were already in Creative. Commit 47036e5.
- Verified: static analysis of both the broken and fixed targeting. Not exercised in a running game (needs two players to reproduce the worst case).

### A4. The Abomination's Creative became permanent on disconnect

- Broken: if you disconnected during the 60-second Creative window, you kept Creative forever - the restore never landed.
- Cause: the queued restore captured the ServerPlayer object itself. When you disconnect, that object is dead; the timer later fired on the stale instance and did nothing, leaving your saved gamemode as Creative.
- Reproduce on 1.0.7: trigger the ability, disconnect within 60 seconds, rejoin - you are still Creative, permanently.
- Fix: pending restores are tracked by player UUID instead. The timer only restores a player who is still online; otherwise the entry stays, and a join handler restores you when you come back. Triggering the ability again while borrowed Creative is active is a no-op, so your original mode is never overwritten. Commit 50b8561.
- Verified: static analysis. Not exercised in a running game.

### A5. The Abomination's Creative survived a server restart

- Broken: a server restart inside (or before the end of) the 60-second window lost the pending restore and stranded the player in Creative permanently.
- Cause: the pending-restore map from A4 lived in memory only; a restart wiped it.
- Reproduce on 1.0.7 (with A4 hypothetically patched): trigger the ability, restart the server within 60 seconds, rejoin - Creative stays.
- Fix: pending restores are stored in world saved data (swordssmp_abomination_pending_restores) with a proper codec, so they survive restarts. The timer restores an online player; the join handler restores anyone whose window spanned a restart. Relogging ends the borrowed window early - the fail-safe direction. Commit ee036d5.
- Verified: static analysis plus a clean world save/stop on the dedicated-server boot test. The full borrow-restart-restore cycle is owed an in-game pass.

### A6. Earth Wave's innermost ring never spawned, and the queue leaked

- Broken: the innermost display ring of Earth Wave never appeared, and every use of the ability left one dead entry behind in the mod's work queue forever.
- Cause: the queue loop decrements each task's delay and runs it only when the counter hits exactly 0. A task queued with delay 0 went straight to -1 on the first tick: it never ran and was never removed. Earth Wave's innermost ring is the one queued with delay 0.
- Reproduce on 1.0.7: cast Earth Wave and compare the rings to the intended pattern - the center ring is missing. (The leak is invisible but grows by one entry per cast.)
- Fix: the loop runs tasks at delay <= 0, so zero-delay tasks execute on the next tick and are removed. All positive-delay callers behave exactly as before. Commit a0c03c3.
- Verified: static analysis of the loop and all callers. Not exercised in a running game.

### A7. Earth Wave cleanup deleted unrelated block displays

- Broken: Earth Wave's 30-tick cleanup pass removed every block-display entity in the area - including displays spawned by other players, by other Earth Wave casts nearby, or by other mods.
- Cause: the cleanup discarded every Display.BlockDisplay inside the area box with no ownership check.
- Reproduce on 1.0.7: have another player or mod place a block display near an Earth Wave cast - it vanishes when the wave's cleanup runs.
- Fix: displays spawned by Earth Wave carry a swordssmp_earthwave tag and the cleanup only removes tagged displays. Commit 7dadf93.
- Verified: static analysis. Not exercised in a running game.

### A8. Two simultaneous Earth Wave casts deleted each other's rings early

- Broken: when two players cast Earth Wave at the same time in the same area, each cast's cleanup removed the other cast's rings before their time.
- Cause: after A7's fix the cleanup filtered on the shared swordssmp_earthwave tag - but that tag is identical for every cast, so one cast's timer matched the other's displays.
- Reproduce on 1.0.7 (with A7 applied): two players cast Earth Wave next to each other; the first cast's cleanup cuts the second cast's rings short.
- Fix: each cast adds a unique per-cast tag (swordssmp_earthwave_cast_N) alongside the shared one, and cleanup filters on the unique tag. Commit f5ccb44.
- Verified: static analysis. Not exercised in a running game.

### A9. Ancient Void Relic: new owner inherited the old owner's cooldown

- Broken: when the relic changed owner, the new owner silently started with the previous owner's remaining cooldown.
- Cause: the owner-reset removed an NBT key called VoidRelicCooldownUntil that is never written anywhere. The actual cooldown lives under VoidRelicCooldownRemaining, which was never cleared.
- Reproduce on 1.0.7: use the relic, hand it to another player before the cooldown ends - they cannot use it until your leftover cooldown expires.
- Fix: the reset removes the real key. Commit 3c2d071.
- Verified: static analysis (both procedures, both NBT keys traced). Not exercised in a running game.

### A10. Chorus Slayer ability ran through two paths per click

- Broken: the Chorus Slayer's ability was invoked twice per right-click; the second invocation was always blocked by the cooldown the first one had just set, so the visible effect was correct but the code ran a redundant, confusing path.
- Cause: same double-wiring pattern as the TNT Rod (A1) - registered on UseItemCallback and called from the item's use(). Here the second call was harmless because the native cooldown rejected it.
- Reproduce on 1.0.7: not visible in game; the doubled invocation is only observable in code (the cooldown masked it).
- Fix: the item's use() is the single path, same as the TNT Rod fix. Commit 1c0acd5.
- Verified: code-path analysis. No behavior change expected or observed in testing scope.

### A11. Dropping an item mid-use desynced the server

- Broken: dropping your food or weapon while actively using it (eating, drawing a bow) left the server thinking you were still using it - a server/client desync.
- Cause: the mod shipped a mixin (ServerPlayerMixin) that was a near-verbatim copy of vanilla 1.21.11's ServerPlayer.drop() - except it omitted the stopUsingItem() call vanilla makes when the active use-item becomes empty. The mixin existed for no functional reason and only introduced that one-line difference.
- Reproduce on 1.0.7: start eating, press Q to drop the food mid-bite - the server still considers you eating until something else clears it.
- Fix: the mixin is deleted, restoring exact vanilla drop behavior. Commit c319c61.
- Verified: bytecode comparison against vanilla 1.21.11 (the mixin differed in exactly that one call; no other caller depends on it). Not exercised in a running game.

### A12. The hardcore gamerule squatted the vanilla namespace and showed raw keys

- Broken: the life-stealer hardcore rule was registered as minecraft:life_stealer_hardcore - inside the vanilla namespace, which mods must not use - and its translation keys matched neither the old nor any new id, so the gamerule screen displayed raw key strings instead of a name.
- Cause: registration used the minecraft: namespace, and the lang file keys did not line up with the actual rule id.
- Reproduce on 1.0.7: open the gamerule screen - the rule appears under a raw untranslated key in the vanilla group.
- Fix: the rule is now swordssmp:life_stealer_hardcore with working translations. Two companion pieces keep old worlds intact: the legacy minecraft: id stays registered as a deprecated alias (labeled 'Life Stealer Hardcore (legacy id, migrated)' in the screen), and on world load its stored value is copied to the new rule once, then the alias is switched off. Without that migration, the rename alone would have silently reset the rule to off in every world that had enabled it. Fine print: an 'on' legacy value wins that one migration even if the new rule was deliberately off; and downgrading a world back to 1.0.7 after opening it here reverts the setting to off, because the old build only reads the old id. Nothing corrupts - you can re-enable it. Commits 027f87c, a91bfbc, f0e0574.
- Verified: runtime for registration - the dedicated-server boot test queried both swordssmp:life_stealer_hardcore and the legacy alias and both respond. The migration itself on an actual old save is owed an in-game pass; the gamerule screen label is client-side and unverified.

### A13. Two items never appeared in the recipe book

- Broken: the XP Orb and TNT Rod recipes were missing from the recipe book until you happened to craft them - every other recipe in the mod unlocked properly.
- Cause: each recipe needs a matching recipe_unlocked advancement; these two were simply never created.
- Reproduce on 1.0.7: open the recipe book on a fresh world - xp_orb_crafting and tnt_rod_crafting are absent.
- Fix: the two missing advancements added. Commit db12666.
- Verified: runtime for loading - the server boot test loaded all 1610 advancements without error. The recipe-book display itself is client-side and unverified.

### A14. The custom creative tab listed every item twice

- Broken: all 27 entries in the mod's creative-inventory tab appeared twice.
- Cause: items were added in two places - the tab builder's displayItems callback and an ItemGroupEvents.modifyEntriesEvent listener for the same tab.
- Reproduce on 1.0.7: open the mod's creative tab and count - every item shows up two times.
- Fix: the tab builder is the single source. Commit 8fae3ca.
- Verified: code-path analysis. The tab is client-side, so the visual result is owed an in-game look.

# Section B - Mistakes the cleanup introduced, caught in review, fixed before the candidate was prepared

These were introduced during the cleanup and corrected before release. They are listed because you asked for full detail on what was fixed in this branch.

### B1. Berserk Hand enchant ratcheted to max and stuck

- Broken (in the intermediate simplified code, not in 1.0.7): the enchant level followed your health down but never came back up - dropping to 2 HP set level 10, healing to full left it at 10 forever, persisted in the item's NBT.
- Cause: the cleanup had dropped the baseline's explicit remove-old-enchantments calls and relied on the game's ItemStack.enchant(). Bytecode inspection showed enchant() routes to an upsert with Math.max semantics: it can raise a level but never lower it. Caught in review round B as a HIGH finding.
- Fix: the tick loop now writes levels with a set operation that replaces in both directions (and removes the enchant at 0), restoring the original per-tick exact behavior. Commit 5551afc.
- Verified: bytecode-verified API semantics for both the wrong and the correct call; static comparison against the original ladder. Not exercised in a running game.

### B2. Berserk health ladder edge at 0 or negative health

- Broken (intermediate only): the first closed-form rewrite of the ten-branch if-ladder handled health <= 0 differently from the original (returned 11 or left the level unchanged instead of 10).
- Cause: hand-rewritten formula edge case, caught when review A re-checked the original ladder branch by branch.
- Fix (current behavior): the formula clamp(((20 - health) / 2) + 1, 1, 10) (commit 1f3ffa3) plus a health <= 0 guard (commit cfcb74a) that returns early - the original ladder had no branch at zero or negative health and left the enchants untouched that tick, and the guard preserves exactly that. The rewrite now reproduces the original ladder for every reachable input, dying tick included. (History: before the guard, the bare formula assigned band 10 at health <= 0 - a difference reachable only while dying, with no gameplay effect since the attribute is recalculated from living health afterwards.)
- Verified: exhaustive equivalence argument over the original branches, including the health <= 0 case by construction via the guard. Not exercised in a running game.

### B3. Player save fields nearly dropped by the cleanup

- Broken (intermediate only): the simplification trimmed 13 unused per-player data fields from the save codec. No code reads them - but they are written into every player's saved data, and removing them from the schema would have silently discarded that stored state in all existing worlds.
- Cause: 'provably unread by code' is not the same as 'safe to delete' when the fields are part of the serialized save format. Caught when the schema change was re-examined after review feedback on compat surfaces.
- Fix: the full original schema is restored - fields, codec, NBT read/write, death-clone copy, reset defaults - so old and new worlds round-trip identical data. Retiring them deliberately is a future, explicit migration. Commit b67d81e.
- Verified: codec and NBT paths diffed against the baseline; a world save/stop on the boot test ran clean. A full old-save load test is owed.

### B4. The first two Smelter fixes were wrong (this is why there are five Smelter commits)

- Broken (intermediate only): the first fix for A2 simply returned false in the BEFORE handler - which cancelled vanilla's whole break path, silently skipping tool durability, ore XP, mined stats and the tool-aware loot context. The second fix moved the work to the AFTER event, but bytecode review proved the AFTER event fires before the drops exist, so that version was silently inert (plain raw drops, no smelting at all) and could only ever catch container spill.
- Cause: each approach assumed something about Fabric's event timing that the actual 1.21.11 bytecode disproved. Both were caught in review rounds with bytecode evidence.
- Fix: the final implementation (described in A2) intercepts the exact vanilla drop call with exact provenance and leaves the rest of the break lifecycle to vanilla. Commit f5de153.
- Verified: runtime, 6 of 6 in-server checks passed (see A2).

# Section C - Pure cleanup (no behavior change)

- Commit 2ea3221: the main simplification pass - Java 4397 to 3157 lines (-28%), dead code removed, three shared helpers introduced. Behavior-preserving by construction and by review.
- Commit cdc377e: deleted TNTSwordToolInHandTickProcedure - a flame-particle procedure for TNT minecarts that was never wired to anything and has never run. Wiring it up would be a feature, not a fix, and is listed in the main report's parked section.
- Commit b5e4efa: version bump 1.0.7 to 1.0.11 (1.0.8/1.0.9 exist on the unmerged Descending-Dark branch; 1.0.10 is the armor fix).

# Section D - Suspected bugs that turned out not to be bugs

- Wind Blade double-fire: retracted. The suspected second invocation path was never registered, so it fires exactly once.
- Berserk formula vs original ladder above 20 health: retracted after re-checking - the original ladder returns 1 there, and so does the formula. The formula matches the original at every health value.

# Section E - Bugs found and fixed after the 1.0.11 candidate

E1-E5 came out of the review passes that ran after the 1.0.11 candidate was prepared; E6-E8 came out of the whole-mod review of 1.0.12. Every one of them also exists in your current 1.0.7 build.

### E1. TNT Rod: reeling in re-triggered the whole TNT cast

- Broken: after casting, the bobber stays in the water; right-clicking again to reel it in ran the full ability a second time - another 5 TNT and another 500 durability, when you just wanted your bobber back.
- Cause: the ability ran on every right-click with the rod, with no check whether a cast was already out. A fishing rod's right-click is also how vanilla reels in.
- Reproduce on 1.0.7: cast the TNT Rod, then right-click again to reel in - 5 more TNT spawn and durability drops again.
- Fix: if the caster's bobber is still out, the right-click is a plain vanilla reel-in; the ability only fires on a fresh throw. Commit 413b6a9.
- Verified: runtime. Automated in-server test: a cast spawned exactly 5 TNT and cost exactly 500 durability; the reel-in spawned 0 new TNT and cost nothing.

### E2. TNT Rod: TNT spawned at every player's bobber

- Broken: the cast summoned its TNT "at every fishing bobber" - including other players' bobbers anywhere in the world. Anyone fishing while you cast had TNT dropped on their bobber.
- Cause: the original command was "/execute at @e[type=fishing_bobber] run summon tnt" - @e selects every bobber entity in the world, not yours.
- Reproduce on 1.0.7: two players fish at the same time; one casts the TNT Rod - TNT appears at both bobbers.
- Fix: the mod remembers the bobber your own throw created and summons the TNT at that bobber's position only. Commit 413b6a9.
- Verified: runtime, same automated test: exactly 5 TNT, at the caster's own bobber.

### E3. TNT Rod: Creative players paid the durability cost

- Broken: casting in Creative drained the same 500 durability as Survival. Tools should not wear in Creative.
- Cause: the durability call passed no player, so vanilla could not apply its Creative exemption.
- Fix: the casting player is passed through; vanilla now skips the wear in Creative and charges exactly 500 in Survival. Commit a85adeb.
- Verified: runtime: Survival cast = exactly 500 damage, Creative cast = 0 damage, both still spawn 5 TNT.

### E4. Life Stealer: one kill could grant +2 max health many times

- Broken: the +2 max health reward (hardcore gamerule) fired on every hit against a dying victim. Dying players stay attackable through their death animation, so holding attack on a dying player stacked +2, +4, +6... from a single kill - and several attackers could each cash in on the same dying victim.
- Cause: the reward only checked "is the victim dying", with no record that this kill had already paid out.
- Fix: the first hit that observes the victim dying - the killing blow, applied just before the callback - claims the reward with an atomic marker on the victim itself. Later hits on that dying victim, by anyone, grant nothing. The marker is stored in the victim's own saved data, so it survives chunk unload while dying and disappears with the corpse; it is cleared on respawn, so a later kill of that player pays out again. The absorption effect on kill is unchanged. Commit d1dc435.
- Verified: runtime. Automated in-server test: the killing blow granted +2 (20 -> 22 max health), further hits on the dying victim granted nothing, and a second attacker hitting the dying victim was granted nothing. That dying entities remain attackable (the reachability of the original bug) was confirmed against 1.21.11 bytecode: Entity.isAttackable() is unconditionally true and LivingEntity does not override it.

### E5. pack.mcmeta rejected by 1.21.11 pack metadata validation

- Broken: every world load logged "Couldn't load swordssmp pack metadata: Pack declares support for format 81, but game versions supporting formats 17 to 81 require a supported_formats field...". Pre-existing in 1.0.7 and carried through 1.0.11.
- Fix: pack.mcmeta now declares the supported_formats range 1.21.11 requires. Commit d238aea.
- Verified: boot test - the warning is gone; 1479 recipes and 1610 advancements load exactly as before.

### E6. Server-work queue grew forever on connected clients

- Broken: the mod's shared server-work queue (used for timed follow-ups like the TNT Rod's staggered TNT spawns) is drained only by server tick events - but item use runs on both sides, so a physical client connected to a dedicated server queued entries that could never run and never drained. Every TNT Rod cast leaked about 5 dead entries, every Earth Wave cast 1, every Wind Blade 4-jump reset 1, every Corrosive Fire application 1, accumulating for the whole client session (slow memory growth; on very long sessions, growing per-tick iteration cost).
- Cause: the queueing calls sat outside the existing server-side checks, so they fired on the client side too.
- Fix: the queueing now happens only inside server-side paths (TNT Rod, Earth Wave, Wind Blade, Corrosive Fire). The queued lambdas never executed client-side before, so this changes no behavior - vanilla client prediction is untouched; it only stops the leak. Commits 6271602 and 7cc3779.
- Verified: code-path analysis (the drain is server-only and the lambdas were already dead on the client, so guarding cannot change behavior). Not exercised with a real connected client.

### E7. Corrosive Fire damaged Creative players' gear

- Broken: the corrosive fire effect's gear damage (50 durability to hand and armor slots, repeated after 200 ticks) also applied in Creative. Tools and armor should not wear in Creative.
- Cause: the durability call passed no entity, so vanilla could not apply its Creative exemption - the same class of bug as E3 on the TNT Rod.
- Reproduce on 1.0.7: in Creative, wear armor and stand where corrosive fire applies its effect - the armor loses durability.
- Fix: the wearer is passed through (players only; non-players keep the previous behavior). Commit 7cc3779.
- Verified: runtime. Automated in-server test: Survival wearer took exactly 50 chestplate damage; Creative wearer took 0.
- Behavior note: with a real wearer now passed to the durability call, the Unbreaking enchantment can reduce corrosive wear on enchanted gear, as it does for other damage sources. This matches vanilla durability semantics; pre-fix, no entity was passed, so Unbreaking never applied.

### E8. TNT Sword 2's thrown TNT lost its big explosion after a server restart

- Broken: TNT Sword 2 throws a live TNT (very long fuse) that explodes with a bigger radius once it lands. The thrown TNT entity survives a server restart in the world save, but the set marking which TNT are "big" was kept in memory only. After a restart, a parked TNT never got its landing detonation and eventually exploded at normal radius - and the old cleanup would also have dropped the tracking for any TNT sitting in an unloaded chunk, restart or not.
- Cause: the tracking set was a static in-memory field while the entity it tracks is persistent.
- Reproduce on 1.0.7: throw a TNT with TNT Sword 2, stop the server before it lands, start it again - the TNT eventually explodes at normal size.
- Fix: the marking set now lives in per-world saved data (same storage pattern the Abomination's pending Creative restores already use), so it survives restarts together with the entity; entries whose chunk is not currently loaded are kept instead of purged. The short-lived landing counter stays in memory and is recomputed from the still-live entity. Commit c293d48.
- Verified: runtime. Automated two-boot in-server test: threw the TNT, stopped the server while it was airborne; after the restart the reloaded TNT was still marked (loaded from disk), landed, and detonated with the big explosion, which consumed its marking.

### E9. Life Stealer: a bystander could claim +2 for a kill they never made

- Broken: the once-per-kill reward (E4's fix) fired for the first hit that OBSERVED the victim dying - including a bystander's sword hit on a victim already dying of fall damage, poison, or someone else's weapon. Hitting a dying corpse with a Life Stealer paid +2 max health for a kill the attacker never made.
- Cause: the claim checked only "is the victim dying now", which any hit on an already-dying victim satisfies - it never asked whether this hit's own damage caused the dying state.
- Reproduce on 1.0.7 (and on the E4-only build): a victim falls to their death; before the corpse disappears, hit them once with a Life Stealer - +2 max health, no kill required.
- Fix: a small hurtServer mixin records the victim's dying state just before each hit's damage lands; only the hit whose own damage transitioned the victim into dying - the actual killing blow - may claim the reward. Hits on a victim already dying of another cause pay nothing and leave the reward unclaimed. The absorption effect on kill is unchanged. Commits c80cd5d.
- Verified: runtime. Automated in-server test: the pre-hit dying state is recorded correctly in both directions; a killing blow still pays 20 -> 22; a second attacker on the claimed corpse gets nothing; a sword hit on a victim already dying of another cause gets nothing and does not consume the reward.

# Section F - How the fixes were verified, and what is still owed

Runtime-tested on a real dedicated server:
- Server boots clean with everything loaded: both gamerule ids respond to queries, 1479 recipes and 1610 advancements load, a world saves and stops cleanly. As of E5, the pack metadata warning is gone too.
- The Smelter Pickaxe passed an automated in-server block-break test, 6 of 6 checks: iron ore gives exactly 1 ingot with normal tool wear; clay gives 1 terracotta; diamond ore gives 1 diamond plus XP; a chest keeps its contents; plain blocks drop normally; container spill untouched.
- The TNT Rod passed an automated in-server test: a cast spawns exactly 5 TNT at the caster's own bobber and costs exactly 500 durability in Survival, 0 in Creative; reeling in is plain vanilla - no TNT, no cost (E1-E3).
- The Life Stealer passed an automated in-server test: +2 max health exactly once per kill, no payout for hitting the dying victim again, no payout for a second attacker (E4).
- Corrosive Fire passed an automated in-server test: Survival wearer took exactly 50 durability damage, Creative wearer took 0 (E7).
- TNT Sword 2 passed an automated two-boot restart test: a TNT thrown before the shutdown was still marked as big after the restart, landed and detonated at the big radius (E8).
- The Life Stealer attribution passed an automated in-server test: pre-hit dying state recorded correctly both ways; killing blow pays 20 -> 22; bystander on the claimed corpse gets nothing; a hit on a victim already dying of another cause gets nothing and leaves the reward unclaimed (E9).

Verified statically (source diff, exact API and bytecode semantics, two independent review rounds) but NOT exercised in a running game: every other fix in Sections A and B, plus the client-queue leak fix E6 (its queued lambdas never ran client-side, so guarding them is provably behavior-neutral, but no real connected client has exercised it).

Still owed before you ship, as an in-game pass: the Fortune/Silk Smelter run; every other ability firing once at the right cost and cooldown; the Berserk ladder at runtime; the Abomination borrow/restore across relog and restart; Earth Wave spawning and cleanup; the gamerule migration on a real old save; and the client-side surfaces (creative tab, recipe book, gamerule screen).

Two housekeeping notes. The design-level findings that were deliberately not fixed (Levitation Wand self-damage and the rest of the main report's Part 3) remain your design calls - the Life Stealer and TNT Rod items from that list were fixed and are in Section E. And the MCreator caveat still applies: many files say 'REGENERATED on each build', so re-opening the project in MCreator can overwrite these hand-written fixes - re-apply the patches after any MCreator session.
