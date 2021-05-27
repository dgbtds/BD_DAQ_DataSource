package Netty.TestDemo;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/19 16:05
 */

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
 * @create: 2020-11-19 16:05
 **/
public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 通过SocketChannel需获取对应的管道
        ChannelPipeline pipeline = ch.pipeline();
        //inboundHandler
        pipeline.addLast("InboundHandlerA", new InboundHandlerA());
        pipeline.addLast("InboundHandlerB", new InboundHandlerB());
        pipeline.addLast("InboundHandlerC", new InboundHandlerC());

        //OutboundHandler
        pipeline.addLast("OutboundHandlerA", new OutboundHandlerA());
        pipeline.addLast("OutboundHandlerB", new OutboundHandlerB());
        pipeline.addLast("OutboundHandlerC", new OutboundHandlerC());
    }
}
