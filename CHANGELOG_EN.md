# Changelog

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
* Java Source / Scheduler dual execution strategies
* File manager
* Reverse shell
* SOCKS5 forward proxy

### Redis
* Module deployment (Rogue Server / master-slave replication)
* Command execution
* File manager (RDB write)
* Reverse shell
* SOCKS5 forward proxy (requires Module)

---

## 2022/05/24 - `v2.1.1`
### Core
* Optimized logic code
* Changed user agreement window
* Refactored HTTP Tunnel generation interface

### MySQL
* Fixed MySQL not showing error popup in some cases

### MSSQL
* Replaced driver with jTDS (Microsoft official driver had too many issues)

### Oracle
* Added standalone upload feature

### Redis
* Optimized internal code
* Fixed Redis test connection returning success on error (again)
* Added reverse shell feature (not recommended for production)
* Fixed Redis not showing error popup in some cases
* Enhanced SSH public key replacement feature

## 2022/05/24 - `v2.1.0`
### Core
* Added HTTP Tunnel feature (Redis not yet supported)
* Optimized logic code
* Increased default timeout

### MSSQL
* Fixed file download bug
* Removed admin password retrieval feature
* Added one-click restore all components
* Fixed CLR Hex String

### Oracle
* Changed Java Util import method
* Optimized Java ShellUtil code

### Redis
* Added slave-read-only feature (Thx @xslzlccc)

## 2021/12/01 - `v2.0.8`
### Core
* Fixed MSSQL 2000 statement compatibility issues
* Set program default encoding
* Removed sensitive files

## 2021/09/14 - `v2.0.7`
### Core
* Optimized internal code
* Revised settings window text

### PostgreSQL
* Added Windows UDF privilege escalation support
* Revised plugin naming convention

## 2021/08/17 - `v2.0.6`
### Core
* Optimized internal code
* Removed auto-update on startup feature
* Improved update interface with online download (GitHub API)
* No longer requires downloading v2.0 first

## 2021/06/21 - `v2.0.5`
### Core
* Changed add/edit interface dimensions
* Added JDBC URL timeout parameter in settings

## 2021/06/21 - `v2.0.4`
### Core
* Fixed Mac Chinese path bug
* Fixed settings interface logic issues

### Redis
* Fixed Redis always returning success on test connection

## 2021/06/20 - `v2.0.3`
### Core
* Added option to disable startup warning
* Added config file reset feature
* Added multi-select delete on home database list

### Oracle
* Fixed Oracle command execution timeout bug (Thanks @yzddmr6)
* Enabled file management feature

## 2021/05/12 - `v2.0.2`
### Core
* Fixed config.yaml encoding inconsistency caused by FileWriter
* Optimized code details

## 2021/05/11 - `v2.0.1`
### Core
* Fixed Windows file:// protocol causing dependency initialization failure
* Project code restructure, open source, UI optimization
* Improved Redis database exploitation
* Fixed multiple bugs, improved code logic
* Custom dependency loading via reflection, compatible with 90% database connections
* Fixed single-thread UI freeze bug

### MySQL
* Fixed Chinese character encoding error
* Fix #4
* Added Windows reverse shell feature

### MSSQL
* Optimized file manager UI interaction

### Oracle
* Simplified initialization and command execution
* Optimized command execution internal logic

### PostgreSQL
* Optimized UI interaction

## 2021/04/22 - `v1.2.1`
* MDAT renamed to MDUT

## 2021/02/03 - `v1.2`
### MSSQL
* Improved file management
* Added admin password retrieval
* Optimized user interaction logic

## 2021/01/06 - `v1.1`
* Added update detection
* Added close and about buttons

### Oracle
* Added function creation (requires appropriate account permissions)
* Added reverse shell
* Added multiple command execution types
* Added trace cleanup
* Minor UI adjustments

### MSSQL
* Added component activation
* Added SP_OACREATE COM command execution method
* Optimized trace cleanup

## 2020/12/30 - `v1.0`
* First release of MDAT
