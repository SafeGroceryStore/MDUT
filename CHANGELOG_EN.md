# Changelog

## 2026/07/23 - `v3.1.1`
### Core
* Added MCP (Model Context Protocol) server with STDIO and HTTP SSE transports
* Exposed 24 MCP tools: connection management, SQL query/execute, database metadata, command execution, file manager, reverse shell, SOCKS5 proxy, and more
* Token authentication support; SSE mode supports configurable bind address and port
* Fixed MySQL UDF `exec_command` returning "UDF not deployed" when MCP creates a new Exploiter instance on each call
* Removed the hardcoded 120-second timeout from MCP `deploy_exploit`; timeout is now controlled by the caller
* Fixed MCP `start_socks5` binding to the broadcast address `255.255.255.255` when `bind_addr` is omitted; empty address now defaults to `0.0.0.0`
* Connection dialog password is now optional
* Disabled closing the connection dialog by clicking the backdrop (gray area) to prevent accidental dismissal

## 2026/07/04 - `v3.1.0`
### Core
* Complete rewrite using Go + Wails v2 + React/TypeScript
* All database drivers replaced with pure Go implementations, zero CGO dependency
* Single binary distribution with embedded frontend assets, no external runtime required
* Supports Windows / macOS (Intel + Apple Silicon)
* Brand new modern UI (shadcn/ui + Tailwind CSS v4, dark/light themes)
* Built-in Monaco Editor for SQL queries
* Chinese/English internationalization support (i18next)
* SOCKS5 proxy tunneling for all database types
* Local SQLite persistent storage for connection configs

### MySQL
* UDF privilege escalation and command execution
* File manager
* Reverse shell
* Shellcode execution
* SOCKS5 forward proxy

### MSSQL
* xp_cmdshell, OLE Automation, and Agent Job execution strategies
* File manager
* Environment probing and one-click deployment

### PostgreSQL
* UDF privilege escalation and command execution
* File manager
* Reverse shell
* SOCKS5 forward proxy

### Oracle
* Java Source / Scheduler execution strategies
* File manager
* Reverse shell
* SOCKS5 forward proxy

### Redis
* Module deployment (Rogue Server / master-slave replication)
* Command execution
* File manager (RDB write)
* Reverse shell
* SOCKS5 forward proxy (requires module)

---

## 2022/05/24 - `v2.1.1`
### Core
* Optimized logic code
* Changed user agreement window
* Refactored Http Tunnel generation interface

### Mysql
* Fixed MySQL error dialog not showing in some cases

### Mssql
* Replaced official Microsoft driver with jTDS (official driver had too many issues)

### Oracle
* Added Oracle single upload feature

### Redis
* Optimized internal code
* Fixed Redis test connection returning success incorrectly
* Added reverse shell feature (not recommended in production)
* Fixed Redis error dialog not showing in some cases
* Enhanced `Replace SSH Public Key` feature


## 2022/05/24 - `v2.1.0`
### Core
* Added HTTP tunnel feature (Redis not supported yet)
* Optimized logic code
* Increased default timeout

### Mssql
* Fixed download file bug
* Removed admin password retrieval feature
* Added one-click restore all components
* Fixed CLR Hex String

### Oracle
* Changed JAVA Util import method
* Optimized JAVA ShellUtil code

### Redis
* Added slave-read-only feature (Thx @xslzlccc)

## 2021/12/01 - `v2.0.8`
### Core
* Fixed Mssql connection compatibility with SQL Server 2000
* Set program default encoding
* Removed sensitive files (see announcement in documentation)

## 2021/09/14 - `v2.0.7`
### Core
* Optimized internal code
* Revised settings window text

### PostgreSql
* Added Windows UDF privilege escalation support
* Revised plugin naming rules

### Other
* Updated PostgreSql UDF plugin documentation link (thx @huahua)
* No longer requires downloading v2.0 first after v2.0.6

## 2021/08/17 - `v2.0.6`
### Core
* Optimized internal code
* Removed software auto-update on startup
* Optimized update interface and added online download update (GitHub Api)
    > Best used with a proxy and Proxifier

* No longer requires downloading v2.0 first after this version

## 2021/06/21 - `v2.0.5`
### Core
* Changed "Add and Settings" interface size
* Added JDBCUrl timeout parameter in settings

## 2021/06/21 - `v2.0.4`
### Core
* Fixed Mac Chinese path bug
* Fixed settings interface logic issue

### Redis
* Fixed Redis test connection always returning success bug

## 2021/06/20 - `v2.0.3`
### Core
* Added option to disable "software startup warning"
* Added config reset feature
* Added multi-select delete for database list on home page

### Oracle
* Fixed Oracle command execution timeout bug (thx @yzddmr6)
* Enabled file manager feature (Linux not tested)

## 2021/05/12 - `v2.0.2`
### Core
* Fixed config.yaml encoding inconsistency caused by FileWriter leading to garbled text
* Optimized detail code

## 2021/05/11 - `v2.0.1 `
### Core
* Fixed dependency package initialization issue caused by file:// protocol on Windows
* Project code refactored, open sourced, UI optimized
* Improved Redis database utilization
* Fixed multiple bugs and improved code logic
* Custom loading of database dependency packages using reflection, compatible with ~90% of database connections
* Fixed single-thread UI freeze bug (inspired by Behinder)
### Mysql
* Fixed Chinese garbled text
* Fix #4
* Added Windows reverse shell feature

### Mssql
* Optimized file manager UI interaction

### Oracle
* Simplified initialization and command execution operations
* Optimized command execution internal logic
### PostgreSql
* Optimized UI interaction

## 2021/04/22 - `v1.2.1`
* MDAT renamed to MDUT

## 2021/02/03 - `v1.2`
### Mssql
* Improved file manager feature
* Added admin password retrieval feature
* Optimized user interaction logic

## 2021/01/06 - `v1.1`
* Added update check feature
* Added close and about buttons

### Oracle
* Added create function feature (requires corresponding account privileges)
* Added reverse shell feature
* Added multiple command execution types
* Added trace cleanup feature
* Fine-tuned UI interaction

### Mssql
* Added activation component feature
* Added SPOACREATE COM component command execution
* Optimized trace cleanup feature

## 2020/12/30 - `v1.0`
* Released first version of MDAT
