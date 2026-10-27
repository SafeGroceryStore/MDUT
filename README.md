<div align="center">

<img src="./build/appicon.png" width="200" />

# MDUT — Multiple Database Utilization Tools

Cross-platform desktop database utilization and management tool

[![Go](https://img.shields.io/badge/Go-1.25%2B-blue)](https://golang.org)
[![Wails](https://img.shields.io/badge/Wails-v2.12-ff4d4d)](https://wails.io)
[![React](https://img.shields.io/badge/React-18.3-61dafb)](https://react.dev)
[![License](https://img.shields.io/github/license/SafeGroceryStore/MDUT.svg?style=flat-square)](https://github.com/SafeGroceryStore/MDUT/blob/main/LICENSE)
[![Stargazers](https://img.shields.io/github/stars/SafeGroceryStore/MDUT.svg?style=flat-square)](https://github.com/SafeGroceryStore/MDUT/stargazers)

</div>

---

[中文](./README_ZH.md) / [ChangeLogs](./CHANGELOG_EN.md)

## Introduction

**MDUT** (Multiple Database Utilization Tools) is a cross-platform desktop database management and exploitation tool, rebuilt from scratch with **Go + Wails v2 + React/TypeScript**. It supports graphical management of multiple mainstream databases and integrates advanced exploitation modules for authorized security testing.

- 🖥️ **Pure Go Drivers** — Zero CGO, single-binary cross-compilation for all platforms
- 🔒 **Zero-dependency Runtime** — Embedded frontend assets, runs as a single binary
- 🌐 **Proxy Support** — SOCKS5 proxy tunneling for all database types
- 🎨 **Modern UI** — Dark/Light themes with React 18 + Tailwind CSS v4 + shadcn/ui
- 🌍 **i18n** — Built-in Chinese/English internationalization
- 📝 **SQL Editor** — Monaco Editor with syntax highlighting and multi-result support
- ⌨️ **CLI Mode** — sqlmap-style command-line interface for scripting and headless environments

## Supported Databases

- **MySQL** 5.5+ / 8.0+ (including MariaDB)
- **Microsoft SQL Server** 2005+
- **PostgreSQL** 8.2+
- **Oracle** 11g+
- **Redis** 4.0+

## Features

| Feature | MySQL | MSSQL | PostgreSQL | Oracle | Redis |
|---------|:-----:|:-----:|:----------:|:------:|:-----:|
| Connection Management | ✅ | ✅ | ✅ | ✅ | ✅ |
| SQL Query Editor (Monaco) | ✅ | ✅ | ✅ | ✅ | — |
| Table Browsing & Editing | ✅ | ✅ | ✅ | ✅ | — |
| Schema Inspection | ✅ | ✅ | ✅ | ✅ | — |
| Command Execution (UDF/CLR/Java) | ✅ | ✅ | ✅ | ✅ | ✅ |
| File Manager | ✅ | ✅ | ✅ | ✅ | ✅ |
| Reverse Shell | ✅ | ✅ | ✅ | ✅ | ✅ |
| Shellcode Execution (Windows targets only) | ✅ | ✅ | ✅ | — | ✅ |
| SOCKS5 Forward Proxy | ✅ | ✅ | ✅ | ✅ | ✅ |
| RDB Write (Redis) | — | — | — | — | ✅ |
| CLI Mode | ✅ | ✅ | ✅ | ✅ | ✅ |

> **MSSQL** supports xp_cmdshell, OLE Automation, and Agent Job execution strategies, plus an optional CLR extension on Windows targets (reverse shell / shellcode / SOCKS5). File management and chunked uploads/downloads do not depend on CLR and are available according to the built-in strategy's reported capabilities.

## Download

Download pre-built binaries from the [Releases](../../releases) page:

| Platform | Format |
|----------|--------|
| Windows | `.exe` / `.zip` |
| macOS (Universal) | `.dmg` / `.zip` |
| Linux (amd64) | `.zip` (contains executable) |

> macOS binary is a Universal Binary that runs natively on both Apple Silicon and Intel Macs.  
> Linux release is a zip archive containing the x86_64 executable. Extract it and run `./Multiple_Database_Utilization_Tools`.

### macOS: Remove Quarantine Attribute

Since the app is not signed with an Apple Developer certificate, macOS Gatekeeper will block the app on first launch. Run the following command to remove the quarantine attribute:

```bash
sudo xattr -r -d com.apple.quarantine /Applications/Multiple_Database_Utilization_Tools.app
```

If you see "Multiple_Database_Utilization_Tools.app is damaged and can't be opened", this command will fix it.

### Linux: Install webkit2gtk Runtime

The GUI and MCP modes on Linux depend on the webkit2gtk runtime library. It requires root privileges to install and must be executed by the user:

**Ubuntu / Debian / Linux Mint:**

```bash
sudo apt update && sudo apt install libwebkit2gtk-4.1-0
```

**Arch / Manjaro:**

```bash
sudo pacman -S webkit2gtk-4.1
```

**Fedora / Rocky / RHEL:**

```bash
sudo dnf install webkit2gtk4.1
```

> After installation, run the extracted executable directly. On first launch it automatically writes a `.desktop` entry and an icon into the user's home directory.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Go 1.25, Wails v2.12 |
| Frontend | React 18.3, TypeScript 5.7, Vite 5.4, Tailwind CSS v4, shadcn/ui |
| State | Zustand 5.0 |
| Editor | Monaco Editor |
| i18n | i18next |
| Storage | SQLite (local, pure Go) |

## Development

### Prerequisites

- [Go 1.25+](https://golang.org/dl/)
- [Node.js 20+](https://nodejs.org/)
- [Wails CLI](https://wails.io/docs/gettingstarted/installation)

## Redis Relay

When the target Redis is behind NAT or a SOCKS5 proxy, the rogue server technique requires an intermediate relay to forward traffic. See **[Redis Relay Usage Guide](./docs/redis-relay-en.md)** for setup instructions.

> Relay tool: [mdut-relay](https://github.com/Ch1ngg/mdut-relay)

## MCP

MDUT exposes its capabilities through the Model Context Protocol (MCP), enabling AI clients to interact with databases, execute commands, and manage files programmatically. See **[MCP Usage Guide](./docs/mcp-en.md)** for configuration and startup instructions.

## CLI

MDUT includes a sqlmap-style CLI with DSN connections, SQL and metadata operations, exploit deployment, command execution, file operations, interactive shell, reverse shell, and SOCKS5 support. Use `--help` for the full command surface (Chinese/English bilingual). See the **[CLI Usage Guide](./docs/cli-en.md)** for complete connection formats, actions, lifecycle semantics, database-specific options, and examples.

```bash
./Multiple_Database_Utilization_Tools cli --help
./Multiple_Database_Utilization_Tools cli -d "mysql://root:root@127.0.0.1:3306/mysql" --exec "id"
./Multiple_Database_Utilization_Tools cli -d "mysql://root:root@127.0.0.1:3306/mysql" --shell
```

## Thanks

[j1anFen](https://github.com/j1anFen) / [冰蝎](https://github.com/rebeyond/Behinder) / [ODAT](https://github.com/quentinhardy/odat) / [MSDAT](https://github.com/quentinhardy/msdat) / SQLTOOLS - 深度撞击 / [PayloadsAllTheThings](https://github.com/swisskyrepo/PayloadsAllTheThings) / [WarSQLKit](https://github.com/mindspoof/MSSQL-Fileless-Rootkit-WarSQLKit)


## Star History

[![Star History Chart](https://api.star-history.com/chart?repos=SafeGroceryStore/MDUT&type=date&legend=top-left&sealed_token=CewH_F_-y8Kqt7CKcCOBScfbsupLJGYJH651nSKiC3ZtQBCDKWK1_F1N93-EiBdCOPJL84az2UG72qoRR7mAyr009EdAcVEHqvVfhwJVMxCTIOpamn3NYnuQwa93W6q48i1WqyarwozCnzS-yp6qJXXtGD1S8lYJbiucD1IjwVAs74hNsyvX2uKn8px3)](https://www.star-history.com/?repos=SafeGroceryStore%2FMDUT&type=date&legend=top-left)

## Disclaimer

> This tool is intended solely for authorized security testing and enterprise security assessment. You must ensure that all usage complies with applicable local laws and regulations. The developers and contributors assume no legal liability for any misuse. By using this tool, you acknowledge that you have read and agreed to these terms.
