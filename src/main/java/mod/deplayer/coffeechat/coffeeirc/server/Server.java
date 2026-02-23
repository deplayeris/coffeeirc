/*MIT License

Copyright (c) 2026 Deplayer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/**
* CIC服务器主类
* */


package mod.deplayer.coffeechat.coffeeirc.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.LogManager;
import mod.deplayer.coffeechat.coffeeirc.server.SwInfo;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

/**CIC服务器核心*/
public class Server {

    /**Server Main Logger<br>一般情况下，日志消息都要沿这样的方式编写：[类型]消息，因为仅靠那么几个log4j提供的消息类型是不够记录的*/
    public Logger sml = (Logger) LogManager.getLogger(Server.class);

    private int ipProtocol = 4;

    private int port = 10025;

    private String serverInstanceName = "CafeRoom";

    private String serverDescription = "A IRC Server.";

    private final String DistributionName;

    private HttpServer serverHttp;

    private Map<String, ClientInfo> connectedClients = new ConcurrentHashMap<>();
    private int clientIdCounter = 0;

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private DateTimeFormatter chatLogFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private PrintWriter chatLogWriter;
    private String currentChatLogDate;

    public Server(int ipProtocol, int port, String serverInstanceName, String serverDescription, String DistributionName){

        this.ipProtocol = ipProtocol;
        this.port = port;
        this.serverInstanceName = serverInstanceName;
        this.serverDescription = serverDescription;
        this.DistributionName = DistributionName;
        sml.info("已创建服务器实例");

    };

    public Server(int ipProtocol, int port, String DistributionName){

        this.ipProtocol = ipProtocol;
        this.port = port;
        this.DistributionName = DistributionName;
        sml.info("已创建服务器实例");

    };

    public Server(int port, String serverInstanceName, String DistributionName){

        this.port = port;
        this.serverInstanceName = serverInstanceName;
        this.DistributionName = DistributionName;
        sml.info("已创建服务器实例");

    };

    public Server(int port,String DistributionName){

        this.port = port;
        this.DistributionName = DistributionName;
        sml.info("已创建服务器实例");

    };
    public Server(String serverInstanceName, String DistributionName){

        this.serverInstanceName = serverInstanceName;
        this.DistributionName = DistributionName;
        sml.info("已创建服务器实例");

    };

    /// 服务器启动
    public void startServer() throws IOException{
        sml.info("[服务器初始化] 开始初始化HTTP服务器...");

        initializeChatLog();
        
        serverHttp = HttpServer.create(new InetSocketAddress(port), 0);

        sml.info("[路由注册] 注册HTTP路由处理器...");
        serverHttp.createContext("/connect", new ConnectHandler());
        sml.info("[路由注册] 已注册 '/connect' 路由 - 处理客户端连接请求");
        serverHttp.createContext("/message", new MessageHandler());
        sml.info("[路由注册] 已注册 '/message' 路由 - 处理客户端消息发送");
        serverHttp.createContext("/disconnect", new DisconnectHandler());
        sml.info("[路由注册] 已注册 '/disconnect' 路由 - 处理客户端断开连接");
        serverHttp.createContext("/broadcast", new BroadcastHandler());
        sml.info("[路由注册] 已注册 '/broadcast' 路由 - 处理广播消息请求");

        serverHttp.start();
        sml.info("---------------------------------------------------------------------------------");
        sml.info("[服务器启动成功] IRC服务器已成功启动");
        sml.info("[监听信息] 服务器正在监听 IPv" + ipProtocol + ":" + port);
        sml.info("[实例信息] 服务器实例名称: " + serverInstanceName);
        sml.info("[实例信息] 服务器描述: " + serverDescription);
        sml.info("");
        sml.info("[核心信息]正在使用的CoffeeIRC核心的软件信息:");
        sml.info("        版本号: " + SwInfo.version);
        sml.info("        开发状态: " + SwInfo.softwareStatus);
        sml.info("        版本代号: " + SwInfo.VerCodename);
        sml.info("        支持协议: " + SwInfo.connection);
        sml.info("");
        sml.info("当前运行本核心的发行版: " + DistributionName);
        sml.info("");
        sml.info("如果遇到核心问题，请提交至: https://github.com/deplayeris/coffeeirc/issues");
        sml.info("如在使用基于本核心的发行版(如无忧聊)时出现问题");
        sml.info("请先检查是否为核心故障(通过查看核心日志)，若非核心问题请联系发行版作者");
        sml.info("");
        sml.info("核心问题提交步骤:");
        sml.info("1. 在GitHub上创建新的Issue");
        sml.info("2. 详细准确地描述遇到的问题");
        sml.info("3. 附上出现问题时的核心日志文件");
        sml.info("---------------------------------------------------------------------------------");
        sml.info("[服务器就绪] 服务器已完全就绪，正在等待客户端连接...");
    };

