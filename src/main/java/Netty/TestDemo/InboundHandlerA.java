package Netty.TestDemo;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/19 16:30
 */

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @program: BG_DAQ_DataSource
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-11-19 16:30
 **/
public class InboundHandlerA extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("InboundHandlerA.channelActive");
        ctx.fireChannelActive();
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("InboundHandlerA.channelRead");

        ByteBuf result = (ByteBuf) msg;
        byte[] bytes = new byte[result.readableBytes()];
        result.readBytes(bytes);
        System.out.println("client said:"+new String(bytes));

        ctx.fireChannelRead(msg);
    }
}
