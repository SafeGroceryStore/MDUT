# 更新日志

## 2026/10/26 - `v3.3.0`
### 核心
* 新增 sqlmap 风格 CLI 模式：连接、SQL/元数据、利用部署、命令执行、文件操作、交互 Shell、SOCKS5 等
* 修复 Redis ARM64 架构识别与 MySQL 老环境 GLIBC 兼容问题
* 修复 Oracle 11g Java CLOB 调用兼容问题；MSSQL 清理时恢复原配置


## 2026/10/20 - `v3.2.0`
### 核心
* MSSQL 新增 CLR 高级扩展：反弹 Shell、Shellcode 执行、SOCKS5 隧道、文件管理（Windows 目标，SQL Server 2005~2022 全版本）
* Redis 支持 Windows 目标：Module 部署（命令执行/文件管理/SOCKS5/Shellcode）、主从复制精确文件写入（二进制载荷逐字节落盘，支持 webshell/启动项等场景）
* Redis 文件写入支持本地文件载荷选择与精确/快速两种写入模式
* PostgreSQL 修复 Windows 版 PG 9.4 及以下 UDF 部署失败的问题
* PostgreSQL 修复中文 Windows 目标错误信息乱码的问题
* Oracle 新增权限提升（Java Source / DBMS_SCHEDULER 双策略，自动判别与回退）
* 新增 linux/arm64 载荷：MySQL、PostgreSQL（9.6+）、Redis
* 载荷全面压缩加密，程序体积显著减小
* MCP 修复若干问题：部署链不生效、清理工具不可用、会话状态丢失、执行字符集与静默模式参数支持
* 文件管理在 Windows 目标支持盘符枚举
* 命令执行页新增分库操作引导；回显/静默切换按钮改为明确的可切换样式
* 左侧导航栏支持点击图标收起/展开连接面板

## 2026/10/17 - `v3.1.2`
### 核心
* 修复 garble 混淆发布版中 MCP 启停按钮报 `TypeError: Cannot read properties of undefined (reading 'MCPAPI')` 的问题（#83），前端切换到 Wails 生成绑定（`ObfuscatedCall`）
* 全数据库利用链落地标识符随机化，去除固定 `mdut` 前缀特征，避免受控环境按表名/函数名/文件名特征拦截（#84）：
* PostgreSQL：COPY FROM PROGRAM 临时表名、libc 函数名、UDF 落盘文件名
* MySQL：UDF 部署中转表名、UDF 落盘文件名
* MSSQL：xp_cmdshell 探测标记、`%TEMP%` 临时文件名、会话临时表名、Agent Job 名
* Oracle：Java 包装函数名（自动兼容并清理旧版固定名残留部署）
* Redis：Module 默认落地路径、RDB 写文件键名、Lua 沙箱逃逸标记

## 2026/07/23 - `v3.1.1`
### 核心
* 新增 MCP（Model Context Protocol）服务器，支持 STDIO 与 HTTP SSE 两种传输模式
* MCP 提供 24 个工具：连接管理、SQL 查询/执行、数据库元数据、命令执行、文件管理、反弹 Shell、SOCKS5 代理等
* MCP 支持 Token 认证，SSE 模式可配置绑定地址与端口
* 修复 MCP 每次新建 Exploiter 导致 MySQL UDF `exec_command` 报 "UDF not deployed" 的问题
* 修复 MCP `deploy_exploit` 硬编码 120 秒超时的问题，改由调用方控制
* 修复 MCP `start_socks5` 未传 `bind_addr` 时绑定到广播地址 `255.255.255.255` 的问题，空地址默认回退到 `0.0.0.0`
* 连接对话框密码改为非必填
* 禁止点击连接对话框遮罩层（灰色区域）关闭弹窗，避免误触退出

## 2026/07/04 - `v3.1.0`
### 核心
* 项目使用 Go + Wails v2 + React/TypeScript 全面重写
* 所有数据库驱动替换为纯 Go 实现，零 CGO 依赖
* 单二进制文件发布，内嵌前端资源，无需额外运行环境
* 支持 Windows / macOS (Intel + Apple Silicon) / Linux 全平台
* 全新现代化 UI（shadcn/ui + Tailwind CSS v4，暗色/亮色主题）
* 内置 Monaco Editor SQL 查询编辑器
* 中文/英文国际化支持（i18next）
* 全数据库类型支持 SOCKS5 代理穿透
* 本地 SQLite 持久化存储连接配置

### MySQL
* 支持 UDF 提权与命令执行
* 文件管理器
* 反弹 Shell
* Shellcode 执行
* SOCKS5 正向代理

### MSSQL
* 支持 xp_cmdshell、OLE Automation、Agent Job 三种执行策略
* 文件管理器
* 环境探测与一键部署

### PostgreSQL
* 支持 UDF 提权与命令执行
* 文件管理器
* 反弹 Shell
* SOCKS5 正向代理

