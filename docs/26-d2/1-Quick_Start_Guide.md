# CoffeeIRC 快速入门指南

## 简介
CoffeeIRC是一个轻量级的Java IRC聊天库，提供完整的客户端-服务器通信功能。

## 核心特性
- 🔧 基于Http的通信架构
- 📝 完整的日志记录系统
- 📡 实时消息推送功能
- 🔐 内置加密通讯系统（RSA+AES双重加密）
- 🎯 简洁的API设计
- 🛡️ 完善的错误处理机制

## 快速开始

### 1. 服务器端部署
```java
import mod.deplayer.coffeechat.coffeeirc.server.Server;

public class QuickStartServer {
    public static void main(String[] args) throws Exception {
        // 创建并启动服务器
        Server server = new Server(10025, "MyChatServer", "MyDistribution");
        server.startServer();
        
        System.out.println("IRC服务器已启动，监听端口: 10025");
        
        // 用于保证服务器端一直运行，推荐使用这一段语句
        Thread.currentThread().join();
    }
}
```

### 2. 客户端连接
```java
import mod.deplayer.coffeechat.coffeeirc.client.Client;

public class QuickStartClient {
    public static void main(String[] args) {
        // 创建客户端（自动启用加密通讯）
        Client client = new Client(4, "localhost", 10025, 
                                 "User", "username", "MyClient");
        
        // 连接并发送一些消息
        // 加密握手会在连接时自动完成
        client.Connect();
        client.sendMessage("Hello CoffeeIRC! 这条消息会被自动加密");
        client.disconnect();
    }
}
```

## 核心API概览

### 服务器API
| 方法 | 描述 |
|------|------|
| `startServer()` | 启动IRC服务器 |
| `stopServer()` | 停止服务器 |
| `broadcastMessage(msg)` | 广播消息给所有用户 |
| `getConnectedClientCount()` | 获取在线用户数 |

### 客户端API
| 方法 | 描述 |
|------|------|
| `Connect()` | 连接到服务器（自动进行加密握手）|
| `sendMessage(msg)` | 发送消息（自动加密传输）|
| `disconnect()` | 断开连接 |

## 处理器类说明

### 服务器端处理器
- **ConnectHandler**: 处理客户端连接请求 (`/connect`)，包含加密握手
- **MessageHandler**: 处理消息发送请求 (`/message`)，支持加密消息解密  
- **DisconnectHandler**: 处理断开连接请求 (`/disconnect`)
- **BroadcastHandler**: 处理广播消息请求 (`/broadcast`)

### 特殊命令
- `/users`: 查看在线用户列表
- `/help`: 获取帮助信息

### 加密通讯特性
- **自动加密**: 连接时自动进行RSA+AES双重加密握手
- **透明传输**: 消息发送和接收自动加密/解密
- **无需配置**: 不需要额外的证书或密钥管理
- **安全可靠**: 符合现代加密标准的安全通讯

## 文件结构
```
运行目录/
├── ciclogs/
│   ├── chatlog-s-日期.log  # 服务器聊天日志
│   └── chatlog-c-日期.log  # 客户端聊天日志
└── cic-日期.log            # 核心常规日志
```

## 下一步
- 查看完整 [API文档](2-API_Documentation.md)
  - 参考 [使用示例](2-API_Documentation.md#使用示例)
  - 了解 [技术架构](2-API_Documentation.md#技术架构)
- 学习 [加密通讯指南](3-Encryption_Guide.md) 掌握安全通讯功能

---
*这个文档适用于 CoffeeIRC v26.d2 版本*