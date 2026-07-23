# MCP Usage Guide

MDUT supports exposing database exploitation capabilities via **MCP (Model Context Protocol)**. AI clients can connect via STDIO, HTTP SSE, or Streamable HTTP.

> This is the English version. For the Chinese version, see [mcp.md](./mcp.md).

## Table of Contents

- [Startup Methods](#startup-methods)
- [Recommended: Configure with CC Switch](#recommended-configure-with-cc-switch)
- [Configuration Examples](#configuration-examples)
  - [STDIO Mode](#stdio-mode)
  - [HTTP SSE Mode](#http-sse-mode)
  - [Streamable HTTP Mode](#streamable-http-mode)
- [MCP Capability List](#mcp-capability-list)
- [Troubleshooting](#troubleshooting)
- [Security Recommendations](#security-recommendations)

---

## Startup Methods

In the examples below, `{mdut}` represents the path to the MDUT executable. Replace it according to your actual installation:

- Command-line install / self-built: `mdut`
- Renamed binary: `Multiple_Database_Utilization_Tools`
- macOS `.app` bundle binary: `/Applications/Multiple_Database_Utilization_Tools.app/Contents/MacOS/Multiple_Database_Utilization_Tools`
- Linux executable: `/path/to/Multiple_Database_Utilization_Tools`

**Recommended: create a symlink to `mdut` so you don't have to use long paths in configs.**

```bash
# macOS
sudo ln -s /Applications/Multiple_Database_Utilization_Tools.app/Contents/MacOS/Multiple_Database_Utilization_Tools /usr/local/bin/mdut

# Linux
sudo ln -s /path/to/Multiple_Database_Utilization_Tools /usr/local/bin/mdut
```

After that, you can use `mdut` directly in the terminal and MCP configuration. On Linux, extract the zip and make the binary executable first:

```bash
unzip Multiple_Database_Utilization_Tools_linux_amd64.zip
chmod +x Multiple_Database_Utilization_Tools
```

### 1. STDIO Mode (Default)

AI clients communicate with MDUT through standard input/output. Suitable for local tools like Claude Desktop, Cursor, and Claude Code.

```bash
{mdut} mcp
```

With token authentication (recommended for shared machines or team environments):

```bash
{mdut} mcp --token your-token-here
```

### 2. HTTP SSE Mode

MDUT exposes MCP endpoints as an HTTP service, and AI clients connect via Server-Sent Events (SSE).

```bash
{mdut} mcp --http --bind 127.0.0.1 --port 16699
```

For LAN access (be aware of security risks):

```bash
{mdut} mcp --http --bind 0.0.0.0 --port 16699 --token your-token-here
```

### 3. Streamable HTTP Mode

MCP extended HTTP transport protocol. Uses standard HTTP request-response instead of long-lived SSE connections, resulting in lower resource usage.

```bash
{mdut} mcp --streamable --bind 127.0.0.1 --port 16699
```

With token authentication:

```bash
{mdut} mcp --streamable --bind 127.0.0.1 --port 16699 --token your-token-here
```

### GUI Mode

Double-click to launch the MDUT desktop app → click the MCP icon in the left sidebar → configure transport mode, bind address, port, and token in the dialog → click "Start".

> Note: CLI mode and GUI mode are mutually exclusive. After starting the MCP service in GUI mode, do not close the MDUT main window.

---

## Recommended: Configure with CC Switch

If you use multiple AI CLI tools such as Claude Code, Codex, and Gemini CLI, we recommend using **[CC Switch](https://ccswitch.io)** to manage MCP configurations centrally, instead of manually editing multiple JSON files.

### Installation

```bash
# macOS
brew install --cask cc-switch

# For other platforms, see the official docs: https://cc-switch.cc/tutorials
```

### Configuration Steps

1. Launch CC Switch and open the **MCP management panel**.
2. Click Add MCP Server and choose `stdio` or `sse` mode.
3. Enter the MDUT configuration:
   - **stdio**: `command` = `{mdut}`, `args` = `["mcp"]` (append `--token` and the token value if needed)
   - **sse**: `url` = `http://127.0.0.1:16699/sse`, add `Authorization: Bearer your-token-here` if token authentication is enabled
4. Select the AI tools to sync (Claude Code / Codex / Gemini CLI, etc.).
5. Click "Sync to Tools", and CC Switch will write the configuration to the corresponding client config files automatically.

### Why Use CC Switch

| Feature | Description |
|---------|-------------|
| Unified management | Configure once and sync to multiple AI CLI tools |
| Bidirectional sync | Push from CC Switch or pull existing configs from tools |
| One-click toggle | Enable or disable an MCP Server without editing files |
| Config backup | Automatically keeps the last 10 configurations for rollback |

---

## Configuration Examples

### STDIO Mode

STDIO configurations are structurally similar across clients. The core is `command` + `args`.

#### Claude Desktop

Configuration file location:

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "mdut": {
      "command": "{mdut}",
      "args": ["mcp"]
    }
  }
}
```

With token authentication:

```json
{
  "mcpServers": {
    "mdut": {
      "command": "{mdut}",
      "args": ["mcp", "--token", "your-token-here"]
    }
  }
}
```

macOS App example:

```json
{
  "mcpServers": {
    "mdut": {
      "command": "/Applications/Multiple_Database_Utilization_Tools.app/Contents/MacOS/Multiple_Database_Utilization_Tools",
      "args": ["mcp"]
    }
  }
}
```

#### Cursor

Create `.cursor/mcp.json` in the project root:

```json
{
  "mcpServers": {
    "mdut": {
      "command": "{mdut}",
      "args": ["mcp"]
    }
  }
}
```

#### Continue

Edit `~/.continue/config.json`:

```json
{
  "experimental": {
    "mcpServers": {
      "mdut": {
        "command": "{mdut}",
        "args": ["mcp"]
      }
    }
  }
}
```

#### Cline

Cline configures MCP through VS Code settings. Add to project `.vscode/mcp.json` or global `settings.json`:

```json
{
  "mcpServers": {
    "mdut": {
      "command": "{mdut}",
      "args": ["mcp"]
    }
  }
}
```

#### Windsurf

Edit `~/.windsurf/mcp-config.json`:

```json
{
  "mcpServers": {
    "mdut": {
      "command": "{mdut}",
      "args": ["mcp"]
    }
  }
}
```

#### Claude Code

Claude Code supports MCP via project-level `claude.md` or global configuration. In the same directory as `.claude/CLAUDE.md`, or use the `/mcp` command:

```bash
/mcp add mdut stdio {mdut} mcp
```

> Note: Claude Code's MCP support depends on the version. Some versions only support MCP Servers declared within the project.

---

### HTTP SSE Mode

In SSE mode, clients need `type: "sse"` and the URL configured.

#### Claude Desktop

```json
{
  "mcpServers": {
    "mdut": {
      "type": "sse",
      "url": "http://127.0.0.1:16699/sse"
    }
  }
}
```

With token authentication:

```json
{
  "mcpServers": {
    "mdut": {
      "type": "sse",
      "url": "http://127.0.0.1:16699/sse",
      "headers": {
        "Authorization": "Bearer your-token-here"
      }
    }
  }
}
```

#### Cursor

```json
{
  "mcpServers": {
    "mdut": {
      "type": "sse",
      "url": "http://127.0.0.1:16699/sse"
    }
  }
}
```

#### Continue

```json
{
  "experimental": {
    "mcpServers": {
      "mdut": {
        "transport": "sse",
        "url": "http://127.0.0.1:16699/sse"
      }
    }
  }
}
```

### Streamable HTTP Mode

Streamable HTTP is currently mainly supported by Claude Desktop and some newer MCP clients.

#### Claude Desktop

```json
{
  "mcpServers": {
    "mdut": {
      "type": "streamable-http",
      "url": "http://127.0.0.1:16699/mcp"
    }
  }
}
```

With token authentication:

```json
{
  "mcpServers": {
    "mdut": {
      "type": "streamable-http",
      "url": "http://127.0.0.1:16699/mcp",
      "headers": {
        "Authorization": "Bearer your-token-here"
      }
    }
  }
}
```

---

## Endpoint Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sse` | GET | SSE event stream endpoint for receiving server messages |
| `/messages` | POST | Endpoint for client-to-server requests |
| `/mcp` | POST/GET | Streamable HTTP endpoint |

---

## MCP Capability List

MDUT exposes the following capabilities through MCP (the actual list is returned by `tools/list` at runtime):

- Database connection and probing
- SQL execution and result reading
- File system read/write (e.g., UDF / privilege escalation scripts)
- System command execution (supported database types)
- Internal network proxy and tunneling operations

After a successful connection, the AI client will usually pull the available tool list automatically. You can then ask the AI to use MDUT to perform database operations.

---

## Troubleshooting

### 1. Command not found / client fails to start

- Make sure `{mdut}` is replaced with the actual executable path
- Make sure the binary is executable: `chmod +x {mdut}`
- Make sure the `mdut` directory is in your system `PATH`, or use an absolute path in the configuration
- macOS `.app` bundles may be blocked by Gatekeeper. Run:

```bash
xattr -dr com.apple.quarantine /Applications/Multiple_Database_Utilization_Tools.app
```

### 2. SSE mode client cannot connect

- Make sure the MDUT service is running: `curl http://127.0.0.1:16699/sse` should receive an SSE stream
- Make sure the client URL matches the startup parameters (port and IP)
- Make sure the firewall is not blocking the port
- If using `0.0.0.0`, make sure the client machine can reach the server machine

### 3. Token authentication fails

- In STDIO mode, pass the token via the `--token` argument
- In SSE / Streamable HTTP mode, pass the token via the `Authorization: Bearer your-token-here` header
- Make sure the token in the startup command exactly matches the one in the client configuration

### 4. Port already in use

```bash
# macOS / Linux
lsof -i :16699

# Windows
netstat -ano | findstr :16699
```

Change the port and update the client configuration accordingly:

```bash
{mdut} mcp --http --bind 127.0.0.1 --port 16698
```

### 5. How to view MDUT MCP logs

In STDIO mode, MDUT logs are usually written to stderr. You can redirect them in the configuration:

```json
{
  "mcpServers": {
    "mdut": {
      "command": "{mdut}",
      "args": ["mcp"],
      "env": {
        "MCP_LOG_LEVEL": "debug"
      }
    }
  }
}
```

In SSE mode, start the service in the foreground and watch the console output.

---

## Security Recommendations

- **Local use**: Prefer STDIO mode. It requires no network port and has the lowest risk.
- **LAN access**: SSE / Streamable HTTP mode must use `--token`, and preferably bind to `127.0.0.1`.
- **Public internet access**: Strongly discouraged. If absolutely necessary, use a reverse proxy with TLS, IP allowlisting, and a strong token.
- **Token management**: Avoid weak passwords or long-lived hardcoded tokens in configuration files. Rotate tokens regularly in team environments.
- **Least privilege**: The OS user running MDUT should only have the necessary file and database permissions. Avoid running as root / Administrator continuously.

---

## Notes

- The default bind address `127.0.0.1` only allows local access; use `0.0.0.0` for LAN access.
- Token authentication is **required** when exposing to LAN / public network.
- Make sure the firewall allows the configured port.
- CLI mode and GUI mode are mutually exclusive. After starting MCP in GUI mode, do not close the MDUT main window.
