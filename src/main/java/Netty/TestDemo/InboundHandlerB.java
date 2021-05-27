package Netty.TestDemo;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/19 16:33
 */

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @program: BG_DAQ_DataSource
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-11-19 16:33
 **/
public class InboundHandlerB extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("InboundHandlerB.channelActive");
        String msg="InboundHandlerB write msg";
        ByteBuf encode = ctx.alloc().buffer(4 * msg.length());
        encode.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        ctx.channel().writeAndFlush(encode);
        ctx.fireChannelActive();
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("InboundHandlerB");
        ctx.fireChannelRead(msg);
    }
}
