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

package mod.deplayer.coffeechat.coffeeirc.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

/**CIC客户端核心*/
public class Client {

    private static final Logger cml = LogManager.getLogger(Client.class);

    private String DistributionName;

    private String ip;

    private int port;

    private int ipProtocol;

    private String nickname;

    private String username;
        
    private String servername;
        
    private String serverdescription;
        
    private String clientId;
        
    private boolean isConnected = false;
    

    private PrintWriter chatLogWriter;
    private String currentChatLogDate;
    private DateTimeFormatter chatLogFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    

    private HttpServer pushServer;
    private int pushPort = 10026;
        
    private HttpClient clientHttp = HttpClient.newHttpClient();
    
    public Client(int ipProtocol, String ip, int port, String nickname, String username, String DistributionName) {
        cml.info("---------------------------------------------------------------------------------");
        cml.info("[核心信息]正在使用的CoffeeIRC核心的软件信息:");
        cml.info("        版本号: " + mod.deplayer.coffeechat.coffeeirc.client.SwInfo.version);
        cml.info("        开发状态: " + mod.deplayer.coffeechat.coffeeirc.client.SwInfo.softwareStatus);
        cml.info("        版本代号: " + mod.deplayer.coffeechat.coffeeirc.client.SwInfo.VerCodename);
        cml.info("        支持协议: " + mod.deplayer.coffeechat.coffeeirc.client.SwInfo.connection);
        cml.info("");
        cml.info("当前运行本核心的发行版: " + DistributionName);
        cml.info("");
        cml.info("如果遇到核心问题，请提交至: https://github.com/deplayeris/coffeeirc/issues");
        cml.info("如在使用基于本核心的发行版(如无忧聊)时出现问题");
        cml.info("请先检查是否为核心故障(通过查看核心日志)，若非核心问题请联系发行版作者");
        cml.info("");
        cml.info("核心问题提交步骤:");
        cml.info("1. 在GitHub上创建新的Issue");
        cml.info("2. 详细准确地描述遇到的问题");
        cml.info("3. 附上出现问题时的核心日志文件");
        cml.info("---------------------------------------------------------------------------------");
        this.ipProtocol = ipProtocol;
        this.ip = ip;
        this.port = port;
        this.nickname = nickname;
        this.username = username;
        cml.info("[客户端初始化] 开始创建客户端实例");
        cml.info("[配置详情] IP协议版本: IPv" + ipProtocol);
        cml.info("[配置详情] 服务器地址: " + ip + ":" + port);
        cml.info("[用户信息] 用户昵称: " + nickname);
        cml.info("[用户信息] 用户名: " + username);
        cml.info("[实例创建] 客户端实例已成功创建并配置完成");

        initializeChatLog();

        startPushService();

    }
    /**记录聊天日志所必须要使用的一个Method*/
    private void initializeChatLog() {
        try {
            currentChatLogDate = LocalDate.now().toString();
            String logFileName = "./ciclogs/chatlog-c-" + currentChatLogDate + ".log";
            FileWriter fileWriter = new FileWriter(logFileName, true);
            chatLogWriter = new PrintWriter(fileWriter, true);
            
            cml.info("[聊天日志] 聊天日志系统已初始化，日志文件: " + logFileName);
            
        } catch (IOException e) {
            cml.error("[聊天日志错误] 初始化聊天日志失败: " + e.getMessage());
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
            cml.error("[聊天日志错误] 记录聊天消息失败: " + e.getMessage());
        }
    }

    /**在使用完毕之后，必须关闭聊天日志记录*/
    private void closeChatLog() {
        if (chatLogWriter != null) {
            chatLogWriter.close();
            chatLogWriter = null;
            cml.info("[聊天日志] 聊天日志文件已关闭");
        }
    }

    /**这个推送服务是用来接收服务器广播消息、客户端发送请求到服务端执行动作的，因此，必须启动*/
    private void startPushService() {
        try {
            pushServer = HttpServer.create(new InetSocketAddress(pushPort), 0);
            pushServer.createContext("/push", new PushHandler());
            pushServer.setExecutor(null);
            pushServer.start();
            cml.info("[推送服务] 推送服务已在端口 " + pushPort + " 启动");
        } catch (IOException e) {
            cml.error("[推送服务错误] 启动推送服务失败: " + e.getMessage());
        }
    }

