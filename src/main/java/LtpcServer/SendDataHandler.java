package LtpcServer;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/20 9:44
 */

import Spark.MapsData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: BG_DAQ_DataSource
 * @description:
 * @author: WuYe
 * @create: 2020-11-20 09:44
 **/
public class SendDataHandler extends ChannelInboundHandlerAdapter {
    private List<ByteBuf> pckList;
    private long trigger;
    private long startTime;
    private int ladder;
    private AtomicInteger connectNum;
    private AtomicLong sendDataSize;
    private AtomicLong firstTime;
    private ByteBuf stopPck;
    private ByteBuf lengthFixedPck;

    public static void main(String[] args) {
    }


    public SendDataHandler(List<ByteBuf> pckList, long trigger, AtomicInteger connectNum, AtomicLong sendDataSize, AtomicLong firstTime) {
        this.pckList = pckList;
        this.trigger = trigger;
        this.connectNum = connectNum;
        this.sendDataSize = sendDataSize;
        this.firstTime = firstTime;
        this.stopPck = MapsData.creatNullPck(0, 0);
        this.lengthFixedPck = MapsData.creatLengthFixedPck(0, 0);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws ExecutionException, InterruptedException {
        startTime = System.currentTimeMillis();
        long l = sendPckListData(ctx);
        ctx.fireChannelActive();
    }

    private long sendDifferentData(ChannelHandlerContext ctx) {
        long byteCount = 0;
        for (int i = 1; i <= trigger; i++) {
            int length = new Random().nextInt(50) * 4 + MapsData.basePckLength;
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(length);
            byteCount += length;
            byteBuf.writeInt(ladder);
            byteBuf.writeInt(length);
            byteBuf.writeInt(i);
            byteBuf.writerIndex(length - 4);
            byteBuf.writeInt(0x22334455);
            ctx.writeAndFlush(byteBuf);
        }
        return byteCount;
    }

    private long sendPckListData(ChannelHandlerContext ctx) throws ExecutionException, InterruptedException {
        long byteCount = 0;
//        for (int i = 1; i <= trigger; i++) {
//            ByteBuf byteBuf = pckList.get(new Random().nextInt(pckList.size()));
//            byteCount += byteBuf.readableBytes() ;
//            byteBuf.retain();
//            ByteBuf duplicate = byteBuf.duplicate();
//            ctx.writeAndFlush(duplicate);
//        }
//        return byteCount;

        int sendNum = 0;
        while (ctx.channel().isRegistered() ) {
            ByteBuf byteBuf = pckList.get(new Random().nextInt(pckList.size()));
            byteCount += byteBuf.readableBytes();
            byteBuf.retain();
            ByteBuf duplicate = byteBuf.duplicate();
            sendNum++;
            if (sendNum % 10000 == 0) {
                long finalByteCount = byteCount;
                ChannelPromise channelPromise = new DefaultChannelPromise(ctx.channel()).addListener(
                        (ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                float sizeMByte = (finalByteCount / 1048576f);
                                long end = System.currentTimeMillis();
                                float rate = finalByteCount / (1048.576f * (end - startTime));
                                System.out.printf("%s  send %f MB data,use time %.2fS,rate %.2f MB/S\n", Thread.currentThread().getName(), sizeMByte,
                                        (end - startTime) / 1000f, rate);
                            }
                        }
                );
                ctx.writeAndFlush(duplicate, channelPromise);
            }
            ctx.write(duplicate);
        }
        return byteCount;
    }

    private void sendLengthFixedData(ChannelHandlerContext ctx) {
        long byteCount = 0;
        for (int i = 1; i < trigger; i++) {
            byteCount += lengthFixedPck.readableBytes();
            lengthFixedPck.retain();
            ByteBuf duplicate = lengthFixedPck.duplicate();
//            ChannelPromise sleepChannelPromise = new DefaultChannelPromise(ctx.channel()).addListener(
//                    (ChannelFutureListener) future -> {
//                        if (future.isSuccess()) {
//                            Thread.sleep(100);
//                        }
//                    }
//            );
//            ctx.writeAndFlush(duplicate,sleepChannelPromise);
            ctx.writeAndFlush(duplicate);
        }
        long finalByteCount = byteCount;
        ChannelPromise channelPromise = new DefaultChannelPromise(ctx.channel()).addListener(
                (ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        float sizeMByte = (finalByteCount / 1048576f);
                        long end = System.currentTimeMillis();
                        float rate = finalByteCount / (1048.576f * (end - startTime));
                        System.out.printf("%s  send %f MB data,use time %.2fS,rate %.2f MB/S\n", Thread.currentThread().getName(), sizeMByte,
                                (end - startTime) / 1000f, rate);
                        sendDataSize.set(sendDataSize.get() + finalByteCount);
                    }
                }
        );
        System.out.println(Thread.currentThread().getName() + "send " + trigger + " pck over");
        ctx.writeAndFlush(lengthFixedPck, channelPromise);
    }

    private void sendFinPck(ChannelHandlerContext ctx, long finalByteCount) {
        ChannelPromise channelPromise = new DefaultChannelPromise(ctx.channel()).addListener(
                (ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        float sizeMByte = (finalByteCount / 1048576f);
                        long end = System.currentTimeMillis();
                        float rate = finalByteCount / (1048.576f * (end - startTime));
                        System.out.printf("%s stop send %f MB data,use time %.2fS,rate %.2f MB/S\n", Thread.currentThread().getName(), sizeMByte,
                                (end - startTime) / 1000f, rate);
                        sendDataSize.set(sendDataSize.get() + finalByteCount);
                    }
                }
        );
        System.out.println(Thread.currentThread().getName() + "send stop pck :" + stopPck.readableBytes());
        ctx.writeAndFlush(stopPck, channelPromise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf result = (ByteBuf) msg;
        byte[] bytes = new byte[result.readableBytes()];
        result.readBytes(bytes);

        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        int i = connectNum.incrementAndGet();
        ladder = i;
        if (i == 1) {
            sendDataSize.set(0);
            firstTime.set(System.currentTimeMillis());
        }
        System.out.printf("--------%s------------------channel Registered :%d \n",
                Thread.currentThread().getName(), i);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        int i = connectNum.decrementAndGet();
        System.out.printf("--------%s------------------channel Unregistered :%d \n",
                Thread.currentThread().getName(), i);
        if (i == 0) {
            float l = sendDataSize.get() / 1048576f;
            float rate = l * 1000 / (System.currentTimeMillis() - firstTime.get());
            System.out.printf("\nall thread send %.2f MB data , rate : %.2f MB/S\n", l, rate);
        }
        super.channelUnregistered(ctx);
    }
}
