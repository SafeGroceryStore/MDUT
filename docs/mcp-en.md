# MCP Usage Guide

MDUT supports exposing features via **MCP (Model Context Protocol)**. AI clients can connect via STDIO or HTTP SSE.

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

# Or for a renamed binary
sudo ln -s /path/to/Multiple_Database_Utilization_Tools /usr/local/bin/mdut
```

After that, you can use `mdut` directly in the terminal and MCP configuration. On Linux, extract the zip and make the binary executable first:

```bash
unzip Multiple_Database_Utilization_Tools_linux_amd64.zip
chmod +x Multiple_Database_Utilization_Tools
```

Example (Linux):

```bash
Multiple_Database_Utilization_Tools mcp
```

### 1. STDIO Mode (Default)

AI clients communicate with MDUT through standard input/output. Suitable for local tools like Claude Desktop.

```bash
{mdut} mcp
```

Example (macOS App):

```bash
/Applications/Multiple_Database_Utilization_Tools.app/Contents/MacOS/Multiple_Database_Utilization_Tools mcp
```

Example (Linux):

```bash
Multiple_Database_Utilization_Tools mcp
```

### 2. HTTP SSE Mode

MDUT exposes MCP endpoints as an HTTP service, and AI clients connect via Server-Sent Events (SSE).

```bash
{mdut} mcp --http --bind 127.0.0.1 --port 16699
```

For LAN access (be aware of security risks):

```bash
{mdut} mcp --http --bind 0.0.0.0 --port 16699 --token mytoken
```

## Configuration Examples

### Claude Desktop (STDIO)

Edit `claude_desktop_config.json` and replace `{mdut}` with the actual executable path:

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

### Claude Desktop (SSE)

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

## Endpoint Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sse` | GET | SSE event stream endpoint for receiving server messages |
| `/messages` | POST | Endpoint for client-to-server requests |

## Notes

- The default bind address `127.0.0.1` only allows local access; use `0.0.0.0` for LAN access.
- Token authentication is **required** when exposing to LAN / public network.
- Make sure the firewall allows the configured port.
- If the command is not found, make sure the executable is in your `PATH`, or use an absolute path in the configuration.
