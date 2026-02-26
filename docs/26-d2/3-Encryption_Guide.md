# CoffeeIRC 加密通讯指南

## 概述
CoffeeIRC内置了企业级的加密通讯系统，采用RSA+AES双重加密机制，为用户提供安全可靠的聊天环境。该系统无需额外配置证书，自动完成密钥交换和加密握手。

## 加密机制详解

### 加密架构
```
客户端 ↔ 服务器
   ↓       ↓
RSA密钥对  RSA密钥对
   ↓       ↓
AES会话密钥 ←→ AES会话密钥
   ↓       ↓
加密消息   加密消息
```

### 加密流程

#### 1. 连接阶段（自动完成）
```java
// 使用随机密钥（默认方式）
Client client = new Client(4, "localhost", 10025, "User", "username", "MyClient");
client.Connect(); // 就在这一步的时候自动触发加密握手

// 或使用自定义密钥
Client secureClient = new Client(4, "localhost", 10025, "User", "username", "MyClient", "my-custom-key-2026");
secureClient.Connect(); // 使用自定义密钥生成确定性的RSA密钥对
```

**内部处理步骤：**
1. 客户端生成RSA 2048位密钥对
2. 发送连接请求，包含客户端公钥
3. 接收服务器公钥和加密的AES会话密钥
4. 使用私钥解密AES会话密钥
5. 加密通讯通道建立完成

#### 2. 消息传输阶段（透明加密）
```java
// 发送消息时自动加密
client.sendMessage("这是一条加密消息");
// 接收消息时自动解密（在服务器端）
```

## 安全特性

RSA长度2048位用于密钥交换，AES 256位（ECB Mode）用于消息传输。并且无需手动配置，而且绘画也有隔离，还有生命周期管理。

### RSA加密（密钥交换）
- **密钥长度**: 2048位（可选3072位或4096位）
- **用途**: 安全地交换AES会话密钥
- **优势**: 前向安全性，即使长期密钥泄露也不会影响历史通讯

### AES加密（消息传输）
- **密钥长度**: 256位
- **模式**: ECB模式（简单高效）
- **用途**: 加密实际的聊天消息内容
- **优势**: 高性能，适合实时通讯

### 自动密钥管理
- **灵活配置**: 支持随机密钥和自定义密钥两种模式
- **会话隔离**: 每个客户端连接使用独立的AES密钥
- **生命周期管理**: 连接断开时自动清理密钥
- **确定性生成**: 相同自定义密钥产生相同RSA密钥对

## API使用示例

### 基本加密通讯
```java
import mod.deplayer.coffeechat.coffeeirc.client.Client;
import mod.deplayer.coffeechat.coffeeirc.server.Server;

// 跟正常方式一样，不用过多配置

public class EncryptedChatExample {
    public static void main(String[] args) throws Exception {
        Server server = new Server(4, 10025, "SecureChat", "安全聊天室", "MyDistribution", "server-key-2026");// 使用自定义密钥
        server.startServer();
        
        Client client = new Client(4, "localhost", 10025, 
                                 "Alice", "alice_secure", "SecureClient", "server-key-2026");
        
        client.Connect();

        client.sendMessage("这是一条完全加密的消息！");
        client.sendMessage("使用自定义密钥确保了一致性");

        client.disconnect();
        server.stopServer();
    }
}
```

## 配置选项

### 密钥配置选项

#### RSA密钥长度选择
```java
// 在Client.java和Server.java中可以调整RSA密钥长度：
// 推荐使用2048位（默认）
// 在速度快的同时，安全性也高
keyGen.initialize(2048, new SecureRandom());

// 如需更高安全性可选择：
// keyGen.initialize(3072, new SecureRandom()); // 更高安全性，稍慢
// keyGen.initialize(4096, new SecureRandom()); // 最高安全性，较慢
```

#### 自定义密钥使用
```java
// 服务器端使用自定义密钥
Server server = new Server(4, 10025, "MyServer", "服务器描述", "Distribution", "my-server-key-2026");

// 客户端使用相同自定义密钥
Client client = new Client(4, "localhost", 10025, "User", "username", "Client", "my-server-key-2026");

// 注意事项：
// 1. 相同的自定义密钥会产生相同的RSA密钥对
// 2. 不同的自定义密钥无法互相通讯
// 3. 建议使用复杂且唯一的密钥字符串
// 4. 密钥应该安全存储和分发
```

### AES密钥配置
```java
// 在Server.java中配置AES密钥长度：
KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
aesKeyGen.init(256); // 256位AES加密
```

## 安全最佳实践

### 1. 密钥长度选择
- **一般应用**: 使用默认的2048位RSA + 256位AES
- **高安全要求**: 可升级到3072位RSA
- **极高安全要求**: 可使用4096位RSA（性能影响较大）

### 2. 自定义密钥安全
- **密钥复杂度**: 使用至少16位以上的复杂字符串
- **唯一性**: 为不同环境使用不同的自定义密钥
- **存储安全**: 密钥应安全存储，避免硬编码在源代码中
- **定期更换**: 定期更换自定义密钥以提高安全性

### 3. 网络安全
- 确保使用可信网络环境
- 建议配合HTTPS反向代理使用
- 定期更新Java版本以获得最新安全补丁

### 4. 日志安全
- 聊天日志包含明文消息，注意存储安全
- 建议对日志文件进行访问控制
- 敏感信息应避免记录到日志中

## 故障排除

### 常见问题

#### 1. 加密握手失败
```
错误信息: [加密握手失败] 与客户端的加密握手失败
解决方案: 
- 检查Java版本是否支持所需加密算法
- 确保使用Java 25或更高版本
- 验证JCE策略文件完整性
```

#### 2. 消息解密失败
```
错误信息: [消息解密错误] 客户端消息解密失败
解决方案:
- 检查连接是否正常建立
- 确认AES密钥是否正确交换
- 查看是否有网络中断导致密钥丢失
```

#### 3. 性能问题
```
现象: 加密通讯较慢
优化建议:
- 使用2048位而非4096位RSA密钥
- 确保有足够的系统内存
- 考虑批量发送减少加密次数
```

## 技术细节

### 加密握手协议
```sequence
Client->Server: 连接请求 + 客户端公钥
Server->Client: 服务器公钥 + 加密的AES密钥
Client->Server: 使用私钥解密AES密钥
Note right of Client: 加密通道建立完成
```

### 消息加密流程
1. 客户端使用AES密钥加密消息
2. 加密后的消息通过HTTP发送到服务器
3. 服务器使用对应的AES密钥解密消息
4. 服务器记录解密后的明文到日志

### 密钥存储安全
- RSA私钥仅存储在生成它的设备内存中
- AES会话密钥在连接期间存储在内存中
- 连接断开后所有密钥立即从内存清除

---
*本文档适用于 CoffeeIRC v26.d2 版本*