    /**在使用完毕之后，必须关闭推送服务*/
    private void stopPushService() {
        if (pushServer != null) {
            pushServer.stop(0);
            pushServer = null;
            cml.info("[推送服务] 推送服务已停止");
        }
    }

    /**推送处理器类*/
    class PushHandler implements HttpHandler {
        @Override
        /**处理推送消息的处理器*/
        public void handle(HttpExchange exchange) throws IOException {
            //至于为什么使用 push 而不使用 pull这个命名，这是受到 github 的熏陶（pull request就是由push者发送）
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    InputStream inputStream = exchange.getRequestBody();
                    String requestBody = new String(inputStream.readAllBytes());
                    inputStream.close();
                    
                    cml.info("[推送接收] 收到推送消息: " + requestBody);
                    
                    String response = "{\"status\":\"success\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    
                } catch (Exception e) {
                    cml.error("[推送错误] 处理推送消息时出错: " + e.getMessage());
                    String response = "{\"status\":\"error\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();//这里就是一坨灾难
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    /**连接*/
    public void Connect() {
        cml.info("[连接] 尝试连接到服务器 http://" + ip + ":" + port + " (用户: " + username + ")");
        
        try {
            String serverUrl = "http://" + ip + ":" + port;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/connect"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"nickname\":\"" + nickname + "\",\"username\":\"" + username + "\"}"))
                    .build();
            
            HttpResponse<String> response = clientHttp.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {

                String responseBody = response.body();
                if (responseBody.contains("\"clientId\":\"")) {
                    int start = responseBody.indexOf("\"clientId\":\"") + 12;
                    int end = responseBody.indexOf("\"", start);
                    if (end > start) {
                        clientId = responseBody.substring(start, end);
                        isConnected = true;
                        cml.info("[连接成功] 已连接到服务器，客户端ID: " + clientId);
                    }
                } else {
                    cml.warn("[警告] 服务器未返回客户端ID");
                    isConnected = true;
                }
            } else {
                cml.error("[连接失败] 状态码: " + response.statusCode() + ", 响应: " + response.body());
            }
            
        } catch (Exception e) {
            cml.error("[连接异常] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// 发送消息
    /// @param message 要发送的消息
    public void sendMessage(String message) {
        if (!isConnected) {
            cml.warn("[发送失败] 客户端未连接");
            return;
        }
        
        cml.info("[发送] '" + nickname + "' 发送消息: " + message);
        

        logChatMessage(nickname, message);
        
        try {
            String serverUrl = "http://" + ip + ":" + port;
            String requestBody = "{\"nickname\":\"" + nickname + "\",\"message\":\"" + message + "\"";
            

            if (clientId != null && !clientId.isEmpty()) {
                requestBody = requestBody.substring(0, requestBody.length() - 1) + ",\"clientId\":\"" + clientId + "\"}";
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/message"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = clientHttp.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                cml.info("[发送成功] 消息已送达服务器");
            } else {
                cml.error("[发送失败] 状态码: " + response.statusCode() + ", 响应: " + response.body());
            }
            
        } catch (Exception e) {
            cml.error("[发送异常] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**断开连接*/
    public void disconnect() {
        if (!isConnected) {
            cml.warn("[断开失败] 客户端未连接");
            return;
        }

        cml.info("[断开] '" + nickname + "' 正在断开连接");

        try {
            String serverUrl = "http://" + ip + ":" + port;
            String requestBody = "{\"nickname\":\"" + nickname + "\"";

            if (clientId != null && !clientId.isEmpty()) {
                requestBody = requestBody.substring(0, requestBody.length() - 1) + ",\"clientId\":\"" + clientId + "\"}";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/disconnect"))
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = clientHttp.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                cml.info("[断开成功] 已从服务器断开");

                isConnected = false;
                clientId = null;
                closeChatLog();
                stopPushService();
                stopPushService();
            } else {
                cml.warn("[断开异常] 状态码: " + response.statusCode());
                isConnected = false;
                clientId = null;
                closeChatLog();
                stopPushService();
            }

        } catch (Exception e) {
            cml.error("[断开异常] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            isConnected = false;
            clientId = null;
            closeChatLog();
            stopPushService();
            
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

    }

}
