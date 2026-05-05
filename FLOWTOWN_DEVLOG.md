Flowtown — Devlog
=================

Summary
-------

This devlog documents what is already implemented in the Flowtown repository as of today. It focuses on implemented modules, the repository/folder layout, build artifacts, and notes about which parts are templates or need configuration. There are multiple mods in this repo — each is kept in its own folder and often contains its own Gradle wrapper and build output.

Notable modules (implemented)
--------------------------------
- `combat-log/` — Combat-log project with a `combat-log-report-1.21.11/` submodule.
- `discord-bot/` — Discord bot for combat-log; includes `config.example.json`, `config.json`, `config.local.json`, a `database/` folder, and Gradle build scripts.
- `armour-invis-template-1.21.11/` — Armor/invisibility template mod (template-style project).
- `drop-events-1.21.11/` — Drop events mod.
- `logon-check-template-1.21.11/` — Logon-check template mod.
- `solid-displays-1.21.11/` — Solid displays mod.
- `swordsmp 1.21.11/` and `swordsmp 1.21.11 compined with armourinvis/` — Main SwordsMP mods and combined variants.
- `whitelist-handler/` — Contains a `discord-bot/` helper, `whitelist-mod/`, and docs.

Repository layout
-----------------

- Root-level helper scripts and docs:
  - `build-all`, `build-all.bat`, `build-all.ps1` — build convenience scripts.
  - `README.md`, `TESTING.md`, `FEATURES.md`, `RUNTIME_TEST_RESULTS.md` — project documentation.
  - `config.json` — repository-level configuration (used by some automation or scripts).
- Each mod folder typically includes:
  - `build.gradle` and `gradle.properties` — Gradle build files.
  - `gradlew` and `gradlew.bat` — Gradle wrapper scripts.
  - `settings.gradle` when needed.
  - `src/` with `main/` and sometimes `client/` sources.
  - `bin/`, `build/`, `gradle/` wrapper directory and `run/` environment used for local testing.

Notes about the `combat-log` and `discord-bot`
---------------------------------------------
- `combat-log/` contains a `combat-log-report-1.21.11/` submodule (a dedicated report mod) and a `discord-bot/` folder at the top level that coordinates with the mod.
- `discord-bot/` contains sample and local config files (`config.example.json`, `config.json`, `config.local.json`). The bot stores runtime data under `discord-bot/database/` and writes logs to `discord-bot/logs/`.

What is ready-to-use vs. template/needs-configuration
------------------------------------------------------
- Ready-to-build: many folders already include a Gradle wrapper and build scripts; running the wrapper in a subfolder will build that subproject and produce artifacts in its `build/libs` or `build/` directories.
- Not immediately runnable out-of-the-box: template folders (names containing `template` or those that are obvious skeletons) require configuration and adaptation before deployment. The `discord-bot` needs its `config.local.json` (or equivalent) populated with tokens/DB settings before running the bot.
- Several project folders target Minecraft `1.21.11` (folder names reflect this). They are implemented as mod projects but may require environment-specific setup (local Minecraft dev environment, mappings, or credentials) to run end-to-end.

Build and run hints
-------------------

Use each mod's Gradle wrapper to build a single module. Examples (PowerShell):

```powershell
Set-Location combat-log/discord-bot
.\gradlew.bat shadowJar

Set-Location combat-log/combat-log-report-1.21.11
.\gradlew.bat build
```

Or use the root helper scripts `build-all` / `build-all.bat` / `build-all.ps1` to run multi-module builds when appropriate.

Artifacts
---------
- Build outputs appear under each module's `build/` directory (for jars check `build/libs`).
- Logs and runtime state for the Discord bot live in `discord-bot/logs/` and `discord-bot/database/`.

Multiple mods and integration
-----------------------------
This repo contains multiple distinct mod projects and helper tools (the Discord bot, reporting modules, and several template mods). They are organized as separate folders so they can be built and tested independently or combined as needed.

Where to look next
------------------
- Read module READMEs: many folders include `README.md` or `USAGE_GUIDE.md` with module-specific guidance.
- For the Discord bot: copy `discord-bot/config.example.json` to `discord-bot/config.local.json` and fill in credentials before starting.
- For building a single mod, run that mod's Gradle wrapper as shown above.

If you want, I can:
- Expand this devlog into a changelog with dates and contributors.
- Add build examples for each mod folder.
- Create a short `Quickstart.md` that walks through building and running the Discord bot and one mod together.

---
Generated devlog for Flowtown.
