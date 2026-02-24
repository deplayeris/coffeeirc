# CoffeeIRC 核心API详细文档

## 概述
CoffeeIRC是一个基于Java的轻量级IRC聊天库，采用HttpServer技术实现客户端-服务器通信架构。该库提供完整的聊天功能，包括连接管理、消息传递、广播系统和日志记录等功能。

## 服务器端 (Server)

### Server 类
**公共类** - IRC服务器核心类，负责处理所有网络通信和业务逻辑

#### 构造方法
```java
// 完整参数构造器
Server(int ipProtocol, int port, String serverInstanceName, String serverDescription, String DistributionName)

// 简化构造器
Server(int ipProtocol, int port, String DistributionName)
Server(int port, String serverInstanceName, String DistributionName)  
Server(int port, String DistributionName)
Server(String serverInstanceName, String DistributionName)
```

**参数说明：**
- `ipProtocol`: IP协议版本（4或6）
- `port`: 服务器监听端口
- `serverInstanceName`: 服务器实例名称
- `serverDescription`: 服务器描述信息
- `DistributionName`: 发行版名称

#### 公共方法

##### 服务器控制方法
```java
void startServer()
```
启动IRC服务器，初始化HTTP服务和所有处理器类。该方法会：
- 创建HttpServer实例并绑定指定端口
- 注册所有路由处理器
- 启动聊天日志系统
- 输出服务器启动信息

```java
void stopServer()
```
优雅地停止服务器，执行清理操作：
- 关闭聊天日志文件
- 断开所有客户端连接
- 清理资源并释放内存

##### 状态查询方法
```java
int getConnectedClientCount()
```
返回当前在线用户数量

```java
Map<String, ClientInfo> getConnectedClients()
```
返回当前所有在线客户端信息的副本映射

##### 广播功能
```java
void broadcastMessage(String message)
```
向所有在线用户发送广播消息，通过推送服务实现消息分发

#### 私有成员变量
- `Logger sml`: 服务器主日志记录器
- `HttpServer serverHttp`: HTTP服务器实例
- `Map<String, ClientInfo> connectedClients`: 在线客户端映射表
- `int clientIdCounter`: 客户端ID计数器
- `PrintWriter chatLogWriter`: 聊天日志写入器

### ClientInfo 类
**私有类** - 客户端连接信息管理类

#### 构造方法
```java
ClientInfo(String clientId, String username, String ipAddress)
```
创建客户端信息实例，自动记录连接时间和最后活动时间

#### 公共方法
```java
// 基础信息获取
String getClientId()           // 获取客户端唯一标识符
String getUsername()           // 获取用户名称
String getIpAddress()          // 获取客户端IP地址

// 时间信息获取
LocalDateTime getConnectTime()         // 获取连接建立时间
LocalDateTime getLastActivityTime()    // 获取最后活动时间
void updateActivityTime()              // 更新最后活动时间为当前时间

// 格式化时间获取
String getFormattedConnectTime()       // 获取格式化的连接时间字符串
String getFormattedLastActivityTime()  // 获取格式化最后活动时间字符串
```

### 处理器类
**私有类** - HTTP请求处理核心组件

#### ConnectHandler (连接处理器类)
处理客户端连接请求的处理器类
- **路由路径**: `/connect`
- **请求方法**: POST
- **功能**: 验证客户端身份，分配客户端ID，建立连接会话

**请求体格式**:
```json
{
  "nickname": "用户昵称",
  "username": "用户名"
}
```

**响应格式**:
```json
{
  "status": "success",
  "message": "连接成功",
  "clientId": "分配的客户端ID"
}
```

#### MessageHandler (消息处理器类)  
处理客户端消息发送请求的处理器类
- **路由路径**: `/message`
- **请求方法**: POST
- **功能**: 接收并处理用户消息，支持特殊命令

**请求体格式**:
```json
{
  "nickname": "用户昵称",
  "message": "消息内容",
  "clientId": "客户端ID"
}
```

**支持的特殊命令**:
- `/users`: 查询在线用户列表
- `/help`: 获取帮助信息

**响应格式**:
```json
{
  "status": "success",
  "message": "消息接收成功",
  "timestamp": "时间戳"
}
```

#### DisconnectHandler (断开连接处理器类)
处理客户端断开连接请求的处理器类
- **路由路径**: `/disconnect`
- **请求方法**: POST
- **功能**: 安全断开客户端连接，清理会话数据

**请求体格式**:
```json
{
  "nickname": "用户昵称",
  "clientId": "客户端ID"
}
```

**响应格式**:
```json
{
  "status": "success",
  "message": "已断开连接"
}
```

#### BroadcastHandler (广播系统处理器类)
处理广播消息请求的处理器类
- **路由路径**: `/broadcast`
- **请求方法**: POST
- **功能**: 接收广播请求并向所有在线用户推送消息

**请求体格式**:
```json
{
  "type": "broadcast_test",
  "message": "广播消息内容",
  "sender": "发送者名称",
  "clientId": "客户端ID"
}
```

**响应格式**:
```json
{
  "status": "success",
  "message": "广播消息已处理"
}
```

## 客户端 (Client)

### Client 类
**公共类** - IRC客户端核心类，负责与服务器通信

#### 构造方法
```java
Client(int ipProtocol, String ip, int port, String nickname, String username, String DistributionName)
```
创建客户端实例并自动初始化相关服务

**参数说明：**
- `ipProtocol`: IP协议版本（4或6）
- `ip`: 服务器IP地址
- `port`: 服务器端口
- `nickname`: 用户显示昵称
- `username`: 用户登录名
- `DistributionName`: 客户端发行版名称

