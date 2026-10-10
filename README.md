<h1 align="center">
  <img width="64" height="64" align="center" alt="DeclutterUI icon" src="https://github.com/user-attachments/assets/a35067af-06b1-4dfd-b690-c53f9c6d44a4">
  DeclutterUI
</h1>

<p align="center">
  <a href="https://modrinth.com/mod/declutterui">Modrinth</a> |
  <a href="https://www.curseforge.com/minecraft/mc-mods/declutterui">CurseForge</a>
</p>

<p align="center">
  A NeoForge mod for Minecraft that hides annoying warnings, buttons, and popups.
</p>

---

## What it does

Every option can be turned on or off individually in the in-game config screen.

Hide Player Glow Outlines is off by default under HUD & Privacy. It suppresses
player glow and spectator outlines locally; other entities keep their outlines.

Tab-list options (all off by default under HUD & Privacy):
- Hide Tab Header & Footer hides server banners and footer text.
- Hide Tab Rank Tags removes team prefixes and suffixes, preserving nicknames where possible.
- Show Account Usernames in Tab replaces server display names with account usernames.

Player heads and ping icons remain visible. Custom tags embedded directly in display names
may remain when only rank hiding is enabled; enable account usernames too for plain names.
Server text inserted as fake player entries is not a header or footer and is unaffected.

Version 1.0.4 adds Hide Player Profile Popups under HUD & Privacy, off by default.
Hide Rank Tags Above Players is also off by default. It displays account usernames
with their team color, removing nameplate prefixes, suffixes and nicknames.
Separate server holograms are unaffected.
It hides player entity tooltips and text tooltips linked to private-message commands
in chat while preserving click actions. Custom server formats may not be detected.

### Menus & Buttons

| Option | What it removes |
|---|---|
| Hide Realms Button | The "Minecraft Realms" button on the title screen |
| Hide Online Options Button | The "Online Options" button in Settings |
| Hide Credits & Attribution Button | The credits button in Settings |
| Hide Accessibility Icon | The small accessibility icon on the title screen |
| Hide Language Button | The language shortcut button on the title screen |

### Warnings & Prompts

| Option | What it removes |
|---|---|
| Skip Online Play Warning Screen | The "Caution: Third-Party Online Play" screen when clicking Multiplayer |
| Skip Experimental World Warning | The confirmation screen when creating a world with experimental features |
| Skip Narrator Setup Screen | The narrator/accessibility setup screen on first launch |
| Skip World Upgrade Backup Screen | The backup prompt when loading a world from an older version *(off by default)* |

### Game Menu (Pause Screen)

| Option | What it removes |
|---|---|
| Hide Give Feedback & Report Bugs Buttons | The two feedback link buttons |
| Hide Player Reporting Button | The player reporting button; Mods moves beside Options (multiplayer only) |
| Hide Open to LAN Button | The Open to LAN button; Mods moves beside Options (singleplayer only) |

### Notifications

| Option | What it removes |
|---|---|
| Hide Advancement Unlock Popups | The pop-up when you unlock an advancement |
| Hide Recipe Unlock Popups | The pop-up when you unlock a new recipe |
| Hide Tutorial Hint Popups | The pop-up hints that appear early in the game |
| Hide Narrator Toggle Popup | The pop-up when turning the narrator on or off |
| Hide Unsecure Server Warning | The pop-up warning when joining a server without secure chat |
| Hide World Backup Success Popup | The notification shown after a world backup completes |
| Hide Resource Pack Error Popups | Resource-pack load, copy, and file-import failure notifications *(off by default)* |
| Hide Resource Pack Download Progress | The resource-pack download progress notification *(off by default)* |

### Title Screen

| Option | What it removes |
|---|---|
| Hide Yellow Splash Text | The rotating yellow "Random splash!" text next to the logo |
| Hide Copyright Notice | The "Copyright Mojang AB. Do not distribute!" text |
| Hide Version Text | The version and modded text in the bottom-left corner *(off by default)* |

### Other

| Option | What it does |
|---|---|
| Turn Off Data Collection | Disables Mojang's telemetry/data tracking |
| Hide Recipe Book Button | Removes the recipe book button from inventory, crafting table, and furnace |
| Hide Chat Message Indicators | Hides the colored bars shown next to chat messages |
| Hide Selected Item Name Popup | Hides item names shown above the hotbar when changing slots *(off by default)* |
| Hide Boss Bars | Hides boss names and health bars displayed at the top of the screen *(off by default)* |
| Hide Scoreboard Sidebar | Hides server scoreboards displayed on the right side of the screen *(off by default)* |
| Hide Action Bar Messages | Hides server messages displayed above the hotbar *(off by default)* |
| Hide Floating Hologram Text | Hides text displays and invisible armor-stand labels commonly used for server holograms *(off by default)* |
