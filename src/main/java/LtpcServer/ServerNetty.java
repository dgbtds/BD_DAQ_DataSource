package LtpcServer;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/19 15:55
 */

import Spark.MapsData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCounted;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: BG_DAQ_DataSource
 * @description:
 * @author: WuYe
 * @create: 2020-11-19 15:55
 **/
public class ServerNetty {
    private static List<ByteBuf> pckList;
    private long trigger;
    private int port;
    private AtomicInteger connectNum =new AtomicInteger(0);
    private AtomicLong sendDataSize = new AtomicLong(0L);
    private AtomicLong firstTime = new AtomicLong(0L);

    public ServerNetty(int port, long trigger, int basePckLength) {
        this.port = port;
        this.trigger = trigger;
        //       pckList= MapsData.creatVariableLengthPackages(0, 0, basePckLength);
        MapsData.basePckLength=basePckLength;
        pckList= MapsData.writeDirectCompositeBuffer(0, 0);
    }

    // netty 服务端启动
    public void action() throws InterruptedException {
        // 用来接收进来的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 用来处理已经被接收的连接，一旦bossGroup接收到连接，就会把连接信息注册到workerGroup上
        EventLoopGroup workerGroup = new NioEventLoopGroup(64);

        try {
            // nio服务的启动类
            ServerBootstrap sbs = new ServerBootstrap();
            // 配置nio服务参数
            sbs.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.ERROR))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 通过SocketChannel需获取对应的管道
                            ChannelPipeline pipeline = ch.pipeline();
                            //inboundHandler
                            pipeline.addLast("SendDataHandler", new SendDataHandler(pckList,trigger ,connectNum,sendDataSize,firstTime));
                        }
                    });

            System.err.println("server 开启--------------");
            // 绑定端口，开始接受链接
            ChannelFuture cf = sbs.bind(port).sync();

            // 开多个端口
//            ChannelFuture cf2 = sbs.bind(3333).sync();
            //cf2.channel().closeFuture().sync();

            //监控直接内存
//            new DirectMemReporter().startReport();

            // 等待服务端口的关闭；在这个例子中不会发生，但你可以优雅实现；关闭你的服务
            cf.channel().closeFuture().sync();
        } finally {
            if (pckList!=null){
                pckList.forEach(ReferenceCounted::release);
            }
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    // 开启netty服务线程
    public static void main(String[] args) throws InterruptedException {
        long trigger;
        int basePckLength=100;
        if (args.length == 2) {
            trigger = Long.parseLong(args[0]);
            basePckLength = Integer.parseInt(args[1]);
        } else {
            throw new RuntimeException("argc error;Please input parameter: trigger ,basePckLength");
        }
        new ServerNetty(20000,trigger,basePckLength).action();
    }

}