#### 公共方法

##### 连接管理
```java
void Connect()
```
连接到指定的IRC服务器：
- 发送连接请求到服务器`/connect`端点
- 接收并保存服务器分配的客户端ID
- 启动本地推送服务监听端口10026

```java
void disconnect()
```
断开与服务器的连接：
- 发送断开请求到服务器`/disconnect`端点
- 停止本地推送服务
- 关闭聊天日志文件
- 清理客户端状态

##### 消息发送
```java
void sendMessage(String message)
```
向服务器发送聊天消息：
- 发送消息到服务器`/message`端点
- 记录消息到本地聊天日志
- 包含客户端ID进行身份验证

#### 私有成员变量
- `Logger cml`: 客户端主日志记录器
- `String clientId`: 服务器分配的客户端唯一标识符
- `boolean isConnected`: 客户端连接状态标志
- `PrintWriter chatLogWriter`: 客户端聊天日志写入器
- `HttpServer pushServer`: 本地推送服务实例

### PushHandler 类
**私有类** - 本地推送服务处理器类

#### 构造方法
自动创建，无需手动实例化

#### 公共方法
```java
void handle(HttpExchange exchange)
```
处理来自服务器的推送消息：
- **监听路径**: `/push`
- **请求方法**: POST
- **功能**: 接收服务器推送的广播消息和其他通知

**请求体格式**:
```json
{
  "type": "broadcast",
  "message": "推送消息内容",
  "timestamp": "时间戳"
}
```

**响应格式**:
```json
{
  "status": "success"
}
```

#### 私有方法
```java
void startPushService()
```
启动本地推送服务，监听端口10026

```java
void stopPushService()
```
停止本地推送服务并释放相关资源

## 日志系统

### 核心日志
使用Log4j2框架记录系统运行日志，输出到`ciclogs`目录

### 聊天日志
独立的聊天内容记录系统：
- **服务器端**: `./ciclog/chatlog-s-日期.log`
- **客户端端**: `./ciclog/chatlog-c-日期.log`
- **格式**: `YYYY-MM-DD HH:MM:SS [ 用户名 ] 消息内容`

## 使用示例

### 服务器端完整示例
```java
import mod.deplayer.coffeechat.coffeeirc.server.Server;

public class ServerExample {
    public static void main(String[] args) {
        try {
            // 创建服务器实例
            Server server = new Server(4, 10025, "MyChatServer", 
                                     "我的聊天服务器", "MyDistribution");
            
            // 启动服务器
            server.startServer();
            
            System.out.println("服务器启动成功，按Ctrl+C停止");
            
            // 保持服务器运行
            Thread.currentThread().join();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 客户端完整示例
```java
import mod.deplayer.coffeechat.coffeeirc.client.Client;

public class ClientExample {
    public static void main(String[] args) {
        // 创建客户端实例
        Client client = new Client(4, "localhost", 10025, 
                                 "Alice", "alice_user", "MyClient");
        
        // 连接服务器
        client.Connect();
        
        // 发送消息
        client.sendMessage("你好，世界！");
        client.sendMessage("这是第二条消息");
        
        // 查询在线用户
        client.sendMessage("/users");
        
        // 断开连接
        client.disconnect();
    }
}
```

### 多客户端聊天示例
```java
public class MultiClientExample {
    public static void main(String[] args) throws InterruptedException {
        // 启动服务器
        Server server = new Server(10025, "ChatRoom");
        server.startServer();
        
        // 创建多个客户端
        Client alice = new Client(4, "localhost", 10025, "Alice", "alice", "Client1");
        Client bob = new Client(4, "localhost", 10025, "Bob", "bob", "Client2");
        Client charlie = new Client(4, "localhost", 10025, "Charlie", "charlie", "Client3");
        
        // 客户端连接
        alice.Connect();
        bob.Connect();
        charlie.Connect();
        
        Thread.sleep(1000); // 等待连接建立
        
        // 聊天交互
        alice.sendMessage("大家好！");
        Thread.sleep(500);
        
        bob.sendMessage("Hello Alice!");
        Thread.sleep(500);
        
        charlie.sendMessage("我也来了！");
        Thread.sleep(500);
        
        // 服务器广播
        server.broadcastMessage("欢迎新用户加入聊天室！");
        
        Thread.sleep(1000);
        
        // 断开连接
        alice.disconnect();
        bob.disconnect();
        charlie.disconnect();
        
        // 停止服务器
        server.stopServer();
    }
}
```

## 技术架构

### 网络通信层
- **基础技术**: Java内置HttpServer
- **通信协议**: HTTP/1.1
- **数据格式**: JSON
- **端口配置**: 服务器监听端口 + 客户端推送端口(10026)

### 核心组件关系
```
Server (主服务)
├── HttpServer (HTTP服务)
├── ConnectHandler (连接处理器)
├── MessageHandler (消息处理器)  
├── DisconnectHandler (断开处理器)
├── BroadcastHandler (广播处理器)
└── ClientInfo Manager (客户端管理)

Client (客户端)
├── HttpClient (HTTP客户端)
├── PushHandler (推送处理器)
└── Local HttpServer (推送服务)
```

### 数据流向
1. **客户端连接**: Client → ConnectHandler → ClientInfo注册
2. **消息发送**: Client → MessageHandler → 聊天日志记录
3. **广播推送**: Server → PushHandler → 客户端接收
4. **断开连接**: Client → DisconnectHandler → 资源清理

---
*这个文档适用于 CoffeeIRC v26.dr1.1 版本*