    // 常规的记录客户端信息的类，你也可以参考一下，这样管理真的很方便
    class ClientInfo {
        private String clientId;
        private String username;
        private String ipAddress;
        private LocalDateTime connectTime;
        private LocalDateTime lastActivityTime;

        public ClientInfo(String clientId, String username, String ipAddress) {
            this.clientId = clientId;
            this.username = username;
            this.ipAddress = ipAddress;
            this.connectTime = LocalDateTime.now();
            this.lastActivityTime = LocalDateTime.now();
        }

        public String getClientId() { return clientId; }
        public String getUsername() { return username; }
        public String getIpAddress() { return ipAddress; }
        public LocalDateTime getConnectTime() { return connectTime; }
        public LocalDateTime getLastActivityTime() { return lastActivityTime; }
        public void updateActivityTime() { this.lastActivityTime = LocalDateTime.now(); }

        public String getFormattedConnectTime() {
            return connectTime.format(timeFormatter);
        }

        public String getFormattedLastActivityTime() {
            return lastActivityTime.format(timeFormatter);
        }
    }

    /// 连接处理器类
    class ConnectHandler implements HttpHandler {
        /// 处理客户端连接请求
        /// @param exchange 请求对象
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

                    InputStream inputStream = exchange.getRequestBody();
                    String requestBody = new String(inputStream.readAllBytes());
                    inputStream.close();

                    String username = "未知用户";
                    if (requestBody.contains("\"username\":\"")) {
                        int start = requestBody.indexOf("\"username\":\"") + 12;
                        int end = requestBody.indexOf("\"", start);
                        if (end > start) {
                            username = requestBody.substring(start, end);
                        }
                    }

                    String clientId = "client_" + (++clientIdCounter);

                    ClientInfo clientInfo = new ClientInfo(clientId, username, clientIp);
                    connectedClients.put(clientId, clientInfo);

