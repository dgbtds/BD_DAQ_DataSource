package Netty.TestDemo;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/19 15:55
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @program: BG_DAQ_DataSource
 * @description:
 * @author: WuYe
 * @create: 2020-11-19 15:55
 **/
public class ServerNetty {
    private int port;

    public ServerNetty(int port) {
        this.port = port;
    }

    // netty 服务端启动
    public void action() throws InterruptedException {

        // 用来接收进来的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 用来处理已经被接收的连接，一旦bossGroup接收到连接，就会把连接信息注册到workerGroup上
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // nio服务的启动类
            ServerBootstrap sbs = new ServerBootstrap();
            // 配置nio服务参数
            sbs.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.ERROR))
                    .childHandler(new MyChannelInitializer());

            System.err.println("server 开启--------------");
            // 绑定端口，开始接受链接
            ChannelFuture cf = sbs.bind(port).sync();

            // 开多个端口
            //ChannelFuture cf2 = sbs.bind(3333).sync();
            //cf2.channel().closeFuture().sync();

            // 等待服务端口的关闭；在这个例子中不会发生，但你可以优雅实现；关闭你的服务
            cf.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    // 开启netty服务线程
    public static void main(String[] args) throws InterruptedException {
        new ServerNetty(20000).action();
    }

}
