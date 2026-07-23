<div align="center">

<img src="./build/appicon.png" width="200" />

# MDUT — Multiple Database Utilization Tools

跨平台桌面数据库利用与管理工具

[![Go](https://img.shields.io/badge/Go-1.25%2B-blue)](https://golang.org)
[![Wails](https://img.shields.io/badge/Wails-v2.12-ff4d4d)](https://wails.io)
[![React](https://img.shields.io/badge/React-18.3-61dafb)](https://react.dev)
[![License](https://img.shields.io/github/license/SafeGroceryStore/MDUT.svg?style=flat-square)](https://github.com/SafeGroceryStore/MDUT/blob/main/LICENSE)
[![Stargazers](https://img.shields.io/github/stars/SafeGroceryStore/MDUT.svg?style=flat-square)](https://github.com/SafeGroceryStore/MDUT/stargazers)

</div>

---

[English](./README.md) / [更新日志](./CHANGELOG.md)

## 简介

**MDUT**（Multiple Database Utilization Tools）是一款跨平台桌面数据库管理与利用工具，基于 **Go + Wails v2 + React/TypeScript** 全新重写。支持多种主流数据库的图形化管理，并集成高级利用功能模块，专为授权安全测试场景设计。

- 🖥️ **纯 Go 驱动** — 所有数据库驱动均为纯 Go 实现，零 CGO，支持单二进制交叉编译
- 🔒 **零依赖运行** — 内置前端资源，单文件即可运行
- 🌐 **代理支持** — 全数据库类型支持 SOCKS5 代理穿透
- 🎨 **现代化 UI** — 基于 React 18 + Tailwind CSS v4 + shadcn/ui 的暗色/亮色双主题
- 🌍 **多语言** — 内置中文/英文国际化支持
- 📝 **SQL 编辑器** — Monaco Editor，支持语法高亮和多结果集

## 支持的数据库

- **MySQL** 5.5+ / 8.0+（含 MariaDB）
- **Microsoft SQL Server** 2005+
- **PostgreSQL** 8.2+
- **Oracle** 11g+
- **Redis** 4.0+

## 功能特性

| 功能 | MySQL | MSSQL | PostgreSQL | Oracle | Redis |
|------|:-----:|:-----:|:----------:|:------:|:-----:|
| 数据库连接与管理 | ✅ | ✅ | ✅ | ✅ | ✅ |
| SQL 查询编辑器（Monaco） | ✅ | ✅ | ✅ | ✅ | — |
| 数据表浏览与编辑 | ✅ | ✅ | ✅ | ✅ | — |
| 数据库结构查看 | ✅ | ✅ | ✅ | ✅ | — |
| 命令执行（UDF/CLR/Java） | ✅ | ✅ | ✅ | ✅ | ✅ |
| 文件管理器 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 反弹 Shell | ✅ | — | ✅ | ✅ | ✅ |
| Shellcode 执行 | ✅ | — | — | — | — |
| SOCKS5 正向代理 | ✅ | — | ✅ | ✅ | ✅ |
| RDB 写入（Redis 专属） | — | — | — | — | ✅ |

> **MSSQL** 当前支持 xp_cmdshell、OLE Automation、Agent Job 三种执行策略。

## 下载

从 [Releases](../../releases) 页面下载对应平台的安装包：

| 平台 | 格式 |
|------|------|
| Windows | `.exe` / `.zip` |
| macOS (Universal) | `.dmg` / `.zip` |
| Linux (amd64) | `.zip`（内含可执行文件） |

> macOS 产物为 Universal Binary，同时支持 Apple Silicon 与 Intel 架构。  
> Linux 发布包为 zip 压缩包，内含 x86_64 可执行文件，解压后运行 `./Multiple_Database_Utilization_Tools`。

### macOS：移除隔离属性

由于应用未使用 Apple 开发者证书签名，首次启动时 macOS Gatekeeper 会拦截应用。执行以下命令移除隔离属性即可：

```bash
sudo xattr -r -d com.apple.quarantine /Applications/Multiple_Database_Utilization_Tools.app
```

如果遇到「Multiple_Database_Utilization_Tools.app 已损坏，无法打开」的提示，执行该命令即可修复。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Go 1.25, Wails v2.12 |
| 前端 | React 18.3, TypeScript 5.7, Vite 5.4, Tailwind CSS v4, shadcn/ui |
| 状态管理 | Zustand 5.0 |
| 编辑器 | Monaco Editor |
| 国际化 | i18next |
| 本地存储 | SQLite（纯 Go） |

## 开发

### 前置要求

- [Go 1.25+](https://golang.org/dl/)
- [Node.js 20+](https://nodejs.org/)
- [Wails CLI](https://wails.io/docs/gettingstarted/installation)

## Redis Relay

当目标 Redis 处于 NAT 或 SOCKS5 代理后面时，Rogue Server 技术需要一个中间 Relay 来转发流量。具体部署和使用方式见 **[Redis Relay 使用说明](./docs/redis-relay.md)**。

> Relay 工具：[mdut-relay](https://github.com/Ch1ngg/mdut-relay)

## MCP

MDUT 通过 Model Context Protocol（MCP）暴露其能力，AI 客户端可以以编程方式连接数据库、执行命令、管理文件等。配置方法与启动方式见 **[MCP 使用说明](./docs/mcp.md)**。

## 致谢

[j1anFen](https://jianfensec.com/) / [冰蝎](https://github.com/rebeyond/Behinder) / [ODAT](https://github.com/quentinhardy/odat) / [MSDAT](https://github.com/quentinhardy/msdat) / SQLTOOLS - 深度撞击 / [PayloadsAllTheThings](https://github.com/swisskyrepo/PayloadsAllTheThings) / [WarSQLKit](https://github.com/mindspoof/MSSQL-Fileless-Rootkit-WarSQLKit)

## Star History

<a href="https://www.star-history.com/?repos=SafeGroceryStore%2FMDUT&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=SafeGroceryStore/MDUT&type=date&theme=dark&legend=top-left&sealed_token=R_6Yl2dRRPoXx36sLaIGpdqSE7e3XBQn_iOX4izcSxsl96JZD4PMc51ae3TAX0bRtegrzjbOgUek27VY2Jwn112ylTPvOIaz3TqxBeFHRej4oKkANtrttQ" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=SafeGroceryStore/MDUT&type=date&legend=top-left&sealed_token=R_6Yl2dRRPoXx36sLaIGpdqSE7e3XBQn_iOX4izcSxsl96JZD4PMc51ae3TAX0bRtegrzjbOgUek27VY2Jwn112ylTPvOIaz3TqxBeFHRej4oKkANtrttQ" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=SafeGroceryStore/MDUT&type=date&legend=top-left&sealed_token=R_6Yl2dRRPoXx36sLaIGpdqSE7e3XBQn_iOX4izcSxsl96JZD4PMc51ae3TAX0bRtegrzjbOgUek27VY2Jwn112ylTPvOIaz3TqxBeFHRej4oKkANtrttQ" />
 </picture>
</a>

## 免责声明

> 本工具仅供授权安全测试和企业安全建设使用。使用本工具时，您应确保所有行为符合当地法律法规。如存在任何非法使用行为，您将自行承担所有后果，本工具开发者和贡献者不承担任何法律及连带责任。使用本工具即视为您已阅读并同意上述条款。
