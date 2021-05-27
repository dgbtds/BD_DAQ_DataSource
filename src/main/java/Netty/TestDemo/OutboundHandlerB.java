package Netty.TestDemo;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/19 16:35
 */

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.TimeUnit;

/**
 * @program: BG_DAQ_DataSource
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-11-19 16:35
 **/
public class OutboundHandlerB extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        System.out.println("OutboundHandlerB");
        ctx.write(msg, promise);
    }
}
