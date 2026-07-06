# Redis Exploitation Relay Guide (mdut-relay)

## Background

When exploiting Redis through master-slave replication to load malicious modules, the target Redis server needs to actively connect back to the "master node". If the target is in an internal network or accessed through a SOCKS5 proxy, it cannot directly connect back to your local listener, causing the exploitation to fail.

**mdut-relay** is a lightweight TCP bidirectional forwarding tool deployed on a public VPS to solve this connectivity issue.

## Architecture

```
[MDUT Local] <──── Port 21000 ────> [VPS: mdut-relay] <──── Port 21001 ────> [Target Redis]
```

- Port **21000**: MDUT client connects to relay (control port)
- Port **21001**: Target Redis connects back to relay (target port)

## Prerequisites

1. A VPS with a public IP address
2. Open firewall ports `21000` and `21001` (TCP)
3. Download the binary for your platform from [mdut-relay releases](https://github.com/Ch1ngg/mdut-relay/releases)

## Deployment Steps

### Step 1: Start the relay on VPS

```bash
# Use default ports (21000 for control, 21001 for target)
./mdut-relay

# Custom ports
./mdut-relay -cp 21000 -tp 21001
```

Parameters:

| Flag | Description | Default |
|------|-------------|---------|
| `-cp` | MDUT client connection port (Control Port) | `21000` |
| `-tp` | Target Redis connection port (Target Port) | `21001` |

Once started, the relay will listen continuously. When both sides connect, it automatically establishes a bidirectional forwarding channel. Idle connections are automatically disconnected after 15 seconds to prevent blocking.

### Step 2: Configure relay in MDUT

In MDUT's Redis exploitation panel, set the master-slave replication (Rogue Server) mode to relay mode and fill in:

- **Relay Address**: VPS public IP
- **Control Port**: `21000` (matches `-cp`)
- **Target Connection Port**: `21001` (matches `-tp`)

### Step 3: Execute exploitation

After configuration, click "Deploy Module". MDUT will:

1. Instruct target Redis to connect to `VPS:21001` as a slave node
2. MDUT local client simultaneously connects to `VPS:21000`
3. Relay establishes transparent forwarding channel when both sides connect
4. Module files are transmitted through the channel to the target and loaded

## Use Cases

| Scenario | Relay Required? |
|----------|----------------|
| Direct access to target, local has public IP | ❌ No |
| Target via SOCKS5 proxy, local has no public IP | ✅ Yes |
| Target in internal network, local has no public IP | ✅ Yes |
| Direct access to target, local has public IP but blocked by firewall | ✅ Yes |

## Notes

- The relay does not store any data, only performs transparent forwarding
- Recommend restarting the relay after each exploitation to avoid residual connections
- VPS ports 21000/21001 should be closed after exploitation is complete

## Project Repository

[https://github.com/Ch1ngg/mdut-relay](https://github.com/Ch1ngg/mdut-relay)
