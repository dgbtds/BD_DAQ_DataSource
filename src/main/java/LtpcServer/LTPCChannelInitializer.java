package LtpcServer;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/20 9:42
 */

import Netty.TestDemo.InboundHandlerA;
import Netty.TestDemo.OutboundHandlerA;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @program: BG_DAQ_DataSource
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-11-20 09:42
 **/
public class LTPCChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 通过SocketChannel需获取对应的管道
        ChannelPipeline pipeline = ch.pipeline();
        //inboundHandler
        pipeline.addLast("InboundHandlerA", new InboundHandlerA());

        //OutboundHandler
        pipeline.addLast("OutboundHandlerA", new OutboundHandlerA());
    }
}

