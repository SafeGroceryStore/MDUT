# 更新日志

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
