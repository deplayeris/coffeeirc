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

package mod.deplayer.coffeechat.coffeeirc;

import mod.deplayer.coffeechat.coffeeirc.client.Client;
import mod.deplayer.coffeechat.coffeeirc.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
/*
public class CDTE {

    private static final Logger mainlogger = LogManager.getLogger(CDTE.class);
    public static void main(String[] args) {

        mainlogger.info("这里是CIC Default Test Environment(CDTE)，CIC官方默认测试环境发行版，Version 3");

        String s = System.console().readLine("请选择测试环境：1. 服务器 2. 客户端");

        if (s.trim().equals("1")) {
            Server server = new Server(4, 10025, "CIC Test Server",
                    "Test", "CDTE");
            try {
                server.startServer();
            } catch (IOException e) {
                mainlogger.error("服务器启动失败");
                e.printStackTrace();
            }
        }
        else if (s.trim().equals("2")) {
            Client client = new Client(4, "127.0.0.1", 10025, "CIC Test Client", "CDTE", "CDTE");
            client.Connect();
            client.sendMessage("Hello World!");
            client.disconnect();
        }




    };
};
 */