### Oracle
* 支持 Java Source / Scheduler 两种执行策略
* 文件管理器
* 反弹 Shell
* SOCKS5 正向代理

### Redis
* 支持 Module 部署（Rogue Server / 主从复制）
* 命令执行
* 文件管理器（RDB 写入）
* 反弹 Shell
* SOCKS5 正向代理（需 Module）

---

## 2022/05/24 - `v2.1.1`
### 核心
* 优化逻辑代码
* 更改用户协议窗口
* 重构 Http Tunnel 生成界面

### Mysql
* 修正 Mysql 某些时候错误不弹窗

### Mssql
* 将依赖包重新替换为 jTDS (Microsoft 官方驱动太多问题)

### Oracle
* 增加oracle 单独上传功能

### Redis
* 优化内部代码
* 再次修复 Redis 测试连接错误信息返回连接成功 Bug
* 增加反弹shell功能 (不推荐使用影响生产环境)
* 修复 Redis 某些时候错误不弹窗
* 增强 `替换 SSH 公钥` 功能


## 2022/05/24 - `v2.1.0`
### 核心
* 增加 HTTP 隧道功能(Redis暂不支持)
* 优化逻辑代码
* 加长默认超时时间

### Mssql
* 修复下载文件 Bug
* 删除获取管理员密码功能
* 增加一键恢复所有组件功能
* 修正 CLR Hex String

### Oracle
* 更改 JAVA Util 导入方式
* 优化 JAVA ShellUtil 代码

### Redis
* 添加 slave-read-only 功能 (Thx @xslzlccc)

## 2021/12/01 - `v2.0.8`
### 核心
* 修复 Mssql 连接 2000 时候的语句兼容性问题
* 设置程序默认编码
* 删除敏感文件(详细说明情况请看文档里的公告一栏)

## 2021/09/14 - `v2.0.7`
### 核心
* 优化内部代码
* 修订设置窗口文字

### PostgreSql
* 增加 Windows UDF 提权支持
* 修订插件命名规则

### 其他
* 文档更新 PostgreSql UDF 插件编写链接 (thx @huahua)
* v2.0.6 之后不再强制要求先下载 v2.0 版，下载即用

## 2021/08/17 - `v2.0.6`
### 核心
* 优化内部代码
* 删除软件自启更新功能
* 优化更新功能界面，增加在线下载更新功能(Github Api)
    > 最好用梯子配合 Proxifier 进行更新

* 后续不再强制要求先下载 v2.0 版，下载即用

## 2021/06/21 - `v2.0.5`
### 核心
* 更改「新增和设置」的界面尺寸
* 新增设置里面 JDBCUrl 的超时参数

## 2021/06/21 - `v2.0.4`
### 核心
* 修复 Mac 中文路径 Bug
* 修复设置界面逻辑问题

### Redis
* 修复 Redis 测试连接时候永远返回成功 Bug

## 2021/06/20 - `v2.0.3`
### 核心
* 增加「软件启动弹出警告」关闭功能
* 增加配置文件重设功能
* 增加首页数据库列表多选删除功能

### Oracle
* 修复 Oracle 命令执行超时 Bug (感谢 @yzddmr6)
* 开启文件管理功能 (暂未测试 Linux 系统)

## 2021/05/12 - `v2.0.2`
### 核心
* 修复 FileWriter 引起的 config.yaml 文件编码不一致导致读取乱码问题
* 优化细节代码

## 2021/05/11 - `v2.0.1 `
### 核心
* 修复 Windows 下 file:// 协议导致的依赖包无法初始化问题
* 项目代码重构，代码开源，界面优化
* 完善 Redis 数据库利用
* 修改多处 Bug，完善多处代码逻辑
* 利用反射技术自定义加载数据库依赖包，基本兼容90%的数据库连接
* 解决单线程 UI 卡死 Bug (参考冰蝎源码)
### Mysql
* 修复中文乱码错误
* Fix #4
* 增加 Windows 反弹 Shell 功能

### Mssql
* 优化文件管理 UI 逻辑交互

### Oracle
* 简化初始化功能和命令执行操作
* 优化命令执行功能内部逻辑
### PostgreSql
* 优化 UI 逻辑交互

## 2021/04/22 - `v1.2.1`
* MDAT 改名为 MDUT

## 2021/02/03 - `v1.2`
### Mssql
* 完善文件管理功能
* 增加获取管理员密码功能
* 优化用户交互逻辑

## 2021/01/06 - `v1.1`
* 增加更新检测功能
* 增加关闭、关于按钮

### Oracle
* 增加创建函数功能使用前需要先按照对应账号权限创建函数
* 增加反弹 Shell 功能
* 增加多种命令执行类型
* 增加清除痕迹功能
* 微调 UI 交互

### Mssql
* 增加激活组件功能
* 增加 SPOACREATE COM 组件的命令执行方式
* 优化清理痕迹功能

## 2020/12/30 - `v1.0`
* 发布第一版 MDAT