                    sml.info("[连接请求处理] 处理来自用户 '" + username + "' 的连接请求");
                    sml.info("[客户端识别] 分配客户端ID: " + clientId);
                    sml.info("[网络信息] 客户端IP地址: " + clientIp);
                    sml.info("[连接建立] 用户 '" + username + "' (ID: " + clientId + ") 成功连接到服务器");
                    sml.info("[时间戳记] 连接建立时间: " + clientInfo.getFormattedConnectTime());
                    sml.info("[统计信息] 当前在线用户总数: " + connectedClients.size());
                    String response = "{\"status\":\"success\",\"message\":\"连接成功\",\"clientId\":\"" + clientId + "\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    sml.info("[响应完成] 已向用户 '" + username + "' (ID: " + clientId + ") 发送连接并且成功确认");

                } catch (Exception e) {
                    sml.error("[连接错误] 处理客户端连接请求时发生错误: " + e.getMessage());
                    String response = "{\"status\":\"error\",\"message\":\"连接失败\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                sml.warn("[非法请求] 收到非POST方法的连接请求");
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    /// 消息处理器类
    class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

                    InputStream inputStream = exchange.getRequestBody();
                    String requestBody = new String(inputStream.readAllBytes());
                    inputStream.close();

                    String message = "无内容";
                    String clientId = "unknown";
                    String username = "未知用户";

                    if (requestBody.contains("\"message\":\"")) {
                        int start = requestBody.indexOf("\"message\":\"") + 11;
                        int end = requestBody.indexOf("\"", start);
                        if (end > start) {
                            message = requestBody.substring(start, end);
                        }
                    }

                    if (requestBody.contains("\"clientId\":\"")) {
                        int start = requestBody.indexOf("\"clientId\":\"") + 12;
                        int end = requestBody.indexOf("\"", start);
                        if (end > start) {
                            clientId = requestBody.substring(start, end);
                        }
                    }

                    ClientInfo clientInfo = connectedClients.get(clientId);
                    if (clientInfo != null) {
                        username = clientInfo.getUsername();
                        clientInfo.updateActivityTime();
                    }

                    String timestamp = LocalDateTime.now().format(timeFormatter);
                    sml.info("[消息处理] 处理来自客户端的消息请求");
                    sml.info("[时间标记] 消息接收时间: " + timestamp);
                    sml.info("[用户验证] 已验证用户身份: '" + username + "' (ID: " + clientId + ")");
                    sml.info("[内容审查] 接收到的消息内容: " + message);
                    sml.info("[会话更新] 已更新用户 '" + username + "' 的最后活动时间");

                    logChatMessage(username, message);

                    if (message.startsWith("/")) {
                        sml.info("[命令识别] 检测到特殊命令输入: " + message);
                        if (message.equals("/users")) {
                            sml.info("[用户查询] 用户 '" + username + "' 请求查看在线用户列表");
                            sml.info("[统计查询] 当前在线用户总数: " + connectedClients.size());
                            sml.info("[列表详情] 详细在线用户信息:");
                            for (ClientInfo info : connectedClients.values()) {
                                sml.info("[用户信息] 用户名: '" + info.getUsername() + "', 客户端ID: " + info.getClientId() + ", 连接时间: " + info.getFormattedConnectTime() + ", 最后活动: " + info.getFormattedLastActivityTime());
                            }
                        } else if (message.equals("/help")) {
                            sml.info("[帮助请求] 用户 '" + username + "' 请求帮助文档");
                        } else {
                            sml.info("[未知命令] 用户 '" + username + "' 输入了未识别的命令: " + message);
                        }
                    } else {
                    }

                    String response = "{\"status\":\"success\",\"message\":\"消息接收成功\",\"timestamp\":\"" + timestamp + "\"}";
                    sml.info("[响应构造] 构造消息确认响应");
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                    sml.info("[响应完成] 已向用户 '" + username + "' (ID: " + clientId + ") 发送消息接收确认");
                    sml.info("[处理结束] 消息处理流程已完成");

                } catch (Exception e) {
                    sml.error("[消息错误] 处理客户端消息时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    String response = "{\"status\":\"error\",\"message\":\"消息处理失败\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                sml.warn("[非法请求] 收到非POST方法的消息请求");
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    /// 断开连接处理器类
    class DisconnectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {

                    String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

                    InputStream inputStream = exchange.getRequestBody();
                    String requestBody = new String(inputStream.readAllBytes());
                    inputStream.close();

                    String clientId = "unknown";
                    String username = "未知用户";

                    if (requestBody.contains("\"clientId\":\"")) {
                        int start = requestBody.indexOf("\"clientId\":\"") + 12;
                        int end = requestBody.indexOf("\"", start);
                        if (end > start) {
                            clientId = requestBody.substring(start, end);
                        }
                    }
                    ClientInfo clientInfo = connectedClients.get(clientId);
                    if (clientInfo != null) {
                        username = clientInfo.getUsername();

                        long onlineSeconds = java.time.Duration.between(clientInfo.getConnectTime(), LocalDateTime.now()).getSeconds();
                        long hours = onlineSeconds / 3600;
                        long minutes = (onlineSeconds % 3600) / 60;
                        long seconds = onlineSeconds % 60;
                        String onlineDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                        sml.info("[断开请求] 处理用户 '" + username + "' (ID: " + clientId + ") 的断开连接请求");
                        sml.info("[会话信息] 用户连接建立时间: " + clientInfo.getFormattedConnectTime());
                        sml.info("[会话时长] 用户总在线时长: " + onlineDuration);
                        sml.info("[最后活动] 用户最后活跃时间: " + clientInfo.getFormattedLastActivityTime());
                        connectedClients.remove(clientId);
                        sml.info("[会话终止] 已从活跃用户列表中移除用户 '" + username + "' (ID: " + clientId + ")");
                        sml.info("[状态更新] 断开后当前在线用户数: " + connectedClients.size());
                    } else {
                        sml.warn("[断开异常] 检测到断开请求中的客户端ID不存在 尝试断开不存在的客户端连接");
                        sml.info("[无效ID] 客户端ID: " + clientId);
                        sml.info("//当前在线用户数仍为"+connectedClients.size() +"，可能是重复断开请求或客户端状态不一致");
                    }

                    String response = "{\"status\":\"success\",\"message\":\"已断开连接\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                    sml.info("[响应发送] 已确认用户 '" + username + "' 断开连接");

                } catch (Exception e) {
                    sml.error("[断开异常] " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    sml.error("[详细错误] 堆栈跟踪:", e);
                    sml.info("[恢复措施] 正在构造错误响应以通知客户端");
                    String response = "{\"status\":\"error\",\"message\":\"断开连接失败\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    sml.info("[响应发送] 已向客户端发送断开失败的错误响应");
                }
            } else {
                sml.warn("[非法请求] 收到非POST方法的断开请求");
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    /// 广播处理器类
    class BroadcastHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
                        
                    InputStream inputStream = exchange.getRequestBody();
                    String requestBody = new String(inputStream.readAllBytes());
                    inputStream.close();
                        
                    String message = "无内容";
                    String sender = "未知用户";
                    String clientId = "unknown";
                        
                    if (requestBody.contains("\"message\":\"")) {
                        int start = requestBody.indexOf("\"message\":\"") + 11;
                        int end = requestBody.indexOf("\"", start);
                        if (end > start) {
                            message = requestBody.substring(start, end);
                        }
                    }
                        
                    if (requestBody.contains("\"sender\":\"")) {
                        int start = requestBody.indexOf("\"sender\":\"") + 10;
                        int end = requestBody.indexOf("\"", start);
                        if (end > start) {
                            sender = requestBody.substring(start, end);
                        }
                    }
                        
                    if (requestBody.contains("\"clientId\":\"")) {
                        int start = requestBody.indexOf("\"clientId\":\"") + 12;
                        int end = requestBody.indexOf("\"", start);
                        if (end > start) {
                            clientId = requestBody.substring(start, end);
                        }
                    }
                        
                    sml.info("[广播] 收到来自 '" + sender + "' (ID: " + clientId + ") 的广播请求, 广播消息: " + message);
                    broadcastMessage(message);
                        
                    String response = "{\"status\":\"success\",\"message\":\"广播消息已处理\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                        
                } catch (Exception e) {
                    sml.error("[广播错误] 处理广播请求时发生错误: " + e.getMessage());
                    sml.error("[详细错误] 堆栈跟踪:", e);
                    String response = "{\"status\":\"error\",\"message\":\"广播处理失败\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                sml.warn("[非法请求] 收到非POST方法的广播请求");
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    /// 停止服务器
    public void stopServer() throws IOException{
        sml.info("[服务器关闭] 正在关闭IRC服务器...");
        sml.info("[关闭统计] 关闭前在线用户数: " + connectedClients.size());
        closeChatLog();
        if (!connectedClients.isEmpty()) {
            sml.info("[强制断开] 以下用户将被强制断开连接:");
            for (ClientInfo clientInfo : connectedClients.values()) {
                long onlineSeconds = java.time.Duration.between(clientInfo.getConnectTime(), LocalDateTime.now()).getSeconds();
                long hours = onlineSeconds / 3600;
                long minutes = (onlineSeconds % 3600) / 60;
                long seconds = onlineSeconds % 60;
                String onlineDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                sml.info("[强制断开] 用户: '" + clientInfo.getUsername() + "' (ID: " + clientInfo.getClientId() + ")");
                sml.info("[会话信息] 连接时间: " + clientInfo.getFormattedConnectTime() + ", 在线时长: " + onlineDuration);
            }
        }

        connectedClients.clear();
        clientIdCounter = 0;

        serverHttp.stop(0);
        sml.info("[服务器关闭] IRC服务器已完全关闭");
        sml.info("[资源清理] 所有客户端连接已清除，资源已释放");
    }

    /// 获取当前在线用户数
    /// 这是 api 来的
    public int getConnectedClientCount() {
        return connectedClients.size();
    }

    /// 获取当前在线用户列表
    /// 这是 api 来的
    public Map<String, ClientInfo> getConnectedClients() {
        return new HashMap<>(connectedClients);
    }

    /**记录聊天日志所必须要使用的一个Method*/
    private void initializeChatLog() {
        try {
            File logDir = new File("./ciclog");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            currentChatLogDate = LocalDate.now().toString();
            String logFileName = "./ciclog/chatlog-s-" + currentChatLogDate + ".log";
            FileWriter fileWriter = new FileWriter(logFileName, true);
            chatLogWriter = new PrintWriter(fileWriter, true);
                
            sml.info("[聊天日志] 聊天日志系统已初始化，日志文件: " + logFileName);
                
        } catch (IOException e) {
            sml.error("[聊天日志错误] 初始化聊天日志失败: " + e.getMessage());
        }
    }

    ///记录聊天消息
    /// @param username 用户名
    /// @param message 消息
    private void logChatMessage(String username, String message) {
        try {
            String today = LocalDate.now().toString();
            if (!today.equals(currentChatLogDate)) {
                closeChatLog();
                initializeChatLog();
            }
                
            if (chatLogWriter != null) {
                String timestamp = LocalDateTime.now().format(chatLogFormatter);
                String logEntry = timestamp + " [ " + username + " ] " + message;
                chatLogWriter.println(logEntry);
                chatLogWriter.flush();
            }
        } catch (Exception e) {
            sml.error("[聊天日志错误] 记录聊天消息失败: " + e.getMessage());
        }
    }

    /**在使用完毕之后，必须关闭聊天日志记录*/
    private void closeChatLog() {
        if (chatLogWriter != null) {
            chatLogWriter.close();
            chatLogWriter = null;
            sml.info("[聊天日志] 聊天日志文件已关闭");
        }
    }

    ///向指定客户端发送推送消息
    /// @param clientId 客户端ID
    /// @param message 消息
    private void sendPushMessage(String clientId, String message) throws Exception {
        ClientInfo clientInfo = connectedClients.get(clientId);
        if (clientInfo == null) {
            throw new Exception("客户端不存在");
        }
        
        String clientIp = clientInfo.getIpAddress();
        String pushUrl = "http://" + clientIp + ":10026/push";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(pushUrl))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();
        
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("推送失败，状态码: " + response.statusCode());
        }
    }

    /// 广播消息
    /// @param message 消息
    public void broadcastMessage(String message) {
        sml.info("[广播] 向所有在线用户广播消息: " + message);
        sml.info("[广播统计] 当前在线用户数: " + connectedClients.size());
            
        if (connectedClients.isEmpty()) {
            sml.warn("[广播警告] 没有在线用户，广播消息未发送");
            return;
        }

        String timestamp = LocalDateTime.now().format(timeFormatter);
        String broadcastMsg = "{\"type\":\"broadcast\",\"message\":\"" + message + "\",\"timestamp\":\"" + timestamp + "\"}";
            
        int successCount = 0;
        int failCount = 0;
            
        for (ClientInfo clientInfo : connectedClients.values()) {
            try {
                sendPushMessage(clientInfo.getClientId(), broadcastMsg);
                sml.info("[广播发送] 向用户 '" + clientInfo.getUsername() + "' (ID: " + clientInfo.getClientId() + ") 发送广播消息");
                successCount++;
            } catch (Exception e) {
                sml.error("[广播失败] 向用户 '" + clientInfo.getUsername() + "' 发送广播时出错: " + e.getMessage());
                failCount++;
            }
        }
            
        sml.info("[广播完成] 广播发送统计 - 成功: " + successCount + "人, 失败: " + failCount + "人");
    }

};
