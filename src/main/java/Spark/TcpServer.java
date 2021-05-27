package Spark;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/10/29 21:18
 */


import Util.IntByteConvert;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: BG_DAQ_FrameWork
 * @description:
 * @author: WuYe
 * @create: 2020-10-29 21:18
 **/
public class TcpServer {
    private static ExecutorService executorService;
    private static int speed = 200;
    private static int serverNum = 64;
    private static byte[] bytes;
    private static ArrayList<byte[]> pckList;

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            speed = Integer.parseInt(args[0]);
        } else {
            throw new RuntimeException("argc error;Please input parameter: speed");
        }
        executorService = Executors.newFixedThreadPool(serverNum);

        TcpServer tcpServer = new TcpServer();
        tcpServer.setSocketServever(20000, 0);

    }

    public void setSocketServever(int port, int header) throws Exception {
        ServerSocket server = new ServerSocket(port);
        System.out.println(port + " 等待与客户端建立连接...");
        try {

            while (true) {
                Socket accept = server.accept();
                executorService.submit(new Task(accept, header));
            }
        } finally {
            executorService.shutdown();
        }
    }

    static class Task implements Callable<Float> {

        private int header;
        private long startTime;
        private Socket socket;

        public Task(Socket socket, int header) {
            this.socket = socket;
            this.header = header;
        }

        @Override
        public Float call() {
            try {
                // server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
                System.out.println("connect--> " + socket.toString()+"\n");
                startTime = System.currentTimeMillis();
                handlerSocketFast();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return 0f;
        }

        //定长最快包
        private void handlerSocketFast() throws Exception {
            // 回应一下客户端
            OutputStream outputStream = socket.getOutputStream();
            byte[] bytes = new byte[1024*8];
            for(int i=0;i<4;i++){
                IntByteConvert.intToByteArray(bytes, 4+2048*i, 2048);
                IntByteConvert.intToByteArray(bytes, 2048*(i+1) - 4, 0x22334455);
            }

            int sleepTimes = 0;
            SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
            String name = Thread.currentThread().getName();
            long fistStartTime = System.currentTimeMillis();
            while (!socket.isClosed()) {
                //startTime = System.currentTimeMillis();
                for (int i = 0; i < (speed/4); i++) {
                    outputStream.write(bytes);
                }
                outputStream.flush();
                sleepTimes++;
                //long stopTime = System.currentTimeMillis();
                //Thread.sleep(1000-stopTime+startTime);
                if (sleepTimes % 1 == 0) {
                    long endTime = System.currentTimeMillis();
                    float rate =  speed *sleepTimes*2/(1.024f*(endTime - fistStartTime));
                    System.out.printf("---->%s , ####%s ; useTime:  %.2fS ; rate: %.2f MB/S ", remoteSocketAddress,name,
                            ((endTime - fistStartTime) / 1000f)
                            , rate);
                    System.out.println("");
                    //startTime=endTime;
                }
            }
        }
        //不定长随机包
        private void handlerSocketVariable() throws Exception {

            // 回应一下客户端
            OutputStream outputStream = socket.getOutputStream();
            byte[] bytes = new byte[8192];
            IntByteConvert.intToByteArray(bytes, 4, 8192);
            IntByteConvert.intToByteArray(bytes, 8192 - 4, 0x22334455);
            startTime = System.currentTimeMillis();
            int sleepTimes = 0;
            while (!socket.isClosed()) {
                for (int i = 0; i < (speed * 128 / 10); i++) {
                    outputStream.write(bytes);
                }
                outputStream.flush();
                sleepTimes++;
                if (sleepTimes % 30 == 0) {
                    long endTime = System.currentTimeMillis();
                    float rate =  speed *3000f/ (endTime - startTime);
                    System.out.printf("\n---->%s , ####%s ; useTime:  %.2fS ; rate: %.2f MB/S ", socket.getRemoteSocketAddress(),Thread.currentThread().getName(),
                            ((endTime - startTime) / 1000f)
                            , rate);
                    startTime=endTime;
                }
                Thread.sleep(100);
            }
        }
    }
}
