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
| Reverse Shell | ✅ | — | ✅ | ✅ | ✅ |
| Shellcode Execution | ✅ | — | — | — | — |
| SOCKS5 Forward Proxy | ✅ | — | ✅ | ✅ | ✅ |
| RDB Write (Redis) | — | — | — | — | ✅ |

> **MSSQL** supports xp_cmdshell, OLE Automation, and Agent Job execution strategies.

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

## Thanks

[j1anFen](https://jianfensec.com/) / [冰蝎](https://github.com/rebeyond/Behinder) / [ODAT](https://github.com/quentinhardy/odat) / [MSDAT](https://github.com/quentinhardy/msdat) / SQLTOOLS - 深度撞击 / [PayloadsAllTheThings](https://github.com/swisskyrepo/PayloadsAllTheThings) / [WarSQLKit](https://github.com/mindspoof/MSSQL-Fileless-Rootkit-WarSQLKit)


## Star History

<a href="https://www.star-history.com/?repos=SafeGroceryStore%2FMDUT&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=SafeGroceryStore/MDUT&type=date&theme=dark&legend=top-left&sealed_token=CewH_F_-y8Kqt7CKcCOBScfbsupLJGYJH651nSKiC3ZtQBCDKWK1_F1N93-EiBdCOPJL84az2UG72qoRR7mAyr009EdAcVEHqvVfhwJVMxCTIOpamn3NYnuQwa93W6q48i1WqyarwozCnzS-yp6qJXXtGD1S8lYJbiucD1IjwVAs74hNsyvX2uKn8px3" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=SafeGroceryStore/MDUT&type=date&legend=top-left&sealed_token=CewH_F_-y8Kqt7CKcCOBScfbsupLJGYJH651nSKiC3ZtQBCDKWK1_F1N93-EiBdCOPJL84az2UG72qoRR7mAyr009EdAcVEHqvVfhwJVMxCTIOpamn3NYnuQwa93W6q48i1WqyarwozCnzS-yp6qJXXtGD1S8lYJbiucD1IjwVAs74hNsyvX2uKn8px3" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=SafeGroceryStore/MDUT&type=date&legend=top-left&sealed_token=CewH_F_-y8Kqt7CKcCOBScfbsupLJGYJH651nSKiC3ZtQBCDKWK1_F1N93-EiBdCOPJL84az2UG72qoRR7mAyr009EdAcVEHqvVfhwJVMxCTIOpamn3NYnuQwa93W6q48i1WqyarwozCnzS-yp6qJXXtGD1S8lYJbiucD1IjwVAs74hNsyvX2uKn8px3" />
 </picture>
</a>

## Disclaimer

> This tool is intended solely for authorized security testing and enterprise security assessment. You must ensure that all usage complies with applicable local laws and regulations. The developers and contributors assume no legal liability for any misuse. By using this tool, you acknowledge that you have read and agreed to these terms.
