# MCP 使用说明

MDUT 支持通过 **MCP（Model Context Protocol）** 暴露功能，AI 客户端可以通过 STDIO 或 HTTP SSE 方式连接。

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

# 或指向重命名后的二进制
sudo ln -s /path/to/Multiple_Database_Utilization_Tools /usr/local/bin/mdut
```

创建后即可在终端和 MCP 配置中直接使用 `mdut`。Linux 解压 zip 后需要给可执行文件添加权限：

```bash
unzip Multiple_Database_Utilization_Tools_linux_amd64.zip
chmod +x Multiple_Database_Utilization_Tools
```

示例（Linux）：

```bash
Multiple_Database_Utilization_Tools mcp
```

### 1. STDIO 模式（默认）

AI 客户端通过标准输入输出与 MDUT 通信，适合本地 Claude Desktop 等工具。

```bash
{mdut} mcp
```

示例（macOS App）：

```bash
/Applications/Multiple_Database_Utilization_Tools.app/Contents/MacOS/Multiple_Database_Utilization_Tools mcp
```

示例（Linux）：

```bash
Multiple_Database_Utilization_Tools mcp
```

### 2. HTTP SSE 模式

MDUT 以 HTTP 服务形式暴露 MCP 端点，AI 客户端通过 SSE（Server-Sent Events）连接。

```bash
{mdut} mcp --http --bind 127.0.0.1 --port 16699
```

局域网访问（注意安全风险）：

```bash
{mdut} mcp --http --bind 0.0.0.0 --port 16699 --token mytoken
```

## 配置示例

### Claude Desktop（STDIO）

编辑 `claude_desktop_config.json`，将 `{mdut}` 替换为实际可执行文件路径：

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

### Claude Desktop（SSE）

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

## 端点说明

| 端点 | 方法 | 说明 |
|------|------|------|
| `/sse` | GET | SSE 事件流端点，AI 客户端从此接收服务端消息 |
| `/messages` | POST | 客户端发送请求的端点 |

## 注意事项

- 默认绑定 `127.0.0.1` 仅本机访问，`0.0.0.0` 允许局域网
- 局域网 / 公网访问时**必须**配置 Token 认证
- 防火墙需放行对应端口
- 如果命令找不到，请检查可执行文件是否已加入 `PATH`，或在配置中使用绝对路径
