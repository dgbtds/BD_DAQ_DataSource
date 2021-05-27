package Spark;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/23 13:06
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: BG_DAQ_DataSource
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-11-23 13:06
 **/
public class TestServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(12345);
        System.out.println(12345 + " 等待与客户端建立连接...");
        Socket accept = server.accept();
        // server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
        System.out.println("connect--> " + accept.toString());
    }
}
