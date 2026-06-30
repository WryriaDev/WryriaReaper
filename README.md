<div align="center">

# ⟨ WryriaReaper ⟩

**ʜɪɢʜ-ᴘᴇʀғᴏʀᴍᴀɴᴄᴇ, ᴍɪɴɪᴍᴀʟɪsᴛ ᴇɴᴛɪᴛʏ ᴄʟᴇᴀɴᴇʀ ғᴏʀ ᴘᴀᴘᴇʀ/sᴘɪɢᴏᴛ 1.21.1**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square&logo=minecraft&logoColor=white)
![Paper](https://img.shields.io/badge/Paper%20API-1.21.1-44cc11?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

A lightweight, zero-bloat ground item cleaner designed for performance-critical servers.

</div>

---

## ✦ Features

- **Async countdown** — Timer runs off the main thread; zero TPS impact
- **Sync purge** — Entity removal safely scheduled on the main thread (Bukkit API compliant)
- **Accurate item counting** — Correctly counts the amount of items inside a stack (e.g., 64 dirt = 64 removed)
- **Loaded chunks only** — Never scans unloaded chunks; no unnecessary world loading
- **Dual config system** — `config.yml` for settings, `messages.yml` for all player-facing text
- **MiniMessage + Hex** — Full [MiniMessage](https://docs.advntr.dev/minimessage/format.html) support with `<#hex>` and legacy `&#hex` color codes
- **Small caps aesthetic** — Stylish Unicode small caps in default messages
- **Hot reload** — Reload both configs without restarting the server
- **Configurable warnings** — Toggle 60s, 30s, 10s countdown alerts independently

---

## ✦ Commands

| Command | Description | Permission |
|---------|------------|------------|
| `/reaper clear` | Instantly purge all target entities | `reaper.admin` |
| `/reaper reload` | Reload config.yml and messages.yml | `reaper.admin` |
| `/reaper time` | Show time until next purge | `reaper.use` |

**Aliases:** `/wr`, `/wyrriareaper`

---

## ✦ Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `reaper.admin` | Access to `clear` and `reload` commands | OP |
| `reaper.use` | Access to `time` command | Everyone |

---

## ✦ Configuration

### config.yml

```yaml
# Purge interval in seconds (default: 300 = 5 minutes)
interval: 300

# Entity types to remove
entity-types:
  - ITEM
  - PRIMED_TNT
  - EXPERIENCE_ORB

# Active worlds (empty = all worlds)
worlds:
  - world
  - world_nether
  - world_the_end

# Countdown warning toggles
warnings:
  60-seconds: true
  30-seconds: true
  10-seconds: true
```

### messages.yml

All player-facing messages are fully customizable with [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

**Supported color formats:**
- MiniMessage native: `<#FF5555>`, `<gradient:#aa:#bb>`, `<red>`, `<bold>`
- Legacy hex: `&#FF5555` (auto-converted to MiniMessage)

**Available placeholders:**
| Placeholder | Description |
|-------------|-------------|
| `{count}` | Number of entities removed |
| `{time}` | Seconds remaining |
| `{minutes}` | Time in `m:ss` format |

---

## ✦ Installation

1. Download `WryriaReaper-1.0.0.jar` from [Releases](../../releases)
2. Drop the JAR into your server's `plugins/` folder
3. Start or restart your server
4. Edit `plugins/WryriaReaper/config.yml` and `messages.yml` to your liking
5. Run `/reaper reload` to apply changes

---

## ✦ Building from Source

**Requirements:** Java 21+, Maven 3.9+

```bash
git clone https://github.com/YOUR_USERNAME/WryriaReaper.git
cd WryriaReaper
mvn clean package
```

The compiled JAR will be at `target/WryriaReaper-1.0.0.jar`.

---

## ✦ Architecture

```
dev.wyrria.reaper
├── WryriaReaper.java          → Plugin lifecycle & module init
├── config/
│   └── ConfigManager.java     → Dual YAML loader, MiniMessage renderer
├── command/
│   ├── ReaperCommand.java     → /reaper subcommand executor
│   └── ReaperTabCompleter.java → Permission-aware tab completion
└── task/
    └── CleanerTask.java       → Async countdown + sync purge engine
```

**Thread model:**
```
Async Thread (BukkitRunnable)          Main Server Thread
 │                                      │
 ├─ countdown.decrementAndGet()         │
 │                                      │
 ├─ if warning threshold ──────────────→├─ broadcast warning
 │                                      │
 └─ if countdown <= 0 ────────────────→├─ scan loaded chunks
                                        ├─ remove target entities
                                        └─ broadcast result ({count})
```

---

## ✦ License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

**ᴍᴀᴅᴇ ᴡɪᴛʜ ♥ ʙʏ ᴡʏʀʀɪᴀ**

</div>
