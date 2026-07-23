# MCP 使用说明

MDUT 支持通过 **MCP（Model Context Protocol）** 暴露数据库利用能力，AI 客户端可以通过 STDIO、HTTP SSE 或 Streamable HTTP 方式连接。

> 本文档对应中文版本。英文版本见 [mcp-en.md](./mcp-en.md)。

## 目录

- [启动方式](#启动方式)
- [推荐：使用 CC Switch 配置](#推荐使用-cc-switch-配置)
- [配置示例](#配置示例)
  - [STDIO 模式](#stdio-模式)
  - [HTTP SSE 模式](#http-sse-模式)
  - [Streamable HTTP 模式](#streamable-http-模式)
- [MCP 能力清单](#mcp-能力清单)
- [常见问题排查](#常见问题排查)
- [安全建议](#安全建议)

---

## 启动方式

下文中 `{mdut}` 表示 MDUT 可执行文件路径，请根据实际安装方式替换：

- 命令行安装 / 自行编译：`mdut`
- 重命名后的二进制：`Multiple_Database_Utilization_Tools`
- macOS `.app` 包内二进制：`/Applications/Multiple_Database_Utilization_Tools.app/Contents/MacOS/Multiple_Database_Utilization_Tools`
- Linux 可执行文件：`/path/to/Multiple_Database_Utilization_Tools`

**推荐：通过软链接统一为 `mdut`，避免配置里写长路径。**

```bash
# macOS
sudo ln -s /Applications/Multiple_Database_Utilization_Tools.app/Contents/MacOS/Multiple_Database_Utilization_Tools /usr/local/bin/mdut

# Linux
sudo ln -s /path/to/Multiple_Database_Utilization_Tools /usr/local/bin/mdut
```

创建后即可在终端和 MCP 配置中直接使用 `mdut`。Linux 解压 zip 后需要给可执行文件添加权限：

```bash
unzip Multiple_Database_Utilization_Tools_linux_amd64.zip
chmod +x Multiple_Database_Utilization_Tools
```

### 1. STDIO 模式（默认）

AI 客户端通过标准输入输出与 MDUT 通信，适合本地 Claude Desktop、Cursor、Claude Code 等工具。

```bash
{mdut} mcp
```

带 Token 认证（推荐在共享机器或团队环境使用）：

```bash
{mdut} mcp --token your-token-here
```

### 2. HTTP SSE 模式

MDUT 以 HTTP 服务形式暴露 MCP 端点，AI 客户端通过 SSE（Server-Sent Events）连接。

```bash
{mdut} mcp --http --bind 127.0.0.1 --port 16699
```

局域网访问（注意安全风险）：

```bash
{mdut} mcp --http --bind 0.0.0.0 --port 16699 --token your-token-here
```

### 3. Streamable HTTP 模式

MCP 扩展的 HTTP 传输协议，使用标准 HTTP 请求-响应代替长连接 SSE，资源占用更低。

```bash
{mdut} mcp --streamable --bind 127.0.0.1 --port 16699
```

带 Token 认证：

```bash
{mdut} mcp --streamable --bind 127.0.0.1 --port 16699 --token your-token-here
```

### GUI 模式

双击启动 MDUT 桌面应用 → 点击左侧导航栏 MCP 图标按钮 → 弹出配置对话框 → 设置传输模式、绑定地址、端口、Token → 点击“启动”。

> 注意：CLI 模式和 GUI 模式互斥。GUI 模式下启动 MCP 服务后，不可关闭 MDUT 窗口。

---

## 推荐使用 CC Switch 配置

如果你同时使用 Claude Code、Codex、Gemini CLI 等多个 AI CLI 工具，推荐用 **[CC Switch](https://ccswitch.io)** 统一管理 MCP 配置，避免手动修改多个 JSON 文件。

### 安装

```bash
# macOS
brew install --cask cc-switch

# 其他平台见官方文档：https://cc-switch.cc/tutorials
```

### 配置步骤

1. 启动 CC Switch，进入 **MCP 管理面板**。
2. 点击添加 MCP Server，选择 `stdio` 或 `sse` 模式。
3. 填入 MDUT 的配置：
   - **stdio**：`command` 填 `{mdut}`，`args` 填 `["mcp"]`（需要 Token 时追加 `--token` 和 Token 值）
   - **sse**：`url` 填 `http://127.0.0.1:16699/sse`，需要 Token 时添加 `Authorization: Bearer your-token-here`
4. 选择要同步的 AI 工具（Claude Code / Codex / Gemini CLI 等）。
5. 点击“同步到工具”，CC Switch 会自动写入对应客户端的配置文件。

### CC Switch 的优势

| 特性 | 说明 |
|------|------|
| 统一管理 | 一处配置，同步到多个 AI CLI 工具 |
| 双向同步 | 既可以从 CC Switch 推送到工具，也可以从工具拉取已有配置 |
| 一键启停 | 不需要时可直接禁用某个 MCP Server |
| 配置备份 | 自动保留最近 10 份配置，方便回滚 |

---

## 配置示例

### STDIO 模式

STDIO 配置在各客户端中结构类似，核心是 `command` + `args`。

#### Claude Desktop

配置文件位置：

- macOS：`~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows：`%APPDATA%\Claude\claude_desktop_config.json`
- Linux：`~/.config/Claude/claude_desktop_config.json`

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

带 Token 认证：

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

macOS App 示例：

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

在项目根目录创建 `.cursor/mcp.json`：

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

编辑 `~/.continue/config.json`：

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

Cline 通过 VS Code 设置配置 MCP，在项目 `.vscode/mcp.json` 或全局 `settings.json` 中添加：

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

编辑 `~/.windsurf/mcp-config.json`：

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

Claude Code 支持通过项目级 `claude.md` 或全局配置接入 MCP。在 `.claude/CLAUDE.md` 同目录下，或使用 `/mcp` 命令添加：

```bash
/mcp add mdut stdio {mdut} mcp
```

> 注意：Claude Code 的 MCP 支持取决于版本，部分版本仅支持项目内已声明的 MCP Server。

---

### HTTP SSE 模式

SSE 模式需要在客户端配置 `type: "sse"` 和 URL。

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

带 Token 认证：

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

### Streamable HTTP 模式

Streamable HTTP 模式目前主要被 Claude Desktop 和部分新版 MCP 客户端支持。

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

带 Token 认证：

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

## 端点说明

| 端点 | 方法 | 说明 |
|------|------|------|
| `/sse` | GET | SSE 事件流端点，AI 客户端从此接收服务端消息 |
| `/messages` | POST | 客户端发送请求的端点 |
| `/mcp` | POST/GET | Streamable HTTP 端点 |

---

## MCP 能力清单

MDUT 通过 MCP 暴露以下能力（具体以运行时 `tools/list` 返回为准）：

- 数据库连接与探测
- SQL 执行与结果读取
- 文件系统读写（如 UDF / 提权脚本落地）
- 系统命令执行（部分数据库类型支持）
- 内网代理与隧道相关操作

AI 客户端连接成功后，通常会自动拉取可用工具列表，你可以在对话中直接要求 AI 使用 MDUT 完成数据库操作。

---

## 常见问题排查

### 1. 命令找不到 / 客户端提示启动失败

- 确认 `{mdut}` 已替换为实际可执行文件路径
- 确认二进制文件有执行权限：`chmod +x {mdut}`
- 确认 `mdut` 所在目录已加入系统 `PATH`，或在配置中使用绝对路径
- macOS `.app` 包运行时可能受 Gatekeeper 隔离属性影响，可执行：

```bash
xattr -dr com.apple.quarantine /Applications/Multiple_Database_Utilization_Tools.app
```

### 2. SSE 模式客户端无法连接

- 确认 MDUT 服务已启动：`curl http://127.0.0.1:16699/sse` 应能收到 SSE 流
- 确认客户端配置的 URL 与启动参数一致（端口、IP）
- 确认防火墙未拦截该端口
- 若使用 `0.0.0.0`，确认客户端所在机器能访问服务所在机器

### 3. Token 认证失败

- STDIO 模式下 Token 通过 `--token` 参数传入
- SSE / Streamable HTTP 模式下 Token 通过请求头 `Authorization: Bearer your-token-here` 传入
- 确认启动时和配置中的 Token 完全一致

### 4. 端口被占用

```bash
# macOS / Linux
lsof -i :16699

# Windows
netstat -ano | findstr :16699
```

更换端口后同步修改客户端配置：

```bash
{mdut} mcp --http --bind 127.0.0.1 --port 16698
```

### 5. 如何查看 MDUT MCP 日志

STDIO 模式下，MDUT 的日志通常输出到 stderr，可在配置中重定向：

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

SSE 模式下可直接在前台启动查看控制台输出。

---

## 安全建议

- **本地使用**：优先使用 STDIO 模式，无需暴露网络端口，风险最低。
- **局域网访问**：SSE / Streamable HTTP 模式必须配置 `--token`，且尽量使用 `127.0.0.1`。
- **公网访问**：强烈不建议将 MCP 服务直接暴露在公网；如必须暴露，请配合反向代理、TLS、IP 白名单和强 Token。
- **Token 管理**：避免在配置文件中使用弱口令或硬编码长期有效的 Token；团队环境建议定期轮换。
- **权限最小化**：运行 MDUT 的操作系统用户应仅拥有必要的文件和数据库权限，避免以 root / Administrator 长期运行。

---

## 注意事项

- 默认绑定 `127.0.0.1` 仅本机访问，`0.0.0.0` 允许局域网
- 局域网 / 公网访问时**必须**配置 Token 认证
- 防火墙需放行对应端口
- CLI 模式和 GUI 模式互斥，GUI 模式下启动 MCP 后请勿关闭 MDUT 主窗